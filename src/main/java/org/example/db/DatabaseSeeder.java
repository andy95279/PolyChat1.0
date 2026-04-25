package org.example.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class DatabaseSeeder {

    public static void seedIfNeeded() {
        try (Connection conn = DatabaseManager.getConnection()) {
            boolean isEmpty = false;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count(*) FROM usuarios")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    isEmpty = true;
                }
            }
            if (isEmpty) {
                System.out.println("Database is empty. Seeding data...");
                seedData(conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void seedData(Connection conn) throws Exception {
        conn.setAutoCommit(false);

        // 1. Insert Users
        String insertUser = "INSERT INTO usuarios (id_usuario, tlf, nombre, apellidos, email, edad, nacionalidad, fecha_registro, ultima_conexion) VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement uStmt = conn.prepareStatement(insertUser)) {
            Object[][] users = {
                {"11111111-1111-1111-1111-111111111111", "+34 600 000 000", "Albert", "Doe", "albert@polychat.app", 28, "ES", Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now())},
                {"22222222-2222-2222-2222-222222222222", "+44 700 000 000", "Emma", "Johnson", "emma@example.com", 24, "EN", Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now())},
                {"33333333-3333-3333-3333-333333333333", "+33 600 000 000", "Marie", "Dubois", "marie@chat.fr", 31, "FR", Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().minusMinutes(5))},
                {"44444444-4444-4444-4444-444444444444", "+49 150 0000000", "Klaus", "Müller", "klaus@web.de", 45, "DE", Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().minusDays(1))},
                {"55555555-5555-5555-5555-555555555555", "+39 300 0000000", "Sofia", "Rossi", "sofia@rossi.it", 22, "IT", Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now())},
                {"66666666-6666-6666-6666-666666666666", "+81 90 0000 0000", "Yuki", "Tanaka", "yuki@tokyo.jp", 29, "JP", Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().minusHours(1))}
            };
            for (Object[] u : users) {
                uStmt.setString(1, (String) u[0]);
                uStmt.setString(2, (String) u[1]);
                uStmt.setString(3, (String) u[2]);
                uStmt.setString(4, (String) u[3]);
                uStmt.setString(5, (String) u[4]);
                uStmt.setInt(6, (Integer) u[5]);
                uStmt.setString(7, (String) u[6]);
                uStmt.setTimestamp(8, (Timestamp) u[7]);
                uStmt.setTimestamp(9, (Timestamp) u[8]);
                uStmt.addBatch();
            }
            uStmt.executeBatch();
        }

        // 2. Insert Contacts
        String insertContact = "INSERT INTO contactos (id_usuario, id_contacto) VALUES (?::uuid, ?::uuid)";
        try (PreparedStatement cStmt = conn.prepareStatement(insertContact)) {
            String u1 = "11111111-1111-1111-1111-111111111111";
            String[] others = {"22222222-2222-2222-2222-222222222222", "33333333-3333-3333-3333-333333333333", "44444444-4444-4444-4444-444444444444", "55555555-5555-5555-5555-555555555555", "66666666-6666-6666-6666-666666666666"};
            for (String o : others) {
                cStmt.setString(1, u1);
                cStmt.setString(2, o);
                cStmt.addBatch();
            }
            cStmt.executeBatch();
        }

        // 3. Insert Chats and Members
        String insertChat = "INSERT INTO chat (id_chat) VALUES (?)";
        String insertMember = "INSERT INTO chat_miembro (id_chat, id_user, fecha_union) VALUES (?, ?::uuid, ?)";
        try (PreparedStatement cStmt = conn.prepareStatement(insertChat);
             PreparedStatement mStmt = conn.prepareStatement(insertMember)) {
            int[] chatIds = {1, 2, 3, 4, 5};
            String u1 = "11111111-1111-1111-1111-111111111111";
            String[] otherUsers = {"22222222-2222-2222-2222-222222222222", "33333333-3333-3333-3333-333333333333", "44444444-4444-4444-4444-444444444444", "55555555-5555-5555-5555-555555555555", "66666666-6666-6666-6666-666666666666"};
            for (int i = 0; i < chatIds.length; i++) {
                cStmt.setInt(1, chatIds[i]);
                cStmt.addBatch();

                mStmt.setInt(1, chatIds[i]);
                mStmt.setString(2, u1);
                mStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
                mStmt.addBatch();

                mStmt.setInt(1, chatIds[i]);
                mStmt.setString(2, otherUsers[i]);
                mStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
                mStmt.addBatch();
            }
            cStmt.executeBatch();
            mStmt.executeBatch();
        }

        // 4. Insert Messages
        String insertMsg = "INSERT INTO mensajes (id_chat, id_usuario, texto_original, idioma_origen, es_audio, fecha_envio) VALUES (?, ?::uuid, ?, ?, FALSE, ?)";
        try (PreparedStatement msgStmt = conn.prepareStatement(insertMsg)) {
            String u1 = "11111111-1111-1111-1111-111111111111";
            Object[][] messages = {
                {1, "22222222-2222-2222-2222-222222222222", "¡Hola! ¿Puedes traducir esto para mí?", "EN", Timestamp.valueOf(LocalDateTime.now().withHour(9).withMinute(30))},
                {1, u1, "¡Claro! ¿Qué necesitas?", "ES", Timestamp.valueOf(LocalDateTime.now().withHour(9).withMinute(31))},
                {1, "22222222-2222-2222-2222-222222222222", "¿Cómo va tu día?", "EN", Timestamp.valueOf(LocalDateTime.now().withHour(9).withMinute(32))},
                {1, u1, "¡Muy bien, gracias! Trabajando en un proyecto JavaFX", "ES", Timestamp.valueOf(LocalDateTime.now().withHour(9).withMinute(33))},
                {1, "22222222-2222-2222-2222-222222222222", "¡Eso suena genial! Muéstramelo cuando termines 🚀", "EN", Timestamp.valueOf(LocalDateTime.now().withHour(9).withMinute(34))},

                {2, "33333333-3333-3333-3333-333333333333", "¡Buenos días! ¿Cómo estás?", "FR", Timestamp.valueOf(LocalDateTime.now().withHour(8).withMinute(15))},
                {2, u1, "¡Bien! ¿Y tú?", "ES", Timestamp.valueOf(LocalDateTime.now().withHour(8).withMinute(16))},
                {2, "33333333-3333-3333-3333-333333333333", "¡Muy bien gracias! ¿Nos vemos mañana?", "FR", Timestamp.valueOf(LocalDateTime.now().withHour(8).withMinute(17))},

                {3, "44444444-4444-4444-4444-444444444444", "¡Buenos días! ¿Tienes tiempo?", "DE", Timestamp.valueOf(LocalDateTime.now().withHour(7).withMinute(0))},
                {3, u1, "Sí, dime", "ES", Timestamp.valueOf(LocalDateTime.now().withHour(7).withMinute(5))},

                {4, "55555555-5555-5555-5555-555555555555", "¡Hola! ¿Cómo estás?", "IT", Timestamp.valueOf(LocalDateTime.now().withHour(10).withMinute(0))},
                {4, u1, "¡Perfecto! ¿Qué tal tu fin de semana?", "ES", Timestamp.valueOf(LocalDateTime.now().withHour(10).withMinute(1))},
                {4, "55555555-5555-5555-5555-555555555555", "¡Bellísimo! Fui a Roma 🏛️", "IT", Timestamp.valueOf(LocalDateTime.now().withHour(10).withMinute(2))},

                {5, "66666666-6666-6666-6666-666666666666", "¡Hola! ¿Estás bien?", "JP", Timestamp.valueOf(LocalDateTime.now().withHour(6).withMinute(30))},
                {5, u1, "¡Hola Yuki! Sí, muy bien 😊", "ES", Timestamp.valueOf(LocalDateTime.now().withHour(6).withMinute(35))}
            };
            for (Object[] m : messages) {
                msgStmt.setInt(1, (Integer) m[0]);
                msgStmt.setString(2, (String) m[1]);
                msgStmt.setString(3, (String) m[2]);
                msgStmt.setString(4, (String) m[3]);
                msgStmt.setTimestamp(5, (Timestamp) m[4]);
                msgStmt.addBatch();
            }
            msgStmt.executeBatch();
        }

        conn.commit();
        conn.setAutoCommit(true);
        System.out.println("Data seeding complete!");
    }
}
