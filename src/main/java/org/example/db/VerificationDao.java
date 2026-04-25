package org.example.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;


public class VerificationDao {

    public static void saveCode(String phone, String code, String message) {
        // Table: codigos_verificacion (tlf, codigo, mensaje, fecha_expiracion)
        String sql = "INSERT INTO public.codigos_verificacion (tlf, codigo, mensaje, fecha_expiracion) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON CONFLICT (tlf) DO UPDATE SET codigo = excluded.codigo, mensaje = excluded.mensaje, fecha_expiracion = excluded.fecha_expiracion";
        
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            pstmt.setString(2, code);
            pstmt.setString(3, message);
            pstmt.setTimestamp(4, Timestamp.valueOf(expiry));
            pstmt.executeUpdate();
            
            // Also save to the new 'verificacion' table for historical tracking
            String sqlHistory = "INSERT INTO public.verificacion (tlf, codigo, fecha_envio, fecha_caducidad) VALUES (?, ?, ?, ?)";
            try (PreparedStatement hPstmt = conn.prepareStatement(sqlHistory)) {
                hPstmt.setString(1, phone);
                hPstmt.setString(2, code);
                hPstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                hPstmt.setTimestamp(4, Timestamp.valueOf(expiry));
                hPstmt.executeUpdate();
            }
            
            System.out.println("✅ Code saved to DB for " + phone);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkCode(String phone, String code) {
        // 1. Check the main table (codigos_verificacion)
        String sql = "SELECT * FROM public.codigos_verificacion WHERE tlf = ? AND codigo = ? AND fecha_expiracion > ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            pstmt.setString(2, code);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("✅ Code verified via codigos_verificacion table.");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 2. Fallback: check the 'verificacion' history table (most recent entry for this phone)
        String sqlVerif = "SELECT codigo FROM public.verificacion WHERE tlf = ? AND fecha_caducidad > ? ORDER BY fecha_envio DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlVerif)) {
            pstmt.setString(1, phone);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedCode = rs.getString("codigo");
                if (storedCode != null && storedCode.equals(code)) {
                    System.out.println("✅ Code verified via verificacion table.");
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("❌ Code verification failed for phone: " + phone);
        return false;
    }

    /**
     * Retrieves the latest valid verification code sent to the given phone number
     * from the 'verificacion' table. Returns null if no valid code is found.
     */
    public static String getLatestCode(String phone) {
        String sql = "SELECT codigo FROM public.verificacion WHERE tlf = ? AND fecha_caducidad > ? ORDER BY fecha_envio DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String code = rs.getString("codigo");
                System.out.println("📋 Latest valid code from DB for " + phone + ": " + code);
                return code;
            } else {
                System.out.println("⚠ No valid code found in 'verificacion' for phone: " + phone);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveSmsLog(String smsId, String phone, String code, String message) {
        String sql = "INSERT INTO public.sms_temporal (id_sms, tlf, codigo, mensaje, fecha_envio) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, smsId);
            pstmt.setString(2, phone);
            pstmt.setString(3, code);
            pstmt.setString(4, message);
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
            System.out.println("🗒️ SMS log saved in DB. Provider ID: " + smsId);
        } catch (SQLException e) {
            System.err.println("❌ Failed to save SMS log: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
