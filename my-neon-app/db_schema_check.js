import pkg from 'pg';
import fs from 'fs';
const { Client } = pkg;

const client = new Client({
  connectionString: 'postgresql://neondb_owner:npg_H6cDK9LrgMpI@ep-old-paper-almcvnjx-pooler.c-3.eu-central-1.aws.neon.tech/neondb?sslmode=require'
});

async function run() {
  let out = "";
  try {
    await client.connect();
    
    // Check schemas
    out += "=== SCHEMAS ===\n";
    const schemas = await client.query("SELECT schema_name FROM information_schema.schemata");
    out += schemas.rows.map(r => r.schema_name).join(", ") + "\n";
    
    // Check tables in neon_auth
    out += "\n=== TABLES IN public ===\n";
    const publicTables = await client.query("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'");
    out += publicTables.rows.map(r => r.table_name).join(", ") + "\n";
    
    out += "\n=== TABLES IN neon_auth ===\n";
    const tables = await client.query("SELECT table_name FROM information_schema.tables WHERE table_schema = 'neon_auth'");
    out += tables.rows.map(r => r.table_name).join(", ") + "\n";
    
    for (let row of tables.rows) {
        out += `\n=== COLUMNS IN neon_auth.${row.table_name} ===\n`;
        const columns = await client.query(`SELECT column_name, data_type FROM information_schema.columns WHERE table_schema = 'neon_auth' AND table_name = '${row.table_name}'`);
        out += columns.rows.map(c => `${c.column_name}: ${c.data_type}`).join("\n") + "\n";
    }
    
    fs.writeFileSync('db_out.txt', out);
    console.log("Written to db_out.txt");
  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

run();
