package org.example.model;

import java.time.LocalDateTime;

public class Story {
    private final String id;
    private final String userId;
    private final String userName;
    private final String avatarColor;
    private final String avatarInitials;
    private final String content;
    private final String imageUrl;
    private final LocalDateTime timestamp;

    public Story(String id, String userId, String userName, String avatarInitials, String avatarColor, String content,
            String imageUrl, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.avatarInitials = avatarInitials;
        this.avatarColor = avatarColor;
        this.content = content;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getAvatarInitials() {
        return avatarInitials;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
