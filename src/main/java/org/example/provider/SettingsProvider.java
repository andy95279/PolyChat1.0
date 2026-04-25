package org.example.provider;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SettingsProvider {
    private static SettingsProvider instance;

    private final StringProperty theme = new SimpleStringProperty("dark");
    private final IntegerProperty chatBackgroundIndex = new SimpleIntegerProperty(0);
    private final StringProperty colorblindMode = new SimpleStringProperty("none");

    private SettingsProvider() {
    }

    public static SettingsProvider getInstance() {
        if (instance == null)
            instance = new SettingsProvider();
        return instance;
    }

    public StringProperty themeProperty() {
        return theme;
    }

    public String getTheme() {
        return theme.get();
    }

    public void setTheme(String t) {
        theme.set(t);
        persistSettings();
    }

    public IntegerProperty chatBackgroundIndexProperty() {
        return chatBackgroundIndex;
    }

    public int getChatBackgroundIndex() {
        return chatBackgroundIndex.get();
    }

    public void setChatBackgroundIndex(int i) {
        chatBackgroundIndex.set(i);
        persistSettings();
    }

    public StringProperty colorblindModeProperty() {
        return colorblindMode;
    }

    public String getColorblindMode() {
        return colorblindMode.get();
    }

    public void setColorblindMode(String m) {
        colorblindMode.set(m);
        persistSettings();
    }

    public void loadForUser(String userId) {
        org.example.db.SettingsDao.loadSettings(userId, this);
    }

    private void persistSettings() {
        if (AuthProvider.getInstance().getCurrentUser() != null) {
            String userId = AuthProvider.getInstance().getCurrentUser().getId();
            org.example.db.SettingsDao.saveSettings(userId, getTheme(), getChatBackgroundIndex(), getColorblindMode());
        }
    }
}
