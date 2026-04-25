package org.example.screen;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;
import javafx.util.Duration;
import org.example.model.Chat;
import org.example.model.Message;
import org.example.provider.AuthProvider;
import org.example.provider.ChatProvider;
import org.example.provider.SettingsProvider;
import org.example.service.AudioRecordingService;

import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.time.format.DateTimeFormatter;

public class ChatScreen extends VBox {

    private final String chatId;
    private final VBox messagesContainer = new VBox(16);
    private final ScrollPane scrollPane = new ScrollPane(messagesContainer);
    private final TextField messageField = new TextField();
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    // Real DB polling — tracks the highest message id seen so far
    private Timeline pollTimeline;
    private volatile int lastMessageId = 0;

    // Audio recording via Vosk
    private final AudioRecordingService audioService = new AudioRecordingService();
    private Button micBtn;

    public ChatScreen(String chatId) {
        this.chatId = chatId;
        setFillWidth(true);
        getStyleClass().add("chat-screen");

        Chat chat = ChatProvider.getInstance().getChatById(chatId);
        if (chat == null)
            return;

        getChildren().addAll(
                buildHeader(chat),
                buildMessageArea(chat),
                buildInputArea(chat));

        // Seed lastMessageId from the messages already loaded
        for (Message m : chat.getMessages()) {
            try {
                int id = Integer.parseInt(m.getId());
                if (id > lastMessageId) lastMessageId = id;
            } catch (NumberFormatException ignored) {}
        }
        startPolling(chat);
    }

    private HBox buildHeader(Chat chat) {
        HBox header = new HBox(14);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("chat-header");

        StackPane avatar = new StackPane();
        Circle circle = new Circle(22);
        circle.setFill(Color.web(chat.getAvatarColor()));
        Label initials = new Label(chat.getAvatarInitials());
        initials.setFont(Font.font("System", FontWeight.BOLD, 13));
        initials.setStyle("-fx-text-fill: white;");
        avatar.getChildren().addAll(circle, initials);

        VBox nameBox = new VBox(2);
        HBox nameRow = new HBox(8);
        Label name = new Label(chat.getParticipantName());
        name.setFont(Font.font("System", FontWeight.BOLD, 15));
        name.getStyleClass().add("chat-header-name");

        Label lang = new Label(chat.getParticipantLanguage());
        lang.getStyleClass().add("chat-lang-badge");
        nameRow.getChildren().addAll(name, lang);

        Label status = new Label(chat.getParticipantStatus());
        status.getStyleClass().add("chat-header-status");
        nameBox.getChildren().addAll(nameRow, status);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        Button infoBtn = new Button("ℹ");
        infoBtn.getStyleClass().add("chat-icon-btn");

        header.getChildren().addAll(avatar, nameBox, infoBtn);
        return header;
    }

    private ScrollPane buildMessageArea(Chat chat) {
        messagesContainer.setPadding(new Insets(20, 24, 20, 24));
        messagesContainer.setFillWidth(true);

        // Listen for background changes
        SettingsProvider settings = SettingsProvider.getInstance();
        settings.chatBackgroundIndexProperty().addListener((obs, o, n) -> {
            applyBackground(n.intValue());
        });
        applyBackground(settings.getChatBackgroundIndex());

        // Load existing messages
        updateMessages(chat);

        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("messages-scroll");

        // Ensure the viewport is transparent to let the VBox background show through
        Platform.runLater(() -> {
            if (scrollPane.lookup(".viewport") != null) {
                scrollPane.lookup(".viewport").setStyle("-fx-background-color: transparent;");
            }
        });

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Scroll to bottom on content change
        messagesContainer.heightProperty().addListener((obs, o, n) -> scrollToBottom());

        return scrollPane;
    }

    private void applyBackground(int bgIndex) {
        // We use CSS classes for backgrounds
        messagesContainer.getStyleClass().removeIf(s -> s.startsWith("chat-bg-"));
        messagesContainer.getStyleClass().add("chat-bg-" + bgIndex);
    }

    private void updateMessages(Chat chat) {
        messagesContainer.getChildren().clear();
        for (Message msg : chat.getMessages()) {
            messagesContainer.getChildren().add(buildMessageBubble(msg));
        }
    }

    private String getMyLangCode() {
        if (AuthProvider.getInstance().getCurrentUser() != null) {
            return ChatProvider.languageNameToCode(AuthProvider.getInstance().getCurrentUser().getLanguage());
        }
        return "ES";
    }

    private VBox buildInputArea(Chat chat) {
        HBox area = new HBox(10);
        area.setPadding(new Insets(14, 16, 16, 16));
        area.setAlignment(Pos.CENTER);
        area.getStyleClass().add("input-area");

        // Attach button
        Button attachBtn = new Button("📎");
        attachBtn.getStyleClass().add("input-icon-btn");
        attachBtn.setTooltip(new Tooltip("Adjuntar archivo"));
        attachBtn.setOnAction(e -> showAttachMenu());

        String myLangCode = getMyLangCode();

        // Text field
        messageField.setPromptText("Escribe en " + myLangCode + "...");
        messageField.getStyleClass().add("message-field");
        HBox.setHgrow(messageField, Priority.ALWAYS);
        messageField.setPrefHeight(44);

        messageField.setOnAction(e -> sendMessage(chat));

        // Language badge
        Label langBadge = new Label(myLangCode);
        langBadge.getStyleClass().add("lang-badge");

        HBox inputWithBadge = new HBox(8, messageField, langBadge);
        inputWithBadge.setAlignment(Pos.CENTER);
        HBox.setHgrow(inputWithBadge, Priority.ALWAYS);

        // Schedule button
        Button scheduleBtn = new Button("🕐");
        scheduleBtn.getStyleClass().add("input-icon-btn");
        scheduleBtn.setTooltip(new Tooltip("Programar mensaje"));
        scheduleBtn.setOnAction(e -> showScheduleInfo());

        // Mic button
        micBtn = new Button("🎤");
        micBtn.getStyleClass().add("input-icon-btn");
        micBtn.setTooltip(new Tooltip("Nota de voz"));
        micBtn.setOnAction(e -> toggleRecording());

        // Send button
        Button sendBtn = new Button("➤");
        sendBtn.getStyleClass().addAll("send-btn");
        sendBtn.setOnAction(e -> sendMessage(chat));

        area.getChildren().addAll(attachBtn, inputWithBadge, scheduleBtn, micBtn, sendBtn);

        VBox inputSection = new VBox(6, area, buildTranslationHint(chat));
        inputSection.getStyleClass().add("input-section");
        return inputSection;
    }

    private Label buildTranslationHint(Chat chat) {
        String myLangCode = getMyLangCode();
        String theirLangName = chat.getParticipantLanguage();
        String theirLangCode = ChatProvider.languageNameToCode(theirLangName);
        
        Label hint;
        if (myLangCode.equalsIgnoreCase(theirLangCode)) {
            hint = new Label("✨ Habláis el mismo idioma (" + myLangCode + ")");
        } else {
            hint = new Label("✨ Tú escribes en " + myLangCode + " y el destinatario lo recibe en " + theirLangCode);
        }
        hint.getStyleClass().add("translation-hint");
        return hint;
    }

    private void sendMessage(Chat chat) {
        String text = messageField.getText().trim();
        if (text.isEmpty())
            return;
        messageField.clear();
        int dbId = ChatProvider.getInstance().sendMessage(chatId, text);
        // Advance the poll cursor so the poller skips this message (prevents duplication)
        if (dbId > lastMessageId) {
            lastMessageId = dbId;
        }
        updateMessages(chat);
    }

    private Region buildMessageBubble(Message message) {
        String myId = AuthProvider.getInstance().getCurrentUser() != null
                ? AuthProvider.getInstance().getCurrentUser().getId() : "";
        boolean isMe = myId.equals(message.getSenderId());

        if (isMe) {
            return buildMyBubble(message);
        } else {
            return buildTheirBubble(message);
        }
    }

    private HBox buildMyBubble(Message message) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_RIGHT);

        StackPane bubble = new StackPane();
        bubble.getStyleClass().add("bubble-me");
        bubble.setMaxWidth(420);

        VBox content = new VBox(6);
        if (message.getAttachmentUrl() != null) {
            content.getChildren().add(buildAttachmentNode(message.getAttachmentUrl()));
        }
        
        if (message.isAudio()) {
            content.getChildren().add(buildAudioPlayer(true));
            if (message.getText() != null) {
                Label transLabel = new Label("Transcripción:");
                transLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.7); -fx-font-weight: bold;");
                
                Label text = new Label(message.getText());
                text.getStyleClass().add("bubble-text-me");
                text.setStyle("-fx-font-style: italic;");
                text.setWrapText(true);
                text.setMaxWidth(380);
                content.getChildren().addAll(transLabel, text);
            }
        } else {
            Label text = new Label(message.getText());
            text.getStyleClass().add("bubble-text-me");
            text.setWrapText(true);
            text.setMaxWidth(380);
            content.getChildren().add(text);
        }
        bubble.getChildren().add(content);

        VBox col = new VBox(4);
        col.setAlignment(Pos.CENTER_RIGHT);
        col.getChildren().add(bubble);

        Label time = new Label(message.getTimestamp().format(timeFmt));
        time.getStyleClass().add("bubble-time");
        col.getChildren().add(time);

        row.getChildren().add(col);
        return row;
    }

    private HBox buildTheirBubble(Message message) {
        Chat chat = ChatProvider.getInstance().getChatById(chatId);
        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);

        // Avatar
        StackPane avatar = new StackPane();
        Circle circle = new Circle(18);
        circle.setFill(chat != null ? Color.web(chat.getAvatarColor()) : Color.GRAY);
        Label initials = new Label(chat != null ? chat.getAvatarInitials() : "?");
        initials.setFont(Font.font("System", FontWeight.BOLD, 10));
        initials.setStyle("-fx-text-fill: white;");
        avatar.getChildren().addAll(circle, initials);

        VBox col = new VBox(4);
        col.setMaxWidth(420);

        StackPane bubble = new StackPane();
        bubble.getStyleClass().add("bubble-them");

        VBox content = new VBox(6);
        if (message.getAttachmentUrl() != null) {
            content.getChildren().add(buildAttachmentNode(message.getAttachmentUrl()));
        }
        
        if (message.isAudio()) {
            content.getChildren().add(buildAudioPlayer(false));
            if (message.getText() != null) {
                Label transLabel = new Label("✨ Transcripción:");
                transLabel.getStyleClass().add("translated-badge");
                
                Label textLabel = new Label(message.isShowOriginal()
                        ? (message.getOriginalText() != null ? message.getOriginalText() : message.getText())
                        : message.getText());
                textLabel.getStyleClass().add("bubble-text-them");
                textLabel.setStyle("-fx-font-style: italic;");
                textLabel.setWrapText(true);
                textLabel.setMaxWidth(380);
                content.getChildren().addAll(transLabel, textLabel);
            }
        } else {
            if (message.getSourceLanguage() != null) {
                Label translated = new Label("🌐  Traducido de " + message.getSourceLanguage());
                translated.getStyleClass().add("translated-badge");
                content.getChildren().add(translated);
            }

            Label textLabel = new Label(message.isShowOriginal()
                    ? (message.getOriginalText() != null ? message.getOriginalText() : message.getText())
                    : message.getText());
            textLabel.getStyleClass().add("bubble-text-them");
            textLabel.setWrapText(true);
            textLabel.setMaxWidth(380);
            content.getChildren().add(textLabel);
        }
        bubble.getChildren().add(content);

        col.getChildren().add(bubble);

        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        Label time = new Label(message.getTimestamp().format(timeFmt));
        time.getStyleClass().add("bubble-time");
        bottomRow.getChildren().add(time);

        if (message.getOriginalText() != null && !message.getOriginalText().equals(message.getText())) {
            Hyperlink toggle = new Hyperlink(message.isShowOriginal() ? "Ver traducción" : "Ver original (" + message.getSourceLanguage() + ")");
            toggle.getStyleClass().add("toggle-original");
            toggle.setOnAction(e -> {
                ChatProvider.getInstance().toggleShowOriginal(chatId, message.getId());
                // Refresh messages
                updateMessages(ChatProvider.getInstance().getChatById(chatId));
            });
            bottomRow.getChildren().add(toggle);
        }

        col.getChildren().add(bottomRow);
        row.getChildren().addAll(avatar, col);
        return row;
    }

    private HBox buildAudioPlayer(boolean isMe) {
        HBox player = new HBox(10);
        player.setAlignment(Pos.CENTER_LEFT);
        player.setPadding(new Insets(4));

        Label play = new Label("▶");
        play.setFont(Font.font(22));
        play.setStyle("-fx-text-fill: " + (isMe ? "white" : "-color-accent-fg") + ";");

        VBox waveform = new VBox(4);
        waveform.setPrefWidth(120);

        // Fake waveform bar
        HBox bars = new HBox(2);
        for (int i = 0; i < 20; i++) {
            Region bar = new Region();
            bar.setPrefWidth(4);
            bar.setPrefHeight(3 + (int) (Math.random() * 18));
            bar.setStyle("-fx-background-color: " + (isMe ? "rgba(255,255,255,0.6)" : "-color-accent-fg")
                    + "; -fx-background-radius: 2;");
            bars.getChildren().add(bar);
        }

        Label duration = new Label("0:30");
        duration.setFont(Font.font(10));
        duration.setStyle("-fx-text-fill: " + (isMe ? "rgba(255,255,255,0.5)" : "-color-fg-muted") + ";");

        waveform.getChildren().addAll(bars, duration);
        player.getChildren().addAll(play, waveform);
        return player;
    }

    private javafx.scene.Node buildAttachmentNode(String url) {
        if (url == null) return new Label();
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.endsWith(".png") || lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg") || lowerUrl.endsWith(".gif")) {
            try {
                ImageView img = new ImageView(new Image(url, true));
                img.setPreserveRatio(true);
                img.setFitWidth(200);
                return img;
            } catch (Exception e) {
                return new Label("[Imagen no encontrada]");
            }
        } else {
            Hyperlink fileLink = new Hyperlink("📄 Abrir archivo");
            fileLink.setStyle("-fx-text-fill: -color-accent-fg; -fx-font-weight: bold; -fx-underline: true;");
            fileLink.setOnAction(e -> {
                try {
                    java.awt.Desktop.getDesktop().open(new File(java.net.URI.create(url)));
                } catch (Exception ex) {
                    showTooltipNotification("❌ Error al abrir el archivo");
                }
            });
            return fileLink;
        }
    }

    private void showAttachMenu() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo");
        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());

        if (selectedFile != null) {
            String fileUrl = selectedFile.toURI().toString();
            int dbId = ChatProvider.getInstance().sendAttachmentMessage(chatId, fileUrl);
            if (dbId > lastMessageId) {
                lastMessageId = dbId;
            }
            updateMessages(ChatProvider.getInstance().getChatById(chatId));
            showTooltipNotification("✅ Archivo adjuntado: " + selectedFile.getName());
        }
    }

    private void showScheduleInfo() {
        Dialog<LocalDateTime> dialog = new Dialog<>();
        dialog.setTitle("Programar mensaje");
        dialog.setHeaderText("Elige cuándo quieres enviar este mensaje");

        ButtonType scheduleButtonType = new ButtonType("Programar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(scheduleButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());

        HBox timeBox = new HBox(5);
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, LocalTime.now().getHour());
        Spinner<Integer> minSpinner = new Spinner<>(0, 59, LocalTime.now().getMinute());
        hourSpinner.setPrefWidth(70);
        minSpinner.setPrefWidth(70);
        timeBox.getChildren().addAll(new Label("Hora:"), hourSpinner, new Label(":"), minSpinner);
        timeBox.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(new Label("Fecha:"), datePicker, timeBox);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == scheduleButtonType) {
                return LocalDateTime.of(datePicker.getValue(),
                        LocalTime.of(hourSpinner.getValue(), minSpinner.getValue()));
            }
            return null;
        });

        Optional<LocalDateTime> result = dialog.showAndWait();
        result.ifPresent(dateTime -> {
            showTooltipNotification(
                    "⏰ Mensaje programado para: " + dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        });
    }

    private void toggleRecording() {
        if (!audioService.isRecording()) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        if (!AudioRecordingService.isModelAvailable()) {
            showTooltipNotification("⚠️ Modelo Vosk no encontrado. Coloca vosk-model-es en resources/");
            return;
        }
        audioService.startRecording();
        micBtn.setText("⏹");
        micBtn.setStyle("-fx-text-fill: #ef4444;");
        messageField.setDisable(true);
        messageField.setPromptText("🎤 Grabando...");
        showTooltipNotification("🎤 Grabación iniciada...");
    }

    private void stopRecording() {
        micBtn.setText("🎤");
        micBtn.setStyle("");
        messageField.setDisable(false);
        messageField.setPromptText("Escribe en " + getMyLangCode() + "...");
        showTooltipNotification("⏳ Transcribiendo nota de voz...");

        // Run Vosk transcription on a background thread — it can take a moment
        new Thread(() -> {
            String transcription = audioService.stopAndTranscribe();
            String duration = audioService.getLastDuration();
            final String finalText = transcription.isEmpty() ? "[Audio]" : transcription;

            Platform.runLater(() -> {
                showTooltipNotification("✅ Nota de voz enviada (" + duration + ")");
                int dbId = ChatProvider.getInstance().sendAudioMessage(chatId, finalText, duration);
                // Advance the poll cursor so the poller skips this message (prevents duplication)
                if (dbId > lastMessageId) {
                    lastMessageId = dbId;
                }
            });
        }).start();
    }

    private void showTooltipNotification(String msg) {
        Tooltip tooltip = new Tooltip(msg);
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setAutoHide(true);
        tooltip.show(getScene().getWindow(),
                getScene().getWindow().getX() + getScene().getWindow().getWidth() / 2 - 100,
                getScene().getWindow().getY() + 100);
    }

    private void startPolling(Chat chat) {
        // Poll the DB every 2 seconds on a background thread, then update UI on FX thread
        pollTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            final int currentLast = lastMessageId;
            new Thread(() -> {
                int newLast = ChatProvider.getInstance().pollNewMessages(chatId, currentLast);
                if (newLast > currentLast) {
                    lastMessageId = newLast;
                    Platform.runLater(() -> updateMessages(chat));
                }
            }).start();
        }));
        pollTimeline.setCycleCount(Timeline.INDEFINITE);
        pollTimeline.play();

        // Stop polling when screen is removed from the scene graph
        sceneProperty().addListener((obs, o, n) -> {
            if (n == null && pollTimeline != null) pollTimeline.stop();
        });
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }
}
