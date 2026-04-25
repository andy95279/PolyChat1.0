package org.example.db;

import org.example.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class UserDao {

    public static User getUserById(String id) {
        String sql = "SELECT * FROM public.usuarios WHERE id_usuario = ?::uuid";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User getUserByPhone(String phone) {
        String sql = "SELECT * FROM public.usuarios WHERE tlf = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateUser(User user) {
        String sql = "UPDATE public.usuarios SET nombre = ?, apellidos = ?, tlf = ?, email = ?, edad = ?, nacionalidad = ?, ultima_conexion = ? " +
                     "WHERE id_usuario = ?::uuid";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getSurnames());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getEmail());
            pstmt.setInt(5, user.getAge());
            pstmt.setString(6, user.getLanguage()); // nacionalidad
            pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(8, user.getId());
            pstmt.executeUpdate();
            System.out.println("User updated in DB: " + user.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static User mapUser(ResultSet rs) throws SQLException {
        String name = rs.getString("nombre");
        String surnames = rs.getString("apellidos");
        if (name == null) name = "";
        if (surnames == null) surnames = "";
        return new User(
                rs.getString("id_usuario"),
                name,
                surnames,
                rs.getString("email"),
                rs.getString("tlf"),
                rs.getString("nacionalidad") != null ? rs.getString("nacionalidad") : "Español",
                rs.getInt("edad")
        );
    }

    public static void saveUser(User user) {
        // App schema in public.usuarios
        String sqlApp = "INSERT INTO public.usuarios (id_usuario, tlf, nombre, apellidos, email, edad, nacionalidad, fecha_registro, ultima_conexion) " +
                        "VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON CONFLICT(id_usuario) DO UPDATE SET tlf = excluded.tlf, nombre = excluded.nombre, " +
                        "apellidos = excluded.apellidos, email = excluded.email, edad = excluded.edad, " +
                        "nacionalidad = excluded.nacionalidad, ultima_conexion = excluded.ultima_conexion";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement appPstmt = conn.prepareStatement(sqlApp)) {
            
            appPstmt.setString(1, user.getId());
            appPstmt.setString(2, user.getPhone());
            appPstmt.setString(3, user.getName());
            appPstmt.setString(4, user.getSurnames());
            appPstmt.setString(5, user.getEmail());
            appPstmt.setInt(6, user.getAge());
            appPstmt.setString(7, user.getLanguage()); // nacionalidad
            appPstmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            appPstmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            appPstmt.executeUpdate();
            
            System.out.println("User saved to public.usuarios!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
