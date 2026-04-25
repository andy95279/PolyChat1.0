package org.example.provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Translates text using the Google Cloud Translation API v2 (Basic).
 * Requires a valid API key.  Replace GOOGLE_API_KEY with yours, or set the
 * environment variable GOOGLE_TRANSLATE_KEY before launching the app.
 */
public class TranslationProvider {

    // -----------------------------------------------------------------------
    // TODO: replace with your real key, or set env var GOOGLE_TRANSLATE_KEY
    // -----------------------------------------------------------------------
    private static final String GOOGLE_API_KEY =
            System.getenv("GOOGLE_TRANSLATE_KEY") != null
                    ? System.getenv("GOOGLE_TRANSLATE_KEY")
                    : "YOUR_GOOGLE_API_KEY";

    private static final String ENDPOINT =
            "https://translation.googleapis.com/language/translate/v2?key=";

    private static TranslationProvider instance;

    private TranslationProvider() {}

    public static TranslationProvider getInstance() {
        if (instance == null) instance = new TranslationProvider();
        return instance;
    }

    /**
     * Translates {@code text} to the given {@code targetLang} (e.g. "EN", "FR", "ES").
     * Returns the original text unchanged if the API call fails or the key is not set.
     */
    public String translate(String text, String targetLang) {
        if (text == null || text.trim().isEmpty()) return text;
        if (targetLang == null) return text;

        if (!"YOUR_GOOGLE_API_KEY".equals(GOOGLE_API_KEY)) {
            try {
                String urlStr = ENDPOINT + GOOGLE_API_KEY;
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);

                // Build JSON body
                String target = targetLang.toLowerCase(); // API expects lowercase ("en", "es", …)
                String body = "{\"q\":" + jsonString(text)
                        + ",\"target\":\"" + target + "\""
                        + ",\"format\":\"text\"}";

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                int status = conn.getResponseCode();
                if (status == 200) {
                    String json = readStream(conn.getInputStream());
                    // Parse: data.translations[0].translatedText
                    JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                    return root.getAsJsonObject("data")
                            .getAsJsonArray("translations")
                            .get(0).getAsJsonObject()
                            .get("translatedText").getAsString();
                } else {
                    String err = readStream(conn.getErrorStream());
                    System.err.println("Google Translate error " + status + ": " + err);
                }
            } catch (IOException e) {
                System.err.println("TranslationProvider (Google): " + e.getMessage());
            }
        }

        // -------------------------------------------------------------------
        // Fallback: Use MyMemory Free API if Google Key is missing or fails
        // -------------------------------------------------------------------
        try {
            System.out.println("Using MyMemory Translation API fallback...");
            String urlStr = "https://api.mymemory.translated.net/get?q=" 
                    + java.net.URLEncoder.encode(text, "UTF-8") 
                    + "&langpair=autodetect|" + targetLang.toLowerCase();
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            
            int status = conn.getResponseCode();
            if (status == 200) {
                String json = readStream(conn.getInputStream());
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                if (root.has("responseData")) {
                    return root.getAsJsonObject("responseData").get("translatedText").getAsString();
                }
            }
        } catch (Exception e) {
            System.err.println("TranslationProvider (MyMemory fallback): " + e.getMessage());
        }

        // Ultimate fallback: return original text
        return text;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Properly JSON-encodes a string value (handles quotes, newlines, etc.). */
    private static String jsonString(String s) {
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r")
                       .replace("\t", "\\t") + "\"";
    }

    private static String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        byte[] buf = new byte[4096];
        StringBuilder sb = new StringBuilder();
        int n;
        while ((n = is.read(buf)) != -1) {
            sb.append(new String(buf, 0, n, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
