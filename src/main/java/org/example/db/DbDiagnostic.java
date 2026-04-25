package org.example.db;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.ResultSetMetaData;

public class DbDiagnostic {
    public static void main(String[] args) {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM usuarios LIMIT 0");
            ResultSetMetaData rsmd = rs.getMetaData();
            System.out.println("=== SCHEMAS ===");
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                System.out.println(rsmd.getColumnName(i) + " : " + rsmd.getColumnTypeName(i) + " (" + rsmd.isNullable(i) + ")");
            }
            // And try to insert a test user
            String sql = "INSERT INTO usuarios (id_usuario, tlf, nombre, apellidos, nacionalidad, fecha_registro, ultima_conexion) " +
             "VALUES ('11111111-2222-3333-4444-555555555555'::uuid, '+34999888777', 'Test', 'User', 'EN', NOW(), NOW())";
            stmt.executeUpdate(sql);
            System.out.println("Test insert successful.");
            stmt.executeUpdate("DELETE FROM usuarios WHERE tlf = '+34999888777'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
