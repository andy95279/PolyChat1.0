package org.example.db;

import org.example.model.Contact;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ContactDao {

    public static List<Contact> getContactsForUser(String userId) {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nombre, u.apellidos, u.nacionalidad, u.ultima_conexion " +
                     "FROM usuarios u " +
                     "JOIN contactos c ON u.id_usuario = c.id_contacto " +
                     "WHERE c.id_usuario = ?::uuid";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                contacts.add(mapRsToContact(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contacts;
    }

    public static List<Contact> searchUsers(String query, String excludeUserId) {
        List<Contact> users = new ArrayList<>();
        String sql = "SELECT id_usuario, nombre, apellidos, nacionalidad, ultima_conexion " +
                     "FROM usuarios " +
                     "WHERE (nombre ILIKE ? OR apellidos ILIKE ?) " +
                     "AND id_usuario != ?::uuid " +
                     "LIMIT 15";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String q = "%" + query + "%";
            pstmt.setString(1, q);
            pstmt.setString(2, q);
            pstmt.setString(3, excludeUserId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(mapRsToContact(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static void addContact(String userId, String contactId) {
        String sql = "INSERT INTO contactos (id_usuario, id_contacto) VALUES (?::uuid, ?::uuid) ON CONFLICT DO NOTHING";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Direction: User -> Contact
            pstmt.setString(1, userId);
            pstmt.setString(2, contactId);
            pstmt.executeUpdate();

            // Direction: Contact -> User (Mutual Friendship)
            pstmt.setString(1, contactId);
            pstmt.setString(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Contact mapRsToContact(ResultSet rs) throws SQLException {
        String id = rs.getString("id_usuario");
        String name = rs.getString("nombre");
        String apellidos = rs.getString("apellidos");
        String language = rs.getString("nacionalidad");
        if (language == null) language = "EN";
        
        Timestamp lastConn = rs.getTimestamp("ultima_conexion");
        boolean isOnline = false;
        if (lastConn != null) {
            isOnline = lastConn.toLocalDateTime().plusMinutes(5).isAfter(LocalDateTime.now());
        }

        String initials = "";
        if (name != null && !name.isEmpty()) initials += name.charAt(0);
        if (apellidos != null && !apellidos.isEmpty()) initials += apellidos.charAt(0);

        return new Contact(id, name + " " + apellidos, language, isOnline, initials, "#3B82F6");
    }
}
