package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CheckDb {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://ep-old-paper-almcvnjx-pooler.c-3.eu-central-1.aws.neon.tech/neondb";
        String user = "neondb_owner";
        String password = "npg_H6cDK9LrgMpI";
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                System.out.println("Tables in public schema:");
                while (rs.next()) {
                    System.out.println("- " + rs.getString("table_name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
