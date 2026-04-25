package org.example.provider;

import org.example.model.Story;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StoryProvider {
    private static StoryProvider instance;
    private final List<Story> stories = new ArrayList<>();

    private StoryProvider() {
        // Will load dynamically, but can run an initial load
        loadStoriesFromDb();
    }

    public static StoryProvider getInstance() {
        if (instance == null)
            instance = new StoryProvider();
        return instance;
    }

    private String getCurrentUserId() {
        return AuthProvider.getInstance().getCurrentUser() != null
            ? AuthProvider.getInstance().getCurrentUser().getId()
            : "unknown";
    }

    private void loadStoriesFromDb() {
        String userId = getCurrentUserId();
        if (!"unknown".equals(userId)) {
            stories.clear();
            stories.addAll(org.example.db.StoryDao.getStoriesForUser(userId));
        }
    }

    public void refreshStories() {
        loadStoriesFromDb();
    }

    public List<Story> getStories() {
        loadStoriesFromDb(); // Dynamic load to ensure freshness
        LocalDateTime limit = LocalDateTime.now().minusHours(24);
        return stories.stream()
                .filter(s -> s.getTimestamp() != null && s.getTimestamp().isAfter(limit))
                .collect(Collectors.toList());
    }

    public Story getStoryById(String id) {
        return stories.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }
    
    public void addStory(String content, String imageUrl) {
        String userId = getCurrentUserId();
        if (!"unknown".equals(userId)) {
            org.example.db.StoryDao.saveStory(userId, content, imageUrl);
            loadStoriesFromDb();
        }
    }

    public void deleteStory(String storyId) {
        String userId = getCurrentUserId();
        if (!"unknown".equals(userId)) {
            org.example.db.StoryDao.deleteStory(storyId, userId);
            loadStoriesFromDb();
        }
    }
}
