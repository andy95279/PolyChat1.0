package org.example.screen;

import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.control.Label;

public class WelcomePane extends VBox {
    public WelcomePane() {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        getStyleClass().add("welcome-pane");

        StackPane iconContainer = new StackPane();
        iconContainer.getStyleClass().add("welcome-icon-container");

        Label icon = new Label("💬");
        icon.getStyleClass().add("welcome-icon");
        iconContainer.getChildren().add(icon);

        Label title = new Label("Selecciona un chat");
        title.getStyleClass().add("welcome-title");

        Label sub = new Label("Elige una conversación para comenzar a chatear con traducción automática");
        sub.getStyleClass().add("welcome-subtitle");
        sub.setWrapText(true);
        sub.setMaxWidth(300);
        sub.setLineSpacing(4);

        getChildren().addAll(iconContainer, title, sub);
    }
}
