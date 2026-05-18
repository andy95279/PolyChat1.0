// Navigation
function navigate(screen, dir='right') {
  const prev = document.querySelector('.screen.active');
  if (prev) { prev.classList.remove('active'); prev.classList.add(dir==='right'?'slide-left':'slide-right'); }
  state.currentScreen = screen;
  render();
  const el = document.querySelector('.screen'); 
  if(el){el.classList.add(dir==='right'?'slide-right':'slide-left');requestAnimationFrame(()=>{requestAnimationFrame(()=>{el.classList.remove('slide-right','slide-left');el.classList.add('active')})});}
}
function render() {
  const app = document.getElementById('app');
  app.innerHTML = '';
  let html = '';
  switch(state.currentScreen) {
    case 'login': html = renderLogin(); break;
    case 'verify': html = renderVerify(); break;
    case 'home': html = renderHome(); break;
    case 'chat': html = renderChat(); break;
    case 'contacts': html = renderContacts(); break;
    case 'addContact': html = renderAddContact(); break;
    case 'notifications': html = renderNotifications(); break;
    case 'settings': html = renderSettings(); break;
  }
  app.innerHTML = html;
  attachEvents();
}
function toast(msg){const t=document.createElement('div');t.className='toast';t.textContent=msg;document.body.appendChild(t);setTimeout(()=>t.remove(),2500)}

// RENDER: Login
function renderLogin(){
  const isReg = state.isRegistering;
  return `<div class="screen active login-screen"><div class="login-card">
    <div class="logo-circle">💬</div>
    <div class="login-title">PolyChat</div>
    <div class="login-subtitle">Mensajería global sin barreras de idioma</div>
    <hr style="width:100%;border:none;border-top:1px solid var(--border)">
    <div class="tab-row">
      <button class="tab-btn ${!isReg?'active':''}" onclick="state.isRegistering=false;render()">Iniciar Sesión</button>
      <button class="tab-btn ${isReg?'active':''}" onclick="state.isRegistering=true;render()">Registrarse</button>
    </div>
    ${isReg ? renderRegisterForm() : renderLoginForm()}
    <div class="error-msg" id="loginError"></div>
    <button class="btn-primary" onclick="handleLogin()">${isReg?'Registrarse →':'Enviar código de verificación →'}</button>
  </div></div>`;
}
function renderLoginForm(){
  return `<div class="form-group"><div class="form-label">📞 Número de teléfono</div>
    <input class="form-input" id="phoneInput" placeholder="123 456 7890" type="tel"></div>
    <div class="demo-box">🛡️ <span>Demo: Tu código llegará pronto a tu dispositivo móvil.</span></div>`;
}
function renderRegisterForm(){
  return `<div class="section-header">INFORMACIÓN PERSONAL</div>
    <div class="form-row"><div class="form-group"><div class="form-label">👤 Nombre</div><input class="form-input" id="regName" placeholder="Juan"></div>
    <div class="form-group"><div class="form-label">👤 Apellidos</div><input class="form-input" id="regSurnames" placeholder="Pérez"></div></div>
    <div class="form-row"><div class="form-group"><div class="form-label">📅 Edad</div><input class="form-input" id="regAge" placeholder="18" type="number"></div>
    <div class="form-group"><div class="form-label">🌐 Idioma</div><select class="form-select" id="regLang">
      <option>es Español</option><option>en English</option><option>fr Français</option><option>de Deutsch</option></select></div></div>
    <div class="section-header">CUENTA Y SEGURIDAD</div>
    <div class="form-group"><div class="form-label">✉ Correo electrónico</div><input class="form-input" id="regEmail" placeholder="juan@ejemplo.com" type="email"></div>
    <div class="form-group"><div class="form-label">📞 Número de teléfono</div><input class="form-input" id="regPhone" placeholder="123 456 7890" type="tel"></div>
    <div class="form-row"><div class="form-group"><div class="form-label">🔒 Contraseña</div><input class="form-input" id="regPass" placeholder="••••••••" type="password"></div>
    <div class="form-group"><div class="form-label">Confirmar</div><input class="form-input" id="regConfirm" placeholder="••••••••" type="password"></div></div>`;
}

// RENDER: Verify
function renderVerify(){
  return `<div class="screen active verify-screen"><div class="login-card">
    <div class="logo-circle">🔐</div>
    <div class="login-title" style="font-size:24px">Verificar Número</div>
    <div class="login-subtitle">Ingresa el código de 6 dígitos enviado a tu teléfono</div>
    <div class="code-row">${[0,1,2,3,4,5].map(i=>`<input class="code-input" id="code${i}" maxlength="1" inputmode="numeric" autocomplete="off">`).join('')}</div>
    <div class="error-msg" id="verifyError"></div>
    <button class="btn-primary" onclick="handleVerify()">Verificar →</button>
    <div class="link-row">
      <button class="link-btn" onclick="navigate('login','left')">← Cambiar número</button>
      <button class="link-btn accent" onclick="autoFillCode()">Reenviar código</button>
    </div>
  </div></div>`;
}

// RENDER: Home
function renderHome(){
  const hasChats = MOCK_CHATS.length > 0;
  return `<div class="screen active home-screen">
    <div class="home-header">
      <span style="font-size:24px">💬</span>
      <div class="home-header-title">PolyChat</div>
      <button class="icon-btn ${MOCK_REQUESTS.length?'notif-active':''}" onclick="navigate('notifications')">🔔</button>
      <button class="icon-btn" onclick="navigate('settings')">⚙️</button>
    </div>
    ${hasChats?`<div class="stories-section">
      <div class="story-cell" onclick="toast('📷 Seleccionar foto para tu historia')">
        <div class="story-avatar empty"><span>Me</span><span class="add-badge">+</span></div>
        <div class="story-name">Tu historia</div>
      </div>
      ${MOCK_STORIES.map(s=>`<div class="story-cell" onclick="showStory('${s.id}')">
        <div class="story-avatar has-story" style="background:${s.color}">${s.avatar}</div>
        <div class="story-name">${s.userName}</div>
      </div>`).join('')}
    </div>`:''}
    <div class="messages-header"><h2>Mensajes</h2><p>Chats con traducción automática</p></div>
    <div class="chat-list">
      ${MOCK_CHATS.map(c=>`<div class="chat-item" onclick="openChat('${c.id}')">
        <div class="chat-avatar" style="background:${c.color}">${c.avatar}</div>
        <div class="chat-info">
          <div class="chat-name-row"><span class="chat-name">${c.name}</span><span class="chat-lang">${c.lang}</span></div>
          <div class="chat-preview">${c.lastMsg||'Sin mensajes'}</div>
        </div>
      </div>`).join('')}
    </div>
    <button class="fab" onclick="navigate('contacts')">💬 Nuevo chat</button>
  </div>`;
}

// RENDER: Chat
function renderChat(){
  const c = MOCK_CHATS.find(x=>x.id===state.currentChat);
  if(!c) return '<div class="screen active"><p>Chat no encontrado</p></div>';
  const myLang = 'ES';
  return `<div class="screen active chat-screen">
    <div class="chat-top-bar">
      <button class="back-btn" onclick="navigate('home','left')">←</button>
      <div class="chat-top-avatar" style="background:${c.color}">${c.avatar}</div>
      <div class="chat-top-info">
        <div class="chat-top-name">${c.name} <span class="chat-lang">${c.lang}</span></div>
        <div class="chat-top-status">${c.status}</div>
      </div>
      <button class="icon-btn">ℹ️</button>
    </div>
    <div class="messages-area" id="messagesArea" style="${getBgStyle()}">
      ${c.messages.map(m=>renderMessage(m,c)).join('')}
    </div>
    <div class="translation-hint">✨ Tú escribes en ${myLang} y el destinatario lo recibe en ${c.lang.substring(0,2).toUpperCase()}</div>
    <div class="input-bar">
      <button class="input-icon" onclick="toast('📎 Adjuntar archivo')">📎</button>
      <input class="input-field" id="msgInput" placeholder="Escribe en ${myLang}...">
      <span class="lang-badge-input">${myLang}</span>
      <button class="input-icon" onclick="toast('🕐 Programar mensaje')">🕐</button>
      <button class="input-icon" id="micBtn" onclick="toggleMic()">🎤</button>
      <button class="send-btn" onclick="sendMessage()">➤</button>
    </div>
  </div>`;
}
function renderMessage(m, c){
  const myId = state.currentUser ? state.currentUser.id : '';
  const isMe = m.senderId === myId;
  const key = c.id+'_'+m.id;
  const showOrig = state.showOriginal[key];
  // m.text = translated text (default to show), m.original = original text (optional toggle)
  const displayText = (showOrig && m.original) ? m.original : m.text;
  const wasTranslated = !isMe && m.original; // translation happened only if original is stored

  if(isMe){
    return `<div class="msg-row me">
      <div><div class="msg-bubble">
        ${m.isAudio?renderAudioPlayer(true):''}
        <div class="msg-text">${displayText}</div>
        <div class="msg-time">${m.time}</div>
      </div></div></div>`;
  }
  return `<div class="msg-row them">
    <div class="msg-avatar-sm" style="background:${c.color}">${c.avatar}</div>
    <div><div class="msg-bubble">
      ${wasTranslated && !showOrig ? `<div class="msg-translated">🌐 Traducido de ${m.srcLang}</div>` : ''}
      ${wasTranslated && showOrig  ? `<div class="msg-translated" style="opacity:.6">📝 Mensaje original (${m.srcLang})</div>` : ''}
      ${m.isAudio?renderAudioPlayer(false):''}
      <div class="msg-text">${displayText}</div>
      <div class="msg-time" style="display:flex;align-items:center;gap:8px">${m.time}
        ${wasTranslated?`<button class="msg-toggle" onclick="toggleOriginal('${c.id}','${m.id}')">${showOrig?'🌐 Ver traducción':'📝 Ver original'}</button>`:''}
      </div>
    </div></div></div>`;
}
function renderAudioPlayer(isMe){
  const bars=Array.from({length:16},()=>3+Math.floor(Math.random()*16));
  return `<div class="msg-audio"><span class="msg-audio-play">▶</span>
    <div class="msg-audio-bars">${bars.map(h=>`<div class="msg-audio-bar" style="height:${h}px"></div>`).join('')}</div>
    <span class="msg-audio-dur">0:30</span></div>`;
}
function getBgStyle(){
  const bgs=['#1e1e1e','radial-gradient(circle at 25% 25%,rgba(59,130,246,.2),transparent 50%),radial-gradient(circle at 75% 75%,rgba(59,130,246,.15),transparent 50%),#1a2a3a',
    'linear-gradient(135deg,rgba(139,92,246,.12) 25%,transparent 25%,transparent 50%,rgba(139,92,246,.12) 50%,rgba(139,92,246,.12) 75%,transparent 75%),#1a1a2a',
    'linear-gradient(rgba(74,222,128,.15) 1px,transparent 1px),linear-gradient(90deg,rgba(74,222,128,.15) 1px,transparent 1px),#0d1a0d',
    'linear-gradient(rgba(168,85,247,.15),transparent 50%,rgba(168,85,247,.15)),#1a0d2a'];
  return `background:${bgs[state.bgIndex]||bgs[0]};background-size:${state.bgIndex===3?'40px 40px':'auto'}`;
}

// RENDER: Contacts
function renderContacts(){
  return `<div class="screen active contacts-screen">
    <div class="screen-header">
      <button class="back-btn" onclick="navigate('home','left')">←</button>
      <h1>Seleccionar Amigo</h1>
      <button class="add-friend-btn" onclick="navigate('addContact')">+ Añadir</button>
    </div>
    <div class="screen-body">
      <p style="color:var(--fg-muted);font-size:13px;margin-bottom:12px">Tus amigos:</p>
      ${MOCK_CONTACTS.map(c=>`<div class="contact-row" onclick="startChat('${c.id}')">
        <div class="contact-avatar" style="background:${c.color}">${c.avatar}</div>
        <div class="contact-info"><div class="contact-name">${c.name}</div>
          <div class="contact-status ${c.online?'online':'offline'}">${c.online?'En línea':'Desconectado'}</div></div>
        <button class="small-btn primary">💬 Chatear</button>
      </div>`).join('')}
      ${MOCK_CONTACTS.length===0?'<div class="empty-state">Aún no tienes amigos. ¡Añade uno!</div>':''}
    </div>
  </div>`;
}

// RENDER: Add Contact
function renderAddContact(){
  return `<div class="screen active add-contact-screen">
    <div class="screen-header">
      <button class="back-btn" onclick="navigate('contacts','left')">←</button>
      <h1>Añadir Contacto</h1>
    </div>
    <div class="screen-body">
      <div class="search-bar">
        <input class="form-input" id="searchInput" placeholder="Buscar por nombre...">
        <button class="small-btn primary" onclick="searchContacts()">Buscar</button>
      </div>
      <p style="color:var(--fg-muted);font-size:13px;margin-bottom:12px">Sugerencias / Resultados:</p>
      <div id="searchResults"></div>
    </div>
  </div>`;
}

// RENDER: Notifications
function renderNotifications(){
  return `<div class="screen active notif-screen">
    <div class="screen-header">
      <button class="back-btn" onclick="navigate('home','left')">←</button>
      <h1>Notificaciones</h1>
    </div>
    <div class="screen-body">
      ${MOCK_REQUESTS.length===0?'<div class="empty-state">No tienes notificaciones nuevas.</div>':''}
      ${MOCK_REQUESTS.map(r=>`<div class="notif-row">
        <div class="contact-avatar" style="background:${r.senderColor}">${r.senderAvatar}</div>
        <div class="contact-info"><div class="contact-name">${r.senderName}</div>
          <div class="contact-status offline">Quiere ser tu amigo</div></div>
        <div class="notif-actions">
          <button class="small-btn success" onclick="acceptRequest('${r.id}')">Aceptar</button>
          <button class="small-btn danger" onclick="rejectRequest('${r.id}')">Rechazar</button>
        </div>
      </div>`).join('')}
    </div>
  </div>`;
}

// RENDER: Settings
function renderSettings(){
  const u = state.currentUser || (MOCK_USERS.length > 0 ? MOCK_USERS[0] : {name:'User',email:'',phone:'',age:0});
  const tab = state.settingsTab;
  return `<div class="screen active settings-screen">
    <div class="screen-header">
      <button class="back-btn" onclick="navigate('home','left')">←</button>
      <h1>⚙ Ajustes</h1>
    </div>
    <div class="settings-tabs">
      <button class="settings-tab ${tab==='profile'?'active':''}" onclick="state.settingsTab='profile';render()">👤 Perfil</button>
      <button class="settings-tab ${tab==='appearance'?'active':''}" onclick="state.settingsTab='appearance';render()">🎨 Apariencia</button>
    </div>
    <div class="settings-body">
      ${tab==='profile'?renderProfileTab(u):renderAppearanceTab()}
    </div>
  </div>`;
}
function renderProfileTab(u){
  return `<div class="settings-section" style="text-align:center">
    <div class="avatar-circle">${u.name.substring(0,2).toUpperCase()}</div>
  </div>
  <div class="settings-section">
    <div class="form-group"><div class="form-label">Nombre</div><input class="form-input" value="${u.name}"></div>
    <div class="form-group" style="margin-top:12px"><div class="form-label">Correo</div><input class="form-input" value="${u.email}"></div>
    <div class="form-group" style="margin-top:12px"><div class="form-label">Teléfono</div><input class="form-input" value="${u.phone}"></div>
    <div class="form-group" style="margin-top:12px"><div class="form-label">Idioma</div>
      <select class="form-select"><option>Español</option><option>English</option><option>Français</option><option>Deutsch</option></select></div>
    <div class="form-group" style="margin-top:12px"><div class="form-label">Edad</div><input class="form-input" value="${u.age}" type="number"></div>
  </div>
  <button class="btn-primary" onclick="toast('✓ Guardado correctamente')" style="margin-top:16px">Guardar cambios</button>
  <hr style="border:none;border-top:1px solid var(--border);margin:20px 0">
  <button class="btn-danger" onclick="state.currentUser=null;navigate('login','left')">Cerrar sesión</button>`;
}
function renderAppearanceTab(){
  const bgs=[{n:'Sin fondo',c:'#1e1e1e'},{n:'Burbujas',c:'#1a2a3a'},{n:'Diagonal',c:'#1a1a2a'},{n:'Cuadrícula',c:'#0d1a0d'},{n:'Ondas',c:'#1a0d2a'}];
  return `<div class="settings-section">
    <div class="settings-section-title">Tema de la aplicación</div>
    <div class="theme-row">
      <button class="theme-btn ${state.theme==='dark'?'active':''}" onclick="state.theme='dark';render()">🌙 Oscuro</button>
      <button class="theme-btn ${state.theme==='light'?'active':''}" onclick="state.theme='light';render()">☀ Claro</button>
    </div>
  </div>
  <div class="settings-section">
    <div class="settings-section-title">Modo Accesibilidad (Daltonismo)</div>
    <select class="form-select" style="width:100%" onchange="toast('Modo aplicado')">
      <option>Desactivado</option><option>Protanopia / Deuteranopia</option><option>Tritanopia</option><option>Monocromatismo</option></select>
  </div>
  <div class="settings-section">
    <div class="settings-section-title">Fondo del chat</div>
    <p style="font-size:12px;color:var(--fg-muted);margin-bottom:10px">Selecciona el patrón de fondo para la ventana de chat</p>
    <div class="bg-options">
      ${bgs.map((b,i)=>`<div class="bg-option" onclick="state.bgIndex=${i};render()">
        <div class="bg-preview ${state.bgIndex===i?'active':''}" style="background:${b.c}"></div>
        <div class="bg-option-name">${b.n}</div>
      </div>`).join('')}
    </div>
  </div>`;
}

// EVENT HANDLERS
async function handleLogin(){
  const phone = document.getElementById(state.isRegistering?'regPhone':'phoneInput');
  if(!phone||!phone.value.trim()){document.getElementById('loginError').textContent='⚠ Ingresa un número de teléfono válido';return}
  state.pendingPhone=phone.value.trim();
  // Show loading
  const errEl = document.getElementById('loginError');
  if(errEl) errEl.textContent='🔄 Buscando usuario...';
  const user = await loginByPhone(state.pendingPhone);
  if(!user || user.error){
    if(errEl) errEl.textContent='⚠ ' + (user?.error || 'Usuario no encontrado en la base de datos');
    return;
  }
  state.currentUser = user;
  navigate('verify');
}
async function handleVerify(){
  let code='';for(let i=0;i<6;i++){const el=document.getElementById('code'+i);code+=el?el.value:''}
  if(code.length<6){document.getElementById('verifyError').textContent='⚠ Completa los 6 dígitos';return}
  if(code===state.verifyCode){
    // Load all data from Neon DB
    document.getElementById('verifyError').textContent='🔄 Cargando datos...';
    await loadAllData(state.currentUser.id);
    navigate('home');
  }
  else{document.getElementById('verifyError').textContent='⚠ El código no coincide';document.querySelectorAll('.code-input').forEach(el=>{el.value='';el.classList.add('error')});document.getElementById('code0').focus()}
}
function autoFillCode(){
  for(let i=0;i<6;i++){const el=document.getElementById('code'+i);if(el)el.value=state.verifyCode[i]}
  toast('✓ Código reenviado con éxito');
}
function openChat(id){state.currentChat=id;navigate('chat');setTimeout(()=>{const a=document.getElementById('messagesArea');if(a)a.scrollTop=a.scrollHeight},100)}
function startChat(contactId){
  const existing=MOCK_CHATS.find(c=>c.participantId===contactId);
  if(existing){openChat(existing.id)}else{toast('💬 Chat creado');navigate('home','left')}
}
function sendMessage(){
  const input=document.getElementById('msgInput');if(!input||!input.value.trim())return;
  const c=MOCK_CHATS.find(x=>x.id===state.currentChat);if(!c)return;
  const myId = state.currentUser ? state.currentUser.id : '';
  const now=new Date();const time=now.getHours().toString().padStart(2,'0')+':'+now.getMinutes().toString().padStart(2,'0');
  c.messages.push({id:String(Date.now()),senderId:myId,text:input.value.trim(),original:null,srcLang:null,time,isAudio:false});
  c.lastMsg=input.value.trim();input.value='';render();
  setTimeout(()=>{const a=document.getElementById('messagesArea');if(a)a.scrollTop=a.scrollHeight},50);
}
function toggleOriginal(chatId,msgId){const key=chatId+'_'+msgId;state.showOriginal[key]=!state.showOriginal[key];render();setTimeout(()=>{const a=document.getElementById('messagesArea');if(a)a.scrollTop=a.scrollHeight},50)}
function toggleMic(){const btn=document.getElementById('micBtn');if(!btn)return;if(btn.classList.contains('recording')){btn.classList.remove('recording');btn.textContent='🎤';toast('✅ Nota de voz enviada')}else{btn.classList.add('recording');btn.textContent='⏹';toast('🎤 Grabando...')}}
function showStory(id){
  const s=MOCK_STORIES.find(x=>x.id===id);if(!s)return;
  const overlay=document.createElement('div');overlay.className='story-overlay';
  overlay.innerHTML=`<div class="story-progress"><div class="story-progress-fill" id="storyFill"></div></div>
    <div class="story-header"><div class="story-header-avatar" style="background:${s.color}">${s.avatar}</div>
      <div class="story-header-name">${s.userName}</div>
      <button class="story-close" onclick="this.closest('.story-overlay').remove()">✕</button></div>
    <div class="story-body"><div class="story-body-text">${s.content}</div></div>`;
  document.body.appendChild(overlay);
  requestAnimationFrame(()=>{const fill=document.getElementById('storyFill');if(fill)fill.style.width='100%'});
  setTimeout(()=>{if(document.body.contains(overlay))overlay.remove()},5500);
}
function acceptRequest(id){MOCK_REQUESTS.splice(MOCK_REQUESTS.findIndex(r=>r.id===id),1);toast('✓ Solicitud aceptada');render()}
function rejectRequest(id){MOCK_REQUESTS.splice(MOCK_REQUESTS.findIndex(r=>r.id===id),1);toast('Solicitud rechazada');render()}
function searchContacts(){
  const q=(document.getElementById('searchInput')?.value||'').toLowerCase();
  const myId = state.currentUser ? state.currentUser.id : '';
  const results=MOCK_USERS.filter(u=>u.id!==myId&&u.name.toLowerCase().includes(q));
  const container=document.getElementById('searchResults');if(!container)return;
  container.innerHTML=results.length===0?'<div class="empty-state">No se encontraron usuarios.</div>':
    results.map(u=>`<div class="contact-row"><div class="contact-avatar" style="background:${u.color}">${u.avatar}</div>
      <div class="contact-info"><div class="contact-name">${u.name} ${u.surnames}</div><div class="contact-status offline">${u.language}</div></div>
      <button class="small-btn primary" onclick="this.textContent='Añadido';this.disabled=true">Añadir</button></div>`).join('');
}

function attachEvents(){
  // Code input auto-advance
  document.querySelectorAll('.code-input').forEach((el,i)=>{
    el.addEventListener('input',()=>{if(el.value.length===1&&i<5)document.getElementById('code'+(i+1))?.focus()});
    el.addEventListener('keydown',e=>{if(e.key==='Backspace'&&!el.value&&i>0)document.getElementById('code'+(i-1))?.focus()});
  });
  // Enter key for message
  const msgInput=document.getElementById('msgInput');
  if(msgInput)msgInput.addEventListener('keydown',e=>{if(e.key==='Enter')sendMessage()});
  // Auto-fill verify after 1s
  if(state.currentScreen==='verify')setTimeout(autoFillCode,1000);
}

// Initialize
document.addEventListener('DOMContentLoaded',()=>render());
