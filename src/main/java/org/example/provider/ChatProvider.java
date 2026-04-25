package org.example.provider;

import org.example.db.ChatDao;
import org.example.db.MessageDao;
import org.example.model.Chat;
import org.example.model.Message;
import org.example.model.User;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ChatProvider {

    private static ChatProvider instance;
    private final ObservableList<Chat> chats = FXCollections.observableArrayList();

    private ChatProvider() {
        loadChatsFromDb();
    }

    private String getCurrentUserId() {
        return AuthProvider.getInstance().getCurrentUser() != null
                ? AuthProvider.getInstance().getCurrentUser().getId()
                : "unknown";
    }

    public static String languageNameToCode(String languageName) {
        if (languageName == null || languageName.trim().isEmpty()) return "ES";
        
        String langTrimmed = languageName.trim();
        // Check for formats like "en English" or "fr Français"
        if (langTrimmed.length() > 2 && langTrimmed.charAt(2) == ' ') {
            return langTrimmed.substring(0, 2).toUpperCase();
        }

        switch (langTrimmed.toUpperCase()) {
            case "ESPAÑOL": case "SPANISH": return "ES";
            case "INGLÉS": case "INGLES": case "ENGLISH": return "EN";
            case "FRANCÉS": case "FRANCES": case "FRENCH": return "FR";
            case "ALEMÁN": case "ALEMAN": case "GERMAN": return "DE";
            case "ITALIANO": case "ITALIAN": return "IT";
            case "PORTUGUÉS": case "PORTUGUES": case "PORTUGUESE": return "PT";
            default: 
                if (langTrimmed.length() == 2) return langTrimmed.toUpperCase();
                return "ES";
        }
    }

    private String getSenderLanguageCode() {
        User u = AuthProvider.getInstance().getCurrentUser();
        if (u != null) {
            return languageNameToCode(u.getLanguage());
        }
        return "ES";
    }

    public static ChatProvider getInstance() {
        if (instance == null) instance = new ChatProvider();
        return instance;
    }

    /**
     * Destroys the current singleton so the next call to getInstance() creates
     * a fresh one loaded with the new user's chats. Must be called on logout.
     */
    public static void reset() {
        instance = null;
    }

    // -----------------------------------------------------------------------
    // Chat loading
    // -----------------------------------------------------------------------

    public void loadChatsFromDb() {
        chats.clear();
        chats.addAll(ChatDao.getChatsForUser(getCurrentUserId()));
        for (Chat c : chats) {
            c.getMessages().clear();
            List<Message> msgs = MessageDao.getMessagesForChat(Integer.parseInt(c.getId()));
            for (Message m : msgs) {
                c.addMessage(translateIfNeeded(m, c));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Real-time polling — called from ChatScreen's timeline on a bg thread
    // -----------------------------------------------------------------------

    /**
     * Fetches any messages with id > lastMessageId from the DB and appends them
     * to the in-memory chat.  Returns the id of the highest message seen so far
     * (unchanged if there are no new messages).
     */
    public int pollNewMessages(String chatId, int lastMessageId) {
        Chat chat = getChatById(chatId);
        if (chat == null) return lastMessageId;

        List<Message> newMsgs;
        try {
            newMsgs = MessageDao.getMessagesAfter(Integer.parseInt(chatId), lastMessageId);
        } catch (NumberFormatException e) {
            return lastMessageId;
        }

        int highestId = lastMessageId;
        for (Message m : newMsgs) {
            chat.addMessage(translateIfNeeded(m, chat));
            try {
                int id = Integer.parseInt(m.getId());
                if (id > highestId) highestId = id;
            } catch (NumberFormatException ignored) {}
        }
        return highestId;
    }

    // -----------------------------------------------------------------------
    // Sending
    // -----------------------------------------------------------------------

    /**
     * Sends a text message and returns the DB-assigned id_mensaje (used to
     * advance lastMessageId in ChatScreen so the poller won't duplicate it).
     */
    public int sendMessage(String chatId, String text) {
        Chat chat = getChatById(chatId);
        if (chat == null || text == null || text.trim().isEmpty()) return -1;

        String msgId = UUID.randomUUID().toString();
        String myLangCode = getSenderLanguageCode();
        Message sent = new Message(msgId, getCurrentUserId(),
                text, text, myLangCode, LocalTime.now(), false, null, null);

        chat.addMessage(sent);
        int dbId = MessageDao.saveMessage(sent, Integer.parseInt(chatId));
        System.out.println("📤 Message saved, DB id: " + dbId);
        return dbId;
        // No bot response — real user on the other end will reply
    }

    public int sendAudioMessage(String chatId, String transcription, String duration) {
        Chat chat = getChatById(chatId);
        if (chat == null) return -1;

        String msgId = UUID.randomUUID().toString();
        String myLangCode = getSenderLanguageCode();
        Message sent = new Message(msgId, getCurrentUserId(),
                transcription, transcription, myLangCode, LocalTime.now(), true, duration, null);

        chat.addMessage(sent);
        int dbId = MessageDao.saveMessage(sent, Integer.parseInt(chatId));
        System.out.println("📤 Audio message saved, DB id: " + dbId);
        return dbId;
        // No bot response
    }

    /**
     * Sends an attachment message and returns the DB-assigned id_mensaje.
     */
    public int sendAttachmentMessage(String chatId, String fileUrl) {
        Chat chat = getChatById(chatId);
        if (chat == null || fileUrl == null) return -1;

        String msgId = UUID.randomUUID().toString();
        String myLangCode = getSenderLanguageCode();
        Message sent = new Message(msgId, getCurrentUserId(),
                "Adjunto", "Adjunto", myLangCode, LocalTime.now(), false, null, fileUrl);

        chat.addMessage(sent);
        int dbId = MessageDao.saveMessage(sent, Integer.parseInt(chatId));
        System.out.println("📤 Attachment message saved, DB id: " + dbId);
        return dbId;
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    public ObservableList<Chat> getChats() { return chats; }

    public Chat getChatById(String id) {
        return chats.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    public void toggleShowOriginal(String chatId, String messageId) {
        Chat chat = getChatById(chatId);
        if (chat == null) return;
        chat.getMessages().stream()
                .filter(m -> m.getId().equals(messageId))
                .findFirst()
                .ifPresent(Message::toggleShowOriginal);
    }

    // -----------------------------------------------------------------------
    // Internal
    // -----------------------------------------------------------------------

    private Message translateIfNeeded(Message m, Chat chat) {
        if (m.getText() != null) return m; // already translated
        String myId = getCurrentUserId();
        // Only translate messages from OTHER users (my own messages are already in ES)
        if (myId.equals(m.getSenderId())) {
            // My own message — show as-is
            return new Message(m.getId(), m.getSenderId(),
                    m.getOriginalText(), m.getOriginalText(),
                    m.getSourceLanguage(), m.getTimestamp(),
                    m.isAudio(), m.getAudioDuration(), m.getAttachmentUrl());
        }
        
        String myLangCode = getSenderLanguageCode();
        String originalLangCode = m.getSourceLanguage() != null ? m.getSourceLanguage() : "ES";
        
        // Don't translate if languages match
        if (myLangCode.equalsIgnoreCase(originalLangCode)) {
             return new Message(m.getId(), m.getSenderId(),
                    m.getOriginalText(), m.getOriginalText(),
                    m.getSourceLanguage(), m.getTimestamp(),
                    m.isAudio(), m.getAudioDuration(), m.getAttachmentUrl());
        }
        
        String translated = TranslationProvider.getInstance()
                .translate(m.getOriginalText(), myLangCode); // translate to user's language
        return new Message(m.getId(), m.getSenderId(),
                translated, m.getOriginalText(),
                m.getSourceLanguage(), m.getTimestamp(),
                m.isAudio(), m.getAudioDuration(), m.getAttachmentUrl());
    }
}
