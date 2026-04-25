package org.example.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Free SMS service using the Textbelt API.
 * No signup or API key required.
 * Free tier: 1 SMS per day (key = "textbelt").
 * For unlimited use, purchase a key at https://textbelt.com
 * 
 * Phone numbers must include country code (e.g. +34612345678 for Spain).
 */
public class TextbeltSmsService implements SmsService {

    private static final String API_URL = "https://textbelt.com/text";
    private final String apiKey;

    /**
     * Creates a TextbeltSmsService with the free key (1 SMS/day).
     */
    public TextbeltSmsService() {
        this("textbelt"); // Free key
    }

    /**
     * Creates a TextbeltSmsService with a custom/paid key for unlimited SMS.
     */
    public TextbeltSmsService(String apiKey) {
        this.apiKey = apiKey;
        System.out.println("📱 TextbeltSmsService initialized (key: " + 
            (apiKey.equals("textbelt") ? "FREE - 1 SMS/day" : "PAID") + ")");
    }

    @Override
    public String sendSms(String to, String message) {
        try {
            // Build JSON payload
            String jsonPayload = String.format(
                "{\"phone\":\"%s\",\"message\":\"%s\",\"key\":\"%s\"}",
                escapeJson(to), escapeJson(message), escapeJson(apiKey)
            );

            System.out.println("📤 Sending SMS to " + to + " via Textbelt...");

            // Create connection
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // Send payload
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read response
            int responseCode = conn.getResponseCode();
            StringBuilder response = new StringBuilder();
            
            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();

            String responseBody = response.toString();
            System.out.println("📨 Textbelt response (HTTP " + responseCode + "): " + responseBody);

            // Check if successful (response contains "success":true)
            if (responseBody.contains("\"success\":true")) {
                String textId = extractJsonValue(responseBody, "textId");
                System.out.println("✅ SMS sent successfully to " + to + " (ID: " + textId + ")");
                return textId != null ? textId : "unknown-id";
            } else {
                System.err.println("❌ SMS failed. Response: " + responseBody);
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ Error sending SMS via Textbelt: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":\"([^\"]*)\"";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // Try numeric if string fails
        pattern = "\"" + key + "\":([^,}]*)";
        matcher = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (matcher.find()) {
            return matcher.group(1).trim().replace("\"", "");
        }
        return null;
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r");
    }
}
