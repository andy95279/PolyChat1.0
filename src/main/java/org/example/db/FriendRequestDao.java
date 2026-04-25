package org.example.db;

import org.example.model.FriendRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestDao {

    public static void sendRequest(String senderId, String receiverId) {
        String sql = "INSERT INTO solicitudes_amistad (id_remitente, id_destinatario, estado) VALUES (?::uuid, ?::uuid, 'PENDIENTE') ON CONFLICT DO NOTHING";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, senderId);
            pstmt.setString(2, receiverId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<FriendRequest> getPendingRequestsForUser(String receiverId) {
        List<FriendRequest> requests = new ArrayList<>();
        String sql = "SELECT s.id_solicitud, s.id_remitente, s.id_destinatario, s.estado, s.fecha_envio, " +
                     "u.nombre, u.apellidos " +
                     "FROM solicitudes_amistad s " +
                     "JOIN usuarios u ON s.id_remitente = u.id_usuario " +
                     "WHERE s.id_destinatario = ?::uuid AND s.estado = 'PENDIENTE'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, receiverId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id_solicitud");
                String senderId = rs.getString("id_remitente");
                String destId = rs.getString("id_destinatario");
                String status = rs.getString("estado");
                Timestamp timestamp = rs.getTimestamp("fecha_envio");
                
                String name = rs.getString("nombre");
                String apellidos = rs.getString("apellidos");
                String initials = "";
                if (name != null && !name.isEmpty()) initials += name.substring(0, 1).toUpperCase();
                if (apellidos != null && !apellidos.isEmpty()) initials += apellidos.substring(0, 1).toUpperCase();
                
                String fullName = (name != null ? name : "") + " " + (apellidos != null ? apellidos : "");
                fullName = fullName.trim();

                requests.add(new FriendRequest(id, senderId, destId, status, 
                    timestamp != null ? timestamp.toLocalDateTime() : null, 
                    fullName, "#3B82F6", initials));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public static void acceptRequest(String requestId, String senderId, String receiverId) {
        String updateSql = "UPDATE solicitudes_amistad SET estado = 'ACEPTADA' WHERE id_solicitud = ?";
        String insertContactSql1 = "INSERT INTO contactos (id_usuario, id_contacto) VALUES (?::uuid, ?::uuid) ON CONFLICT DO NOTHING";
        String insertContactSql2 = "INSERT INTO contactos (id_usuario, id_contacto) VALUES (?::uuid, ?::uuid) ON CONFLICT DO NOTHING";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt1 = conn.prepareStatement(updateSql);
                 PreparedStatement pstmt2 = conn.prepareStatement(insertContactSql1);
                 PreparedStatement pstmt3 = conn.prepareStatement(insertContactSql2)) {
                
                pstmt1.setInt(1, Integer.parseInt(requestId));
                pstmt1.executeUpdate();

                // Sender adds receiver
                pstmt2.setString(1, senderId);
                pstmt2.setString(2, receiverId);
                pstmt2.executeUpdate();

                // Receiver adds sender
                pstmt3.setString(1, receiverId);
                pstmt3.setString(2, senderId);
                pstmt3.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void rejectRequest(String requestId) {
        String sql = "UPDATE solicitudes_amistad SET estado = 'RECHAZADA' WHERE id_solicitud = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(requestId));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
