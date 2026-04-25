package org.example.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class AlterMensajesTable {
    public static void main(String[] args) {
        System.out.println("Attempting to connect to PostgreSQL database...");
        try (Connection conn = DatabaseManager.getConnection()) {
            System.out.println("Successfully connected to the PostgreSQL database!");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE public.mensajes ADD COLUMN IF NOT EXISTS adjunto_url TEXT");
                System.out.println("Column adjunto_url added to public.mensajes!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
