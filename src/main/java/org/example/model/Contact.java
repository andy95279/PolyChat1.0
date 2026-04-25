package org.example.model;

public class Contact {
    private final String id;
    private final String name;
    private final String language;
    private final boolean isOnline;
    private final String avatarInitials;
    private final String avatarColor;

    public Contact(String id, String name, String language, boolean isOnline, String avatarInitials, String avatarColor) {
        this.id = id;
        this.name = name;
        this.language = language;
        this.isOnline = isOnline;
        this.avatarInitials = avatarInitials;
        this.avatarColor = avatarColor;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public String getAvatarInitials() {
        return avatarInitials;
    }

    public String getAvatarColor() {
        return avatarColor;
    }
}
