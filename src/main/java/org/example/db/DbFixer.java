package org.example.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DbFixer {

    public static void runSql() {
        System.out.println("Attempting to connect to PostgreSQL database...");
        try (Connection conn = DatabaseManager.getConnection()) {
            System.out.println("Successfully connected to the PostgreSQL database!");
            
            // Re-create the tables properly using UUIDs for users
            String[] setupQueries = {
                // DROP everything first to ensure a clean slate
                "DROP TABLE IF EXISTS public.mensajes CASCADE",
                "DROP TABLE IF EXISTS public.chat_miembro CASCADE",
                "DROP TABLE IF EXISTS public.participantes CASCADE", // Legacy plural
                "DROP TABLE IF EXISTS public.chat CASCADE",
                "DROP TABLE IF EXISTS public.chats CASCADE", // Legacy plural
                "DROP TABLE IF EXISTS public.contactos CASCADE",
                "DROP TABLE IF EXISTS public.solicitudes_amistad CASCADE",
                "DROP TABLE IF EXISTS public.historias CASCADE",
                "DROP TABLE IF EXISTS public.codigos_verificacion CASCADE",
                "DROP TABLE IF EXISTS public.configuracion_usuario CASCADE",
                "DROP TABLE IF EXISTS public.sms_temporal CASCADE",
                "DROP TABLE IF EXISTS public.verificacion CASCADE",
                "DROP TABLE IF EXISTS public.usuarios CASCADE",

                // 1. Usuarios
                "CREATE TABLE public.usuarios (" +
                "  id_usuario UUID PRIMARY KEY DEFAULT gen_random_uuid()," +
                "  tlf VARCHAR(20) UNIQUE NOT NULL," +
                "  nombre VARCHAR(50)," +
                "  apellidos VARCHAR(100)," +
                "  email VARCHAR(100)," +
                "  edad INT," +
                "  nacionalidad VARCHAR(50)," +
                "  fecha_registro TIMESTAMP DEFAULT NOW()," +
                "  ultima_conexion TIMESTAMP" +
                ")",

                // 2. Contactos 
                "CREATE TABLE public.contactos (" +
                "  id_usuario UUID REFERENCES public.usuarios(id_usuario) ON DELETE CASCADE," +
                "  id_contacto UUID REFERENCES public.usuarios(id_usuario) ON DELETE CASCADE," +
                "  PRIMARY KEY (id_usuario, id_contacto)" +
                ")",

                "CREATE TABLE public.solicitudes_amistad (" +
                "  id_solicitud SERIAL PRIMARY KEY," +
                "  id_remitente UUID REFERENCES public.usuarios(id_usuario) ON DELETE CASCADE," +
                "  id_destinatario UUID REFERENCES public.usuarios(id_usuario) ON DELETE CASCADE," +
                "  estado VARCHAR(20) DEFAULT 'PENDIENTE'," +
                "  fecha_envio TIMESTAMP DEFAULT NOW()," +
                "  CONSTRAINT unique_solicitud UNIQUE(id_remitente, id_destinatario)" +
                ")",

                // 3. Chat
                "CREATE TABLE public.chat (" +
                "  id_chat SERIAL PRIMARY KEY," +
                "  fecha_creacion TIMESTAMP DEFAULT NOW()," +
                "  creado_por UUID REFERENCES public.usuarios(id_usuario) ON DELETE SET NULL" +
                ")",

                // 4. Chat Miembros
                "CREATE TABLE public.chat_miembro (" +
                "  id_chat INT REFERENCES public.chat(id_chat) ON DELETE CASCADE," +
                "  id_user UUID REFERENCES public.usuarios(id_usuario) ON DELETE CASCADE," +
                "  fecha_union TIMESTAMP DEFAULT NOW()," +
                "  PRIMARY KEY (id_chat, id_user)" +
                ")",

                // 5. Mensajes
                "CREATE TABLE public.mensajes (" +
                "  id_mensaje SERIAL PRIMARY KEY," +
                "  id_chat INT REFERENCES public.chat(id_chat) ON DELETE CASCADE," +
                "  id_usuario UUID REFERENCES public.usuarios(id_usuario) ON DELETE CASCADE," +
                "  texto_original TEXT," +
                "  idioma_origen VARCHAR(10)," +
                "  es_audio BOOLEAN DEFAULT FALSE," +
                "  duracion_audio VARCHAR(10)," +
                "  fecha_envio TIMESTAMP DEFAULT NOW()" +
                ")",

                // 6. Settings and Stories
                "CREATE TABLE public.configuracion_usuario (" +
                "  id_usuario UUID PRIMARY KEY REFERENCES public.usuarios(id_usuario) ON DELETE CASCADE," +
                "  tema VARCHAR(20) DEFAULT 'LIGHT'," +
                "  notificaciones BOOLEAN DEFAULT TRUE," +
                "  idioma_app VARCHAR(10) DEFAULT 'EN'" +
                ")",
                
                "CREATE TABLE public.historias (" +
                "  id_historia SERIAL PRIMARY KEY," +
                "  id_usuario UUID REFERENCES public.usuarios(id_usuario) ON DELETE CASCADE," +
                "  contenido TEXT," +
                "  imagen_url TEXT," +
                "  fecha_creacion TIMESTAMP DEFAULT NOW()" +
                ")",

                // 7. Verification codes
                "CREATE TABLE public.codigos_verificacion (" +
                "  tlf VARCHAR(20) PRIMARY KEY," +
                "  codigo VARCHAR(10) NOT NULL," +
                "  mensaje TEXT," +
                "  fecha_expiracion TIMESTAMP NOT NULL" +
                ")",
                
                "CREATE TABLE public.sms_temporal (" +
                "  id SERIAL PRIMARY KEY," +
                "  id_sms VARCHAR(100)," +
                "  tlf VARCHAR(20)," +
                "  codigo VARCHAR(10)," +
                "  mensaje TEXT," +
                "  fecha_envio TIMESTAMP DEFAULT NOW()" +
                ")",

                "CREATE TABLE public.verificacion (" +
                "  id SERIAL PRIMARY KEY," +
                "  tlf VARCHAR(20)," +
                "  codigo VARCHAR(10)," +
                "  fecha_envio TIMESTAMP DEFAULT NOW()," +
                "  fecha_caducidad TIMESTAMP" +
                ")"
            };

            for (String query : setupQueries) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(query);
                }
            }
            
            System.out.println("✅ Original database schemas restored!");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        runSql();
    }
}
