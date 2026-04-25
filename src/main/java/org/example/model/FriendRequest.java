package org.example.model;

import java.time.LocalDateTime;

public class FriendRequest {
    private String id; // id_solicitud
    private String senderId;
    private String receiverId;
    private String status; // PENDIENTE, ACEPTADA, RECHAZADA
    private LocalDateTime timestamp;
    
    // Additional fields for UI display (Sender info)
    private String senderName;
    private String senderAvatarColor;
    private String senderInitials;

    public FriendRequest(String id, String senderId, String receiverId, String status, LocalDateTime timestamp, String senderName, String senderAvatarColor, String senderInitials) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.timestamp = timestamp;
        this.senderName = senderName;
        this.senderAvatarColor = senderAvatarColor;
        this.senderInitials = senderInitials;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatarColor() {
        return senderAvatarColor;
    }

    public void setSenderAvatarColor(String senderAvatarColor) {
        this.senderAvatarColor = senderAvatarColor;
    }

    public String getSenderInitials() {
        return senderInitials;
    }

    public void setSenderInitials(String senderInitials) {
        this.senderInitials = senderInitials;
    }
}
