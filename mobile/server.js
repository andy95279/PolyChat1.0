import http from 'http';
import https from 'https';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import pkg from 'pg';
const { Pool } = pkg;

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const PORT = 8080;

// Neon PostgreSQL connection pool
const pool = new Pool({
  connectionString: 'postgresql://neondb_owner:npg_H6cDK9LrgMpI@ep-old-paper-almcvnjx-pooler.c-3.eu-central-1.aws.neon.tech/neondb?sslmode=require',
  max: 5
});

// MIME types for static files
const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml'
};

// Color palette for users (assigned by index)
const USER_COLORS = ['#3B82F6', '#8B5CF6', '#10B981', '#F59E0B', '#EC4899', '#F97316', '#06B6D4', '#EF4444'];

// Map nationality strings to ISO 639-1 language codes
function getLangCode(nationality) {
  if (!nationality) return 'es';
  const n = nationality.toLowerCase();
  if (n.includes('english') || n.includes('inglés') || n.includes('ingles') || n.startsWith('en')) return 'en';
  if (n.includes('french') || n.includes('français') || n.includes('frances') || n.startsWith('fr')) return 'fr';
  if (n.includes('deutsch') || n.includes('german') || n.includes('alemán') || n.startsWith('de')) return 'de';
  if (n.includes('italian') || n.includes('italiano') || n.startsWith('it')) return 'it';
  if (n.includes('portuguese') || n.includes('português') || n.includes('portugués') || n.startsWith('pt')) return 'pt';
  if (n.includes('russian') || n.includes('ruso') || n.startsWith('ru')) return 'ru';
  if (n.includes('chinese') || n.includes('chino') || n.startsWith('zh')) return 'zh';
  if (n.includes('japanese') || n.includes('japonés') || n.startsWith('ja')) return 'ja';
  if (n.includes('arabic') || n.includes('árabe') || n.startsWith('ar')) return 'ar';
  return 'es'; // default Spanish
}

// Translate text using unofficial Google Translate API (no key needed)
async function translateText(text, sourceLang, targetLang) {
  if (!text || sourceLang === targetLang) return null;
  return new Promise((resolve) => {
    const encoded = encodeURIComponent(text);
    const path = `/translate_a/single?client=gtx&sl=${sourceLang}&tl=${targetLang}&dt=t&q=${encoded}`;
    const options = {
      hostname: 'translate.googleapis.com',
      path,
      method: 'GET',
      headers: { 'User-Agent': 'Mozilla/5.0' },
      timeout: 8000
    };
    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try {
          const json = JSON.parse(data);
          // Response format: [ [ ["translated","original",...], ... ], ... ]
          const parts = json[0];
          if (!Array.isArray(parts)) { resolve(null); return; }
          const translated = parts.map(p => p[0]).filter(Boolean).join('');
          resolve(translated || null);
        } catch { resolve(null); }
      });
    });
    req.on('error', () => resolve(null));
    req.on('timeout', () => { req.destroy(); resolve(null); });
    req.end();
  });
}

// Build avatar initials from name + apellidos
function makeAvatar(nombre, apellidos) {
  const first = (nombre || '?')[0].toUpperCase();
  const second = (apellidos || '?')[0].toUpperCase();
  return first + second;
}

// ===================== API HANDLERS =====================

// GET /api/users — all users from the 'usuarios' table
async function handleGetUsers(res) {
  const { rows } = await pool.query('SELECT * FROM usuarios ORDER BY nombre');
  const users = rows.map((u, i) => ({
    id: u.id_usuario,
    name: u.nombre,
    surnames: u.apellidos || '',
    email: u.email || '',
    phone: u.tlf || '',
    language: u.nacionalidad || 'es Español',
    age: u.edad || 0,
    avatar: makeAvatar(u.nombre, u.apellidos),
    color: USER_COLORS[i % USER_COLORS.length]
  }));
  sendJson(res, users);
}

// GET /api/chats?userId=xxx — chats for a specific user
async function handleGetChats(res, userId) {
  if (!userId) { sendJson(res, [], 400); return; }

  // Get chats where this user is a member
  const { rows: chatRows } = await pool.query(`
    SELECT c.id_chat, c.fecha_creacion, c.creado_por
    FROM chat c
    JOIN chat_miembro cm ON c.id_chat = cm.id_chat
    WHERE cm.id_user = $1
    ORDER BY c.fecha_creacion DESC
  `, [userId]);

  // Get all users for lookup
  const { rows: allUsers } = await pool.query('SELECT * FROM usuarios');
  const userMap = {};
  allUsers.forEach((u, i) => {
    userMap[u.id_usuario] = {
      id: u.id_usuario,
      nombre: u.nombre,
      apellidos: u.apellidos || '',
      nacionalidad: u.nacionalidad || 'Español',
      avatar: makeAvatar(u.nombre, u.apellidos),
      color: USER_COLORS[i % USER_COLORS.length]
    };
  });

  // Get the current user's language
  const currentUserRow = allUsers.find(u => u.id_usuario === userId);
  const myLangCode = getLangCode(currentUserRow ? currentUserRow.nacionalidad : 'es');

  const chats = [];
  for (const chat of chatRows) {
    // Get the other participant(s) in this chat
    const { rows: members } = await pool.query(
      'SELECT id_user FROM chat_miembro WHERE id_chat = $1 AND id_user != $2',
      [chat.id_chat, userId]
    );
    if (members.length === 0) continue;

    const participantId = members[0].id_user;
    const participant = userMap[participantId];
    if (!participant) continue;

    const participantLangCode = getLangCode(participant.nacionalidad);
    const needsTranslation = myLangCode !== participantLangCode;

    // Get messages for this chat
    const { rows: msgs } = await pool.query(
      'SELECT * FROM mensajes WHERE id_chat = $1 ORDER BY fecha_envio ASC',
      [chat.id_chat]
    );

    const messages = [];
    for (const m of msgs) {
      const d = new Date(m.fecha_envio);
      const time = d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0');
      const isFromOther = m.id_usuario !== userId;
      const originalText = m.texto_original || '';

      let displayText = originalText;
      let originalForToggle = null;
      let srcLangCode = m.idioma_origen || (isFromOther ? participantLangCode : myLangCode);

      // Translate messages received from the other user
      if (isFromOther && needsTranslation && originalText) {
        const translated = await translateText(originalText, srcLangCode, myLangCode);
        if (translated && translated !== originalText) {
          displayText = translated;       // show translation by default
          originalForToggle = originalText; // allow toggling to original
        }
      }

      messages.push({
        id: String(m.id_mensaje),
        senderId: m.id_usuario,
        text: displayText,
        original: originalForToggle,
        srcLang: isFromOther && needsTranslation ? participantLangCode.toUpperCase() : null,
        time,
        isAudio: m.es_audio || false
      });
    }

    const lastMsg = messages.length > 0 ? messages[messages.length - 1].text : 'Sin mensajes';

    chats.push({
      id: String(chat.id_chat),
      participantId,
      name: participant.nombre + ' ' + participant.apellidos,
      lang: participant.nacionalidad,
      status: 'En línea',
      avatar: participant.avatar,
      color: participant.color,
      lastMsg,
      messages
    });
  }

  sendJson(res, chats);
}

// GET /api/contacts?userId=xxx — contacts for a user
async function handleGetContacts(res, userId) {
  if (!userId) { sendJson(res, [], 400); return; }

  const { rows: allUsers } = await pool.query('SELECT * FROM usuarios');
  const userMap = {};
  allUsers.forEach((u, i) => {
    userMap[u.id_usuario] = { ...u, _color: USER_COLORS[i % USER_COLORS.length] };
  });

  const { rows: contactRows } = await pool.query(
    'SELECT id_contacto FROM contactos WHERE id_usuario = $1',
    [userId]
  );

  const contacts = contactRows.map(c => {
    const u = userMap[c.id_contacto];
    if (!u) return null;
    return {
      id: u.id_usuario,
      name: u.nombre + ' ' + (u.apellidos || ''),
      lang: u.nacionalidad || 'Español',
      color: u._color,
      avatar: makeAvatar(u.nombre, u.apellidos),
      online: true
    };
  }).filter(Boolean);

  sendJson(res, contacts);
}

// GET /api/stories — stories built from recent user activity
async function handleGetStories(res, userId) {
  // Generate stories from users who have recent messages (simulating real stories)
  const { rows: allUsers } = await pool.query('SELECT * FROM usuarios ORDER BY ultima_conexion DESC');
  const stories = allUsers
    .filter(u => u.id_usuario !== userId)
    .map((u, i) => {
      const storyTexts = [
        `Conectado desde ${u.nacionalidad} 🌍`,
        `Último mensaje reciente 💬`,
        `Activo en PolyChat 🚀`
      ];
      return {
        id: String(i + 1),
        userId: u.id_usuario,
        userName: u.nombre,
        avatar: makeAvatar(u.nombre, u.apellidos),
        color: USER_COLORS[i % USER_COLORS.length],
        content: storyTexts[i % storyTexts.length],
        imageUrl: null
      };
    });

  sendJson(res, stories);
}

// GET /api/login?phone=xxx — find user by phone
async function handleLogin(res, phone) {
  if (!phone) { sendJson(res, { error: 'No phone' }, 400); return; }
  const cleaned = phone.replace(/\s/g, '');
  const { rows } = await pool.query('SELECT * FROM usuarios');
  // Try to match phone (with or without spaces)
  const user = rows.find(u => (u.tlf || '').replace(/\s/g, '') === cleaned);
  if (!user) {
    sendJson(res, { error: 'Usuario no encontrado' }, 404);
    return;
  }
  const idx = rows.indexOf(user);
  sendJson(res, {
    id: user.id_usuario,
    name: user.nombre,
    surnames: user.apellidos || '',
    email: user.email || '',
    phone: user.tlf || '',
    language: user.nacionalidad || 'es Español',
    age: user.edad || 0,
    avatar: makeAvatar(user.nombre, user.apellidos),
    color: USER_COLORS[idx % USER_COLORS.length]
  });
}

// ===================== HELPERS =====================
function sendJson(res, data, status = 200) {
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Access-Control-Allow-Origin': '*'
  });
  res.end(JSON.stringify(data));
}

function parseQuery(url) {
  const qIdx = url.indexOf('?');
  if (qIdx === -1) return {};
  const params = {};
  url.substring(qIdx + 1).split('&').forEach(p => {
    const [k, v] = p.split('=');
    params[decodeURIComponent(k)] = decodeURIComponent(v || '');
  });
  return params;
}

// ===================== SERVER =====================
const server = http.createServer(async (req, res) => {
  const urlPath = req.url.split('?')[0];
  const query = parseQuery(req.url);

  // CORS preflight
  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type'
    });
    res.end();
    return;
  }

  // API Routes
  try {
    if (urlPath === '/api/users') {
      await handleGetUsers(res);
      return;
    }
    if (urlPath === '/api/chats') {
      await handleGetChats(res, query.userId);
      return;
    }
    if (urlPath === '/api/contacts') {
      await handleGetContacts(res, query.userId);
      return;
    }
    if (urlPath === '/api/stories') {
      await handleGetStories(res, query.userId);
      return;
    }
    if (urlPath === '/api/login') {
      await handleLogin(res, query.phone);
      return;
    }
  } catch (err) {
    console.error('API Error:', err);
    sendJson(res, { error: 'Internal server error' }, 500);
    return;
  }

  // Static file serving
  let filePath = urlPath === '/' ? '/index.html' : urlPath;
  const fullPath = path.join(__dirname, filePath);

  try {
    const data = fs.readFileSync(fullPath);
    const ext = path.extname(fullPath);
    res.writeHead(200, {
      'Content-Type': MIME[ext] || 'text/plain',
      'Access-Control-Allow-Origin': '*'
    });
    res.end(data);
  } catch (err) {
    res.writeHead(404);
    res.end('Not found');
  }
});

server.listen(PORT, () => {
  console.log(`\n✅ PolyChat Mobile Server running on http://localhost:${PORT}`);
  console.log(`   API endpoints:`);
  console.log(`   GET /api/users        — All users`);
  console.log(`   GET /api/login?phone=  — Login by phone`);
  console.log(`   GET /api/chats?userId= — Chats for user`);
  console.log(`   GET /api/contacts?userId= — Contacts`);
  console.log(`   GET /api/stories?userId= — Stories\n`);
});
