package org.example.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class AlterStoriesTables {
    public static void main(String[] args) {
        System.out.println("Attempting to connect to PostgreSQL database...");
        try (Connection conn = DatabaseManager.getConnection()) {
            System.out.println("Successfully connected to the PostgreSQL database!");
            try (Statement stmt = conn.createStatement()) {
                String bloqueosTable = "CREATE TABLE IF NOT EXISTS public.bloqueos (" +
                        "id_bloqueador UUID REFERENCES public.usuarios(id_usuario) ON DELETE CASCADE, " +
                        "id_bloqueado UUID REFERENCES public.usuarios(id_usuario) ON DELETE CASCADE, " +
                        "PRIMARY KEY (id_bloqueador, id_bloqueado)" +
                        ")";
                stmt.execute(bloqueosTable);
                System.out.println("Table public.bloqueos created or already exists!");

                String restriccionesTable = "CREATE TABLE IF NOT EXISTS public.restricciones_historias (" +
                        "id_usuario UUID REFERENCES public.usuarios(id_usuario) ON DELETE CASCADE, " +
                        "id_restringido UUID REFERENCES public.usuarios(id_usuario) ON DELETE CASCADE, " +
                        "PRIMARY KEY (id_usuario, id_restringido)" +
                        ")";
                stmt.execute(restriccionesTable);
                System.out.println("Table public.restricciones_historias created or already exists!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
