package org.example.db;

import org.example.model.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDao {

    public static List<Message> getMessagesForChat(int chatId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id_mensaje, id_usuario, texto_original, idioma_origen, es_audio, duracion_audio, adjunto_url, fecha_envio " +
                     "FROM public.mensajes WHERE id_chat = ? ORDER BY fecha_envio ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, chatId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id = String.valueOf(rs.getInt("id_mensaje"));
                String senderId = rs.getString("id_usuario");
                String content = rs.getString("texto_original");
                String sourceLang = rs.getString("idioma_origen") != null ? rs.getString("idioma_origen") : "ES";
                boolean isAudio = rs.getBoolean("es_audio");
                String duration = rs.getString("duracion_audio");
                String attachmentUrl = rs.getString("adjunto_url");
                Timestamp ts = rs.getTimestamp("fecha_envio");
                
                Message msg = new Message(id, senderId, null, content, sourceLang, ts.toLocalDateTime().toLocalTime(), isAudio, duration, attachmentUrl);
                messages.add(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Returns only messages with id_mensaje strictly greater than {@code lastMessageId}.
     * Used by the ChatScreen polling loop so we don't reload the full history each tick.
     */
    public static List<Message> getMessagesAfter(int chatId, int lastMessageId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id_mensaje, id_usuario, texto_original, idioma_origen, es_audio, duracion_audio, adjunto_url, fecha_envio " +
                     "FROM public.mensajes WHERE id_chat = ? AND id_mensaje > ? ORDER BY fecha_envio ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, chatId);
            pstmt.setInt(2, lastMessageId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id       = String.valueOf(rs.getInt("id_mensaje"));
                String sender   = rs.getString("id_usuario");
                String content  = rs.getString("texto_original");
                String srcLang  = rs.getString("idioma_origen") != null ? rs.getString("idioma_origen") : "ES";
                boolean isAudio = rs.getBoolean("es_audio");
                String duration = rs.getString("duracion_audio");
                String attachmentUrl = rs.getString("adjunto_url");
                Timestamp ts    = rs.getTimestamp("fecha_envio");
                messages.add(new Message(id, sender, null, content, srcLang,
                        ts.toLocalDateTime().toLocalTime(), isAudio, duration, attachmentUrl));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Saves a message and returns the DB-assigned id_mensaje, or -1 on failure.
     * Using RETURNING ensures the poller knows this id is already in memory.
     */
    public static int saveMessage(Message message, int chatId) {
        String sql = "INSERT INTO public.mensajes (id_chat, id_usuario, texto_original, idioma_origen, es_audio, duracion_audio, adjunto_url, fecha_envio) " +
                     "VALUES (?, ?::uuid, ?, ?, ?, ?, ?, ?) RETURNING id_mensaje";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, chatId);
            pstmt.setString(2, message.getSenderId());
            pstmt.setString(3, message.getOriginalText() != null ? message.getOriginalText() : message.getText());
            pstmt.setString(4, message.getSourceLanguage() != null ? message.getSourceLanguage() : "ES");
            pstmt.setBoolean(5, message.isAudio());
            pstmt.setString(6, message.getAudioDuration());
            pstmt.setString(7, message.getAttachmentUrl());

            LocalDateTime dt = LocalDateTime.now().with(message.getTimestamp());
            pstmt.setTimestamp(8, Timestamp.valueOf(dt));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
