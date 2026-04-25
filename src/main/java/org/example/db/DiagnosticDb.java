package org.example.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DiagnosticDb {

    public static void main(String[] args) {
        System.out.println("🔍 STARTING DATABASE EXPLORATION...");

        try (Connection conn = DatabaseManager.getConnection()) {
            System.out.println("✅ Connected to: " + conn.getMetaData().getURL());
            System.out.println("👤 Connected as: " + conn.getMetaData().getUserName());
            
            DatabaseMetaData metaData = conn.getMetaData();
            
            // 1. List Tables
            System.out.println("\n📋 TABLES IN PUBLIC SCHEMA:");
            ResultSet tables = metaData.getTables(null, "public", "%", new String[]{"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("\n[TABLE: " + tableName + "]");
                
                // 2. List Columns for each table
                ResultSet columns = metaData.getColumns(null, "public", tableName, "%");
                while (columns.next()) {
                    String colName = columns.getString("COLUMN_NAME");
                    String colType = columns.getString("TYPE_NAME");
                    System.out.println("  - " + colName + " (" + colType + ")");
                }
            }
            
            System.out.println("\n✅ EXPLORATION COMPLETE.");
            System.out.println("👉 Copy and paste the output above so I can verify the schema.");

        } catch (SQLException e) {
            System.err.println("❌ FAILED to explore database:");
            e.printStackTrace();
            System.err.println("\n⚠️ TIP: Check if your Connection String in DatabaseManager.java matches the project 'bold-art-72714243'.");
        }
    }
}
