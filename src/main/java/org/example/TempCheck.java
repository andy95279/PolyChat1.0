package org.example;

import org.example.db.DatabaseManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TempCheck {
    public static void main(String[] args) {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id_usuario, nombre, nacionalidad FROM usuarios");
            while (rs.next()) {
                System.out.println(rs.getString("nombre") + " -> " + rs.getString("nacionalidad"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
