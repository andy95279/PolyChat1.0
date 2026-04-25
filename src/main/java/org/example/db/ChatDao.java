package org.example.db;

import org.example.model.Chat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ChatDao {

    public static List<Chat> getChatsForUser(String userId) {
        List<Chat> chats = new ArrayList<>();
        String sql = "SELECT c.id_chat, c.fecha_creacion, u.id_usuario, u.nombre, u.apellidos, u.nacionalidad " +
                     "FROM public.chat c " +
                     "JOIN public.chat_miembro cm ON c.id_chat = cm.id_chat " +
                     "JOIN public.usuarios u ON cm.id_user = u.id_usuario " +
                     "WHERE c.id_chat IN (SELECT id_chat FROM public.chat_miembro WHERE id_user = ?::uuid) " +
                     "AND u.id_usuario != ?::uuid";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int chatId = rs.getInt("id_chat");
                String nameUser = rs.getString("nombre");
                String surnamesUser = rs.getString("apellidos");
                String partName = (nameUser != null ? nameUser : "") + " " + (surnamesUser != null ? surnamesUser : "");
                
                String language = rs.getString("nacionalidad"); // For UI 🇮🇹 IT, 🇬🇧 EN, etc.
                if (language == null) language = "EN";
                
                String status = "Desconectado";
                // Note: Assuming logic for status remains based on existing schema
                
                String initials = "";
                String[] parts = partName.split(" ");
                if (parts.length > 0 && !parts[0].isEmpty()) initials += parts[0].charAt(0);
                if (parts.length > 1 && !parts[1].isEmpty()) initials += parts[1].charAt(0);
                
                String color = "#3B82F6"; // Default color

                Chat chat = new Chat(String.valueOf(chatId), partName.trim(), language, status, initials, color);
                chats.add(chat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chats;
    }

    public static int createChat(String user1Id, String user2Id) {
        int chatId = -1;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            String insertChat = "INSERT INTO public.chat (creado_por) VALUES (?::uuid)";
            try (PreparedStatement cStmt = conn.prepareStatement(insertChat, Statement.RETURN_GENERATED_KEYS)) {
                cStmt.setString(1, user1Id);
                cStmt.executeUpdate();
                ResultSet rs = cStmt.getGeneratedKeys();
                if (rs.next()) {
                    chatId = rs.getInt(1);
                }
            }

            if (chatId != -1) {
                String insertMember = "INSERT INTO public.chat_miembro (id_chat, id_user) VALUES (?, ?::uuid)";
                try (PreparedStatement mStmt = conn.prepareStatement(insertMember)) {
                    mStmt.setInt(1, chatId);
                    mStmt.setString(2, user1Id);
                    mStmt.addBatch();
                    mStmt.setInt(1, chatId);
                    mStmt.setString(2, user2Id);
                    mStmt.addBatch();
                    mStmt.executeBatch();
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chatId;
    }

    public static int getChatBetween(String userId, String contactId) {
        String sql = "SELECT id_chat FROM public.chat_miembro " +
                     "WHERE id_user = ?::uuid " +
                     "AND id_chat IN (SELECT id_chat FROM public.chat_miembro WHERE id_user = ?::uuid)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, contactId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_chat");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
