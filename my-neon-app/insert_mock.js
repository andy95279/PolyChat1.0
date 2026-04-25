import pg from 'pg';
const client = new pg.Client({ connectionString: 'postgresql://neondb_owner:npg_H6cDK9LrgMpI@ep-old-paper-almcvnjx-pooler.c-3.eu-central-1.aws.neon.tech/neondb?sslmode=require' });

async function insertMockUser() {
  await client.connect();
  try {
    const defaultUuid = '11111111-1111-1111-1111-111111111111';
    
    // Insert into neon_auth (if needed, though saving mostly matters for the app right now)
    await client.query(`
      INSERT INTO neon_auth."user" (id, name, email, "emailVerified", "createdAt", "updatedAt") 
      VALUES ($1, 'Test User', 'test@example.com', false, now(), now())
      ON CONFLICT DO NOTHING
    `, [defaultUuid]);
    
    // Insert into public.usuarios
    await client.query(`
      INSERT INTO usuarios (id_usuario, nombre, apellidos, tlf, nacionalidad) 
      VALUES ($1, 'Test', 'User', '123456789', 'EN')
      ON CONFLICT DO NOTHING
    `, [defaultUuid]);

    console.log("Mock user inserted.");
  } catch(e) {
    console.error(e);
  } finally {
    await client.end();
  }
}
insertMockUser();
