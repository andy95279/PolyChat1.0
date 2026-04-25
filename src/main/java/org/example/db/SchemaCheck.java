package org.example.db;

import java.sql.Connection;
import java.sql.ResultSet;

public class SchemaCheck {
    public static void main(String[] args) {
        DatabaseManager.connect();
        try (Connection conn = DatabaseManager.getConnection()) {
            String[] tables = {"usuarios", "chat", "mensajes", "chat_miembro", "contactos"};
            
            for (String table : tables) {
                System.out.println("--- Table: " + table + " ---");
                ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) AS total FROM " + table);
                if (rs.next()) {
                    System.out.println("Rows: " + rs.getInt("total"));
                }
                System.out.println("-------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.disconnect();
        }
    }
}
