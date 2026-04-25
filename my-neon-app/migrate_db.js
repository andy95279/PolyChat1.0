import pkg from 'pg';
const { Client } = pkg;

const client = new Client({
  connectionString: 'postgresql://neondb_owner:npg_H6cDK9LrgMpI@ep-old-paper-almcvnjx-pooler.c-3.eu-central-1.aws.neon.tech/neondb?sslmode=require'
});

async function migrate() {
  try {
    await client.connect();
    console.log("Connected to DB.");

    await client.query("BEGIN");

    // Rename old tables if they exist
    const tablesToRename = ['mensajes', 'chat_miembro', 'chat', 'usuarios', 'contactos'];
    for (const table of tablesToRename) {
      try {
        await client.query(`ALTER TABLE ${table} RENAME TO ${table}_old_${Date.now()}`);
        console.log(`Renamed ${table} to old version.`);
      } catch (e) {
        console.log(`Table ${table} might not exist or already renamed: ${e.message}`);
      }
    }

    // Create new tables with UUID
    const createSql = `
      CREATE TABLE usuarios (
          id_usuario UUID PRIMARY KEY,
          tlf VARCHAR(20),
          nombre VARCHAR(50),
          apellidos VARCHAR(100),
          nacionalidad VARCHAR(50),
          fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          ultima_conexion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );

      CREATE TABLE chat (
          id_chat SERIAL PRIMARY KEY,
          creado_por UUID REFERENCES usuarios(id_usuario),
          fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );

      CREATE TABLE chat_miembro (
          id_chat INT REFERENCES chat(id_chat) ON DELETE CASCADE,
          id_user UUID REFERENCES usuarios(id_usuario),
          fecha_union TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          PRIMARY KEY (id_chat, id_user)
      );

      CREATE TABLE mensajes (
          id_mensaje SERIAL PRIMARY KEY,
          id_chat INT REFERENCES chat(id_chat) ON DELETE CASCADE,
          id_usuario UUID REFERENCES usuarios(id_usuario),
          texto TEXT,
          fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          leido BOOLEAN DEFAULT FALSE
      );
      
      CREATE TABLE contactos (
          id_usuario UUID REFERENCES usuarios(id_usuario),
          id_contacto UUID REFERENCES usuarios(id_usuario),
          fecha_agregado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          PRIMARY KEY (id_usuario, id_contacto)
      );
    `;

    await client.query(createSql);
    console.log("Created new tables with UUID support.");

    await client.query("COMMIT");
    console.log("Migration committed successfully.");
  } catch (err) {
    await client.query("ROLLBACK");
    console.error("Migration failed:", err);
  } finally {
    await client.end();
  }
}

migrate();
