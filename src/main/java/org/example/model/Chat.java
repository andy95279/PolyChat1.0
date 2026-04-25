package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private final String id;
    private final String participantName;
    private final String participantLanguage;
    private final String participantStatus;
    private String lastMessage;
    private final List<Message> messages;
    private final String avatarInitials;
    private final String avatarColor;

    public Chat(String id, String participantName, String participantLanguage,
            String participantStatus, String avatarInitials, String avatarColor) {
        this.id = id;
        this.participantName = participantName;
        this.participantLanguage = participantLanguage;
        this.participantStatus = participantStatus;
        this.avatarInitials = avatarInitials;
        this.avatarColor = avatarColor;
        this.messages = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getParticipantName() {
        return participantName;
    }

    public String getParticipantLanguage() {
        return participantLanguage;
    }

    public String getParticipantStatus() {
        return participantStatus;
    }

    public String getAvatarInitials() {
        return avatarInitials;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void addMessage(Message message) {
        messages.add(message);
        lastMessage = message.getText();
    }
}
