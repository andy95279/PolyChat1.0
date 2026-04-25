package org.example.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDao {

    public static void initializeSettingsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS public.configuracion_usuario (" +
                     "id_usuario UUID PRIMARY KEY, " +
                     "tema VARCHAR(20) DEFAULT 'dark', " +
                     "fondo_chat INT DEFAULT 0, " +
                     "modo_daltonismo VARCHAR(20) DEFAULT 'none'" +
                     ")";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadSettings(String userId, org.example.provider.SettingsProvider provider) {
        String sql = "SELECT tema, fondo_chat, modo_daltonismo FROM public.configuracion_usuario WHERE id_usuario = ?::uuid";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                provider.setTheme(rs.getString("tema"));
                provider.setChatBackgroundIndex(rs.getInt("fondo_chat"));
                provider.setColorblindMode(rs.getString("modo_daltonismo"));
            } else {
                // Si no tiene configuracion, la creamos vacia para este usuario
                saveSettings(userId, "dark", 0, "none");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveSettings(String userId, String theme, int bgIndex, String cbMode) {
        String sql = "INSERT INTO public.configuracion_usuario (id_usuario, tema, fondo_chat, modo_daltonismo) " +
                     "VALUES (?::uuid, ?, ?, ?) " +
                     "ON CONFLICT(id_usuario) DO UPDATE SET " +
                     "tema = excluded.tema, fondo_chat = excluded.fondo_chat, modo_daltonismo = excluded.modo_daltonismo";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, theme);
            pstmt.setInt(3, bgIndex);
            pstmt.setString(4, cbMode);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
