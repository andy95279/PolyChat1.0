package org.example.screen;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import org.example.db.ChatDao;
import org.example.model.Contact;
import org.example.provider.AuthProvider;
import org.example.provider.ChatProvider;
import org.example.provider.ContactProvider;

public class ContactsListScreen extends VBox {

    private final MainAppScreen parent;
    private final VBox contactsContainer = new VBox(10);

    public ContactsListScreen(MainAppScreen parent) {
        this.parent = parent;
        setPadding(new Insets(30, 40, 30, 40));
        setSpacing(20);
        getStyleClass().add("add-contact-screen");

        Label title = new Label("Seleccionar Amigo");
        title.setFont(Font.font("System", 24));
        title.getStyleClass().add("screen-title");

        Button addFriendBtn = new Button("¿Añadir Nuevo Amigo?");
        addFriendBtn.getStyleClass().add("action-btn");
        addFriendBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold;");
        addFriendBtn.setOnAction(e -> parent.showAddContact());

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, addFriendBtn);

        ScrollPane scroll = new ScrollPane(contactsContainer);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("results-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(header, new Label("Tus amigos:"), scroll);

        loadContactsUI();

        ContactProvider.getInstance().getContacts().addListener((ListChangeListener<Contact>) c -> {
            loadContactsUI();
        });
    }

    private void loadContactsUI() {
        contactsContainer.getChildren().clear();
        if (ContactProvider.getInstance().getContacts().isEmpty()) {
            contactsContainer.getChildren().add(new Label("Aún no tienes amigos. ¡Añade uno!"));
        } else {
            for (Contact contact : ContactProvider.getInstance().getContacts()) {
                contactsContainer.getChildren().add(buildContactRow(contact));
            }
        }
    }

    private HBox buildContactRow(Contact user) {
        HBox row = new HBox(15);
        row.setPadding(new Insets(10));
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("user-search-row");

        Circle avatar = new Circle(20, Color.web(user.getAvatarColor()));
        Label initials = new Label(user.getAvatarInitials());
        initials.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        StackPane avatarStack = new StackPane(avatar, initials);

        VBox info = new VBox(2);
        Label name = new Label(user.getName());
        name.setFont(Font.font("System", 14));
        name.setStyle("-fx-font-weight: bold;");
        Label status = new Label(user.isOnline() ? "En línea" : "Desconectado");
        status.setStyle("-fx-text-fill: " + (user.isOnline() ? "#10B981" : "#6B7280") + "; -fx-font-size: 12px;");
        info.getChildren().addAll(name, status);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button startChatBtn = new Button("💬 Chatear");
        startChatBtn.getStyleClass().add("primary-btn");
        startChatBtn.setOnAction(e -> startChat(user, startChatBtn));

        row.getChildren().addAll(avatarStack, info, spacer, startChatBtn);
        return row;
    }

    private void startChat(Contact contact, Button btn) {
        // Disable button while the DB call is in progress
        btn.setText("⏳ Conectando…");
        btn.setDisable(true);

        String myId = AuthProvider.getInstance().getCurrentUser().getId();

        new Thread(() -> {
            int chatId = ChatDao.getChatBetween(myId, contact.getId());
            if (chatId == -1) {
                chatId = ChatDao.createChat(myId, contact.getId());
            }

            final int finalChatId = chatId;

            javafx.application.Platform.runLater(() -> {
                if (finalChatId == -1) {
                    btn.setText("💬 Chatear");
                    btn.setDisable(false);
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("No se pudo crear el chat");
                    alert.setContentText("Revisa la conexión a la base de datos e inténtalo de nuevo.");
                    alert.showAndWait();
                    return;
                }

                // Reload provider so getChatById will find the new chat
                ChatProvider.getInstance().loadChatsFromDb();
                parent.showChat(String.valueOf(finalChatId));
            });
        }).start();
    }
}
