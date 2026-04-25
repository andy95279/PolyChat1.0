package org.example.db;

import org.example.model.Story;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StoryDao {

    public static void initializeStoriesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS public.historias (" +
                     "id_historia SERIAL PRIMARY KEY, " +
                     "id_usuario UUID NOT NULL, " +
                     "contenido TEXT, " +
                     "imagen_url TEXT, " +
                     "fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW()" +
                     ")";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Story> getStoriesForUser(String userId) {
        List<Story> stories = new ArrayList<>();
        // Fetch stories from the user's contacts and their own (limit to last 24h)
        String sql = "SELECT DISTINCT h.id_historia, h.id_usuario, u.nombre, u.apellidos, h.contenido, h.imagen_url, h.fecha_creacion " +
                     "FROM public.historias h " +
                     "JOIN public.usuarios u ON h.id_usuario = u.id_usuario " +
                     "WHERE h.fecha_creacion > ? " +
                     "AND (" +
                     "  h.id_usuario = ?::uuid " +
                     "  OR (" +
                     "    h.id_usuario IN (SELECT id_contacto FROM public.contactos WHERE id_usuario = ?::uuid) " +
                     "    AND h.id_usuario NOT IN (SELECT id_bloqueador FROM public.bloqueos WHERE id_bloqueado = ?::uuid) " +
                     "    AND h.id_usuario NOT IN (SELECT id_bloqueado FROM public.bloqueos WHERE id_bloqueador = ?::uuid) " +
                     "    AND h.id_usuario NOT IN (SELECT id_usuario FROM public.restricciones_historias WHERE id_restringido = ?::uuid)" +
                     "  )" +
                     ") " +
                     "ORDER BY h.fecha_creacion DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            Timestamp oneDayAgo = Timestamp.valueOf(LocalDateTime.now().minusHours(24));
            pstmt.setTimestamp(1, oneDayAgo);
            pstmt.setString(2, userId);
            pstmt.setString(3, userId);
            pstmt.setString(4, userId);
            pstmt.setString(5, userId);
            pstmt.setString(6, userId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id = String.valueOf(rs.getInt("id_historia"));
                String authorId = rs.getString("id_usuario");
                String name = rs.getString("nombre");
                String surnames = rs.getString("apellidos");
                String userName = (name != null ? name : "") + " " + (surnames != null ? surnames : "");
                
                String content = rs.getString("contenido");
                String imageUrl = rs.getString("imagen_url");
                LocalDateTime timestamp = rs.getTimestamp("fecha_creacion").toLocalDateTime();
                
                String initials = "";
                String[] parts = userName.split(" ");
                if (parts.length > 0 && !parts[0].isEmpty()) initials += parts[0].charAt(0);
                if (parts.length > 1 && !parts[1].isEmpty()) initials += parts[1].charAt(0);

                // Use default color or hash user id for color
                String color = "#3B82F6"; 
                
                Story story = new Story(id, authorId, userName.trim(), initials, color, content, imageUrl, timestamp);
                stories.add(story);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stories;
    }

    public static void saveStory(String userId, String content, String imageUrl) {
        String sql = "INSERT INTO public.historias (id_usuario, contenido, imagen_url, fecha_creacion) VALUES (?::uuid, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, content);
            pstmt.setString(3, imageUrl);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteStory(String storyId, String userId) {
        String sql = "DELETE FROM public.historias WHERE id_historia = ? AND id_usuario = ?::uuid";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(storyId));
            pstmt.setString(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
