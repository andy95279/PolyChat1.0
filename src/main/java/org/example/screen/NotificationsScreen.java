package org.example.screen;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import org.example.model.FriendRequest;
import org.example.provider.FriendRequestProvider;

public class NotificationsScreen extends VBox {

    private final VBox listContainer = new VBox(10);

    public NotificationsScreen(MainAppScreen parent) {
        setPadding(new Insets(30, 40, 30, 40));
        setSpacing(20);

        Label title = new Label("Notificaciones");
        title.setFont(Font.font("System", 24));
        title.setStyle("-fx-font-weight: bold;");

        ScrollPane scroll = new ScrollPane(listContainer);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("results-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(title, scroll);

        loadRequestsUI();

        FriendRequestProvider.getInstance().getPendingRequests().addListener((ListChangeListener<FriendRequest>) c -> {
            loadRequestsUI();
        });
    }

    private void loadRequestsUI() {
        listContainer.getChildren().clear();
        if (FriendRequestProvider.getInstance().getPendingRequests().isEmpty()) {
            listContainer.getChildren().add(new Label("No tienes notificaciones nuevas."));
        } else {
            for (FriendRequest req : FriendRequestProvider.getInstance().getPendingRequests()) {
                listContainer.getChildren().add(buildRequestRow(req));
            }
        }
    }

    private HBox buildRequestRow(FriendRequest req) {
        HBox row = new HBox(15);
        row.setPadding(new Insets(10));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8;");

        Circle avatar = new Circle(20, Color.web(req.getSenderAvatarColor()));
        Label initials = new Label(req.getSenderInitials());
        initials.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        StackPane avatarStack = new StackPane(avatar, initials);

        VBox info = new VBox(2);
        Label name = new Label(req.getSenderName());
        name.setFont(Font.font("System", 14));
        name.setStyle("-fx-font-weight: bold;");
        Label text = new Label("Quiere ser tu amigo");
        text.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        info.getChildren().addAll(name, text);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button acceptBtn = new Button("Aceptar");
        acceptBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        acceptBtn.setOnAction(e -> {
            FriendRequestProvider.getInstance().acceptRequest(req.getId(), req.getSenderId());
        });

        Button rejectBtn = new Button("Rechazar");
        rejectBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        rejectBtn.setOnAction(e -> {
            FriendRequestProvider.getInstance().rejectRequest(req.getId());
        });

        row.getChildren().addAll(avatarStack, info, spacer, acceptBtn, rejectBtn);
        return row;
    }
}
