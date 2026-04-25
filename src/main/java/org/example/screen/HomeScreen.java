package org.example.screen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.model.Chat;
import org.example.model.Story;
import org.example.provider.ChatProvider;
import org.example.provider.StoryProvider;

import java.util.List;
import javafx.stage.FileChooser;
import java.io.File;

public class HomeScreen extends VBox {

    private final MainAppScreen parent;
    private final VBox chatListContainer = new VBox(0);

    public HomeScreen(MainAppScreen parent) {
        this.parent = parent;
        setSpacing(0);
        setFillWidth(true);
        getStyleClass().add("sidebar");

        boolean hasContacts = !ChatProvider.getInstance().getChats().isEmpty();

        getChildren().add(buildHeader());
        
        if (hasContacts) {
            getChildren().addAll(
                buildStoriesSection(),
                buildDivider()
            );
        }

        getChildren().addAll(
                buildMessagesHeader(),
                buildChatList(),
                buildNewChatButton());
    }

    private HBox buildHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 16, 12, 20));
        header.getStyleClass().add("sidebar-header");

        Label logoIcon = new Label("💬");
        logoIcon.setFont(Font.font(24));

        Label title = new Label("PolyChat");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.getStyleClass().add("sidebar-title");
        HBox.setMargin(title, new Insets(0, 0, 0, 10));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // "Mi historia"
        Button notifBtn = new Button("🔔");
        notifBtn.getStyleClass().addAll("sidebar-icon-btn");
        notifBtn.setTooltip(new Tooltip("Notificaciones"));
        notifBtn.setOnAction(e -> parent.showNotifications());

        org.example.provider.FriendRequestProvider.getInstance().getPendingRequests().addListener((javafx.collections.ListChangeListener<org.example.model.FriendRequest>) c -> {
            if (org.example.provider.FriendRequestProvider.getInstance().getPendingRequests().isEmpty()) {
                notifBtn.setStyle("");
            } else {
                notifBtn.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            }
        });

        Button settingsBtn = new Button("⚙");
        settingsBtn.getStyleClass().addAll("sidebar-icon-btn");
        settingsBtn.setTooltip(new Tooltip("Ajustes"));
        settingsBtn.setOnAction(e -> parent.showSettings());

        header.getChildren().addAll(logoIcon, title, spacer, notifBtn, settingsBtn);
        return header;
    }

    private ScrollPane buildStoriesSection() {
        HBox stories = new HBox(18);
        stories.setPadding(new Insets(12, 16, 16, 16));
        stories.setAlignment(Pos.CENTER_LEFT);

        String currentUserId = org.example.provider.AuthProvider.getInstance().getCurrentUser() != null ? 
                org.example.provider.AuthProvider.getInstance().getCurrentUser().getId() : "";
                
        List<Story> allStories = StoryProvider.getInstance().getStories();
        java.util.Map<String, Story> latestStories = new java.util.LinkedHashMap<>();
        for (Story s : allStories) {
            if (!latestStories.containsKey(s.getUserId())) {
                latestStories.put(s.getUserId(), s);
            }
        }

        Story myLatestStory = latestStories.get(currentUserId);
        
        // "Mi historia"
        stories.getChildren().add(buildMyStory(myLatestStory));

        for (Story s : latestStories.values()) {
            if (!s.getUserId().equals(currentUserId)) {
                stories.getChildren().add(buildStoryCell(s));
            }
        }

        ScrollPane scroll = new ScrollPane(stories);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("stories-scroll");
        scroll.setPrefHeight(140);
        scroll.setMinHeight(140);
        scroll.setMaxHeight(140);
        return scroll;
    }

    private VBox buildMyStory(Story myLatestStory) {
        VBox cell = new VBox(6);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.setMinWidth(60);

        StackPane avatarStack = new StackPane();
        Circle avatar = new Circle(30);
        
        if (myLatestStory != null) {
            // User has a story, show their avatar or story preview
            avatar.setFill(Color.web(myLatestStory.getAvatarColor()));
            avatar.getStyleClass().add("story-ring");
            
            Label initials = new Label(myLatestStory.getAvatarInitials());
            initials.setFont(Font.font("System", FontWeight.BOLD, 18));
            initials.setStyle("-fx-text-fill: white;");
            avatarStack.getChildren().addAll(avatar, initials);
            
            // Si ya tiene historia, al pulsarla que la vea
            cell.setStyle("-fx-cursor: hand;");
            cell.setOnMouseClicked(e -> parent.showStory(myLatestStory));
        } else {
            // No story, show empty state with "+"
            avatar.getStyleClass().add("story-avatar-empty");
            Label initials = new Label("Me");
            initials.setFont(Font.font("System", FontWeight.BOLD, 18));
            initials.setStyle("-fx-text-fill: white;");
    
            Label addBadge = new Label("+");
            addBadge.setFont(Font.font(12));
            addBadge.setStyle(
                    "-fx-background-color: white; -fx-text-fill: black; -fx-background-radius: 100; -fx-padding: 1 3;");
            StackPane.setAlignment(addBadge, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(addBadge, new Insets(0, 5, 5, 0));
    
            avatarStack.getChildren().addAll(avatar, initials, addBadge);
            
            cell.setStyle("-fx-cursor: hand;");
            cell.setOnMouseClicked(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Seleccionar foto para tu historia");
                File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
                
                if (selectedFile != null) {
                    String fileUrl = selectedFile.toURI().toString();
                    StoryProvider.getInstance().addStory("", fileUrl);
                    parent.refreshSidebar();
                }
            });
        }

        Label name = new Label("Tu historia");
        name.getStyleClass().add("story-name");

        cell.getChildren().addAll(avatarStack, name);
        
        return cell;
    }

    private VBox buildStoryCell(Story story) {
        VBox cell = new VBox(8);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.setMinWidth(70); 
        cell.setStyle("-fx-cursor: hand;");

        StackPane ring = new StackPane();
        Circle outerRing = new Circle(32);
        outerRing.setFill(Color.TRANSPARENT);
        outerRing.getStyleClass().add("story-ring");

        Circle avatar = new Circle(28);
        avatar.setFill(Color.web(story.getAvatarColor()));
        Label init = new Label(story.getAvatarInitials());
        init.setFont(Font.font("System", FontWeight.BOLD, 14));
        init.setStyle("-fx-text-fill: white;");

        ring.getChildren().addAll(outerRing, avatar, init);

        Label nameLabel = new Label(story.getUserName());
        nameLabel.getStyleClass().add("story-name");
        nameLabel.setMaxWidth(60);
        nameLabel.setAlignment(Pos.CENTER);

        cell.getChildren().addAll(ring, nameLabel);
        cell.setOnMouseClicked(e -> parent.showStory(story));
        return cell;
    }

    private Separator buildDivider() {
        Separator sep = new Separator();
        sep.getStyleClass().add("sidebar-divider");
        return sep;
    }

    private VBox buildMessagesHeader() {
        VBox header = new VBox(4);
        header.setPadding(new Insets(16, 16, 12, 20));

        Label messages = new Label("Mensajes");
        messages.setFont(Font.font("System", FontWeight.BOLD, 20));
        messages.getStyleClass().add("section-title");

        Label sub = new Label("Chats con traducción automática");
        sub.getStyleClass().add("section-subtitle");

        header.getChildren().addAll(messages, sub);
        return header;
    }

    private ScrollPane buildChatList() {
        chatListContainer.setFillWidth(true);
        loadChatItems();

        ChatProvider.getInstance().getChats().addListener((javafx.collections.ListChangeListener<Chat>) c -> {
            javafx.application.Platform.runLater(this::loadChatItems);
        });

        ScrollPane scroll = new ScrollPane(chatListContainer);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("chat-list-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    private void loadChatItems() {
        chatListContainer.getChildren().clear();
        for (Chat chat : ChatProvider.getInstance().getChats()) {
            chatListContainer.getChildren().add(buildChatItem(chat));
        }
    }

    private HBox buildChatItem(Chat chat) {
        HBox item = new HBox(14);
        item.setPadding(new Insets(12, 16, 12, 20));
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("chat-item");
        item.setId("chat-" + chat.getId());

        // Avatar
        StackPane avatar = new StackPane();
        Circle circle = new Circle(26);
        circle.setFill(Color.web(chat.getAvatarColor()));
        Label initials = new Label(chat.getAvatarInitials());
        initials.setFont(Font.font("System", FontWeight.BOLD, 14));
        initials.setStyle("-fx-text-fill: white;");
        avatar.getChildren().addAll(circle, initials);

        // Text
        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        HBox nameRow = new HBox();
        Label name = new Label(chat.getParticipantName());
        name.setFont(Font.font("System", FontWeight.BOLD, 14));
        name.getStyleClass().add("chat-item-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lang = new Label(chat.getParticipantLanguage());
        lang.getStyleClass().add("chat-item-lang");
        nameRow.getChildren().addAll(name, spacer, lang);

        Label lastMsg = new Label(chat.getLastMessage() != null ? chat.getLastMessage() : "Sin mensajes");
        lastMsg.getStyleClass().add("chat-item-preview");
        lastMsg.setMaxWidth(200);

        textBox.getChildren().addAll(nameRow, lastMsg);
        item.getChildren().addAll(avatar, textBox);

        item.setOnMouseClicked(e -> selectChat(chat, item));
        return item;
    }

    private void selectChat(Chat chat, HBox item) {
        // Clear active state from all items
        chatListContainer.getChildren().forEach(n -> n.getStyleClass().remove("chat-item-active"));
        // Set active
        item.getStyleClass().add("chat-item-active");
        parent.showChat(chat.getId());
    }

    private VBox buildNewChatButton() {
        HBox btn = new HBox(10);
        btn.setAlignment(Pos.CENTER);
        btn.getStyleClass().add("new-chat-btn");

        Label icon = new Label("💬");
        icon.getStyleClass().add("new-chat-icon");

        Label text = new Label("Nuevo chat");
        text.getStyleClass().add("new-chat-label");

        btn.getChildren().addAll(icon, text);
        btn.setOnMouseClicked(e -> {
            parent.showContactsList();
        });

        VBox wrapper = new VBox(btn);
        wrapper.setPadding(new Insets(0, 20, 20, 20));
        return wrapper;
    }
}
