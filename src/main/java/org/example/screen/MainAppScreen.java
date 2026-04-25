package org.example.screen;

import javafx.scene.layout.*;
import org.example.navigation.NavigationManager;

/**
 * The main two-column layout: left sidebar (HomeScreen) + right content
 * (ChatScreen / SettingsScreen)
 */
public class MainAppScreen extends StackPane {

    private final StackPane rightPanel = new StackPane();
    private final HomeScreen homeScreen;
    private final StackPane overlayContainer = new StackPane();

    public MainAppScreen() {
        homeScreen = new HomeScreen(this);

        // Sidebar configuration
        homeScreen.setPrefWidth(340);
        homeScreen.setMinWidth(300);
        homeScreen.setMaxWidth(380);

        // Right panel configuration
        rightPanel.getStyleClass().add("right-panel");
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        // Main content layout (HBox)
        HBox mainBox = new HBox(homeScreen, rightPanel);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        mainBox.setFillHeight(true);

        // Content layout
        StackPane desktopLayout = new StackPane();
        desktopLayout.getChildren().add(mainBox);

        // Final StackPane assembly
        overlayContainer.setMouseTransparent(true);
        overlayContainer.setPickOnBounds(false);

        getChildren().addAll(desktopLayout, overlayContainer);

        // Welcome placeholder
        showWelcome();

        // Register right panel for local navigation
        NavigationManager.getInstance().setLocalContentArea(rightPanel);
    }

    public void showChat(String chatId) {
        rightPanel.getChildren().setAll(new ChatScreen(chatId));
    }

    public void showSettings() {
        rightPanel.getChildren().setAll(new SettingsScreen(this));
    }

    public void showWelcome() {
        rightPanel.getChildren().setAll(new WelcomePane());
    }

    public void showAddContact() {
        rightPanel.getChildren().setAll(new AddContactScreen(this));
    }

    public void showNotifications() {
        rightPanel.getChildren().setAll(new NotificationsScreen(this));
    }

    public void showContactsList() {
        rightPanel.getChildren().setAll(new ContactsListScreen(this));
    }

    public void showStory(org.example.model.Story story) {
        overlayContainer.setMouseTransparent(false);
        StoryView storyView = new StoryView(story, () -> {
            overlayContainer.getChildren().clear();
            overlayContainer.setMouseTransparent(true);
        });
        overlayContainer.getChildren().setAll(storyView);
    }

    public void refreshSidebar() {
        // Re-create the home screen to refresh stories/chats
        HomeScreen sidebar = new HomeScreen(this);
        sidebar.setPrefWidth(280);
        ((BorderPane) getChildren().get(0)).setLeft(sidebar);
    }
}
