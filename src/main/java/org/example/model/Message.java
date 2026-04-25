package org.example.model;

import java.time.LocalTime;

public class Message {
    private final String id;
    private final String senderId;
    private final String text;
    private final String originalText;
    private final String sourceLanguage;
    private final LocalTime timestamp;
    private final boolean isAudio;
    private final String audioDuration;
    private final String attachmentUrl;
    private boolean showOriginal;

    public Message(String id, String senderId, String text, String originalText,
            String sourceLanguage, LocalTime timestamp, boolean isAudio, String audioDuration, String attachmentUrl) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.originalText = originalText;
        this.sourceLanguage = sourceLanguage;
        this.timestamp = timestamp;
        this.isAudio = isAudio;
        this.audioDuration = audioDuration;
        this.attachmentUrl = attachmentUrl;
        this.showOriginal = false;
    }

    // Convenience constructors
    public Message(String id, String senderId, String text, LocalTime timestamp) {
        this(id, senderId, text, null, null, timestamp, false, null, null);
    }

    public Message(String id, String senderId, String text, String originalText,
            String sourceLanguage, LocalTime timestamp) {
        this(id, senderId, text, originalText, sourceLanguage, timestamp, false, null, null);
    }

    public String getId() {
        return id;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getText() {
        return text;
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public LocalTime getTimestamp() {
        return timestamp;
    }

    public boolean isAudio() {
        return isAudio;
    }

    public String getAudioDuration() {
        return audioDuration;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public boolean isShowOriginal() {
        return showOriginal;
    }

    public void setShowOriginal(boolean showOriginal) {
        this.showOriginal = showOriginal;
    }

    public void toggleShowOriginal() {
        this.showOriginal = !this.showOriginal;
    }
}
