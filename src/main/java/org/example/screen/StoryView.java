package org.example.screen;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.example.model.Story;
import org.example.provider.AuthProvider;
import org.example.provider.StoryProvider;

public class StoryView extends StackPane {

    private final Runnable onClose;
    private Timeline timeline;

    public StoryView(Story story, Runnable onClose) {
        this.onClose = onClose;

        getStyleClass().add("story-overlay");

        // Background dim
        Region dim = new Region();
        dim.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(400);
        content.setMaxHeight(600);
        content.getStyleClass().add("story-content-container");

        // Header with user info and progress
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));

        ProgressBar progress = new ProgressBar(0);
        progress.getStyleClass().add("story-progress");
        progress.setMaxWidth(Double.MAX_VALUE);

        HBox userInfo = new HBox(12);
        userInfo.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(20);
        avatar.setFill(Color.web(story.getAvatarColor()));
        Label initials = new Label(story.getAvatarInitials());
        initials.getStyleClass().add("story-view-initials");
        StackPane avatarStack = new StackPane(avatar, initials);

        Label name = new Label(story.getUserName());
        name.getStyleClass().add("story-view-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("story-close-btn");
        closeBtn.setOnAction(e -> close());

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        String currentUserId = AuthProvider.getInstance().getCurrentUser() != null ? AuthProvider.getInstance().getCurrentUser().getId() : "";
        if (currentUserId.equals(story.getUserId())) {
            Button deleteBtn = new Button("🗑");
            deleteBtn.getStyleClass().add("story-close-btn");
            deleteBtn.setStyle("-fx-text-fill: #ef4444;");
            deleteBtn.setOnAction(e -> {
                StoryProvider.getInstance().deleteStory(story.getId());
                close();
            });
            actions.getChildren().addAll(deleteBtn, closeBtn);
        } else {
            actions.getChildren().add(closeBtn);
        }

        userInfo.getChildren().addAll(avatarStack, name, spacer, actions);
        header.getChildren().addAll(progress, userInfo);

        // Story content
        StackPane storyBody = new StackPane();
        storyBody.getStyleClass().add("story-body");
        VBox.setVgrow(storyBody, Priority.ALWAYS);

        if (story.isImage()) {
            ImageView imageView = new ImageView(new Image(story.getImageUrl(), true));
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(380); // container max width is ~400
            imageView.getStyleClass().add("story-image");

            storyBody.getChildren().add(imageView);

            if (story.getContent() != null && !story.getContent().trim().isEmpty()) {
                // Add a subtle dark gradient/overlay for text readability if needed
                Region overlay = new Region();
                overlay.setStyle("-fx-background-color: rgba(0,0,0,0.3);");

                Label text = new Label(story.getContent());
                text.getStyleClass().add("story-body-text-overlay");
                text.setWrapText(true);
                text.setMaxWidth(340);
                text.setAlignment(Pos.CENTER);

                storyBody.getChildren().addAll(overlay, text);
            }
        } else {
            Label text = new Label(story.getContent());
            text.getStyleClass().add("story-body-text");
            text.setWrapText(true);
            text.setMaxWidth(300);
            text.setAlignment(Pos.CENTER);
            storyBody.getChildren().add(text);
        }

        content.getChildren().addAll(header, storyBody);

        getChildren().addAll(dim, content);

        // Timer for auto-close
        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progress.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(5), e -> close(), new KeyValue(progress.progressProperty(), 1)));
        timeline.play();
    }

    private void close() {
        if (timeline != null)
            timeline.stop();
        onClose.run();
    }
}
