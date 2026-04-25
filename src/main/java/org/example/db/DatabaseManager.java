package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    // The provided Neon PostgreSQL database URL
    private static final String URL = "jdbc:postgresql://ep-old-paper-almcvnjx-pooler.c-3.eu-central-1.aws.neon.tech/neondb?sslmode=require";
    private static final String USER = "neondb_owner";
    private static final String PASSWORD = "npg_H6cDK9LrgMpI";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL Driver not found!");
            e.printStackTrace();
        }
    }

    private DatabaseManager() {
    }

    /**
     * Returns a new connection to the database.
     * The caller is responsible for closing the connection (e.g. using try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Deprecated helpers for compatibility
    public static void connect() {
        // No longer needed as getConnection() handles it
    }

    public static void disconnect() {
        // No longer needed
    }
}
