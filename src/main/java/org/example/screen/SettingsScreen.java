package org.example.screen;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.model.User;
import org.example.navigation.NavigationManager;
import org.example.provider.AuthProvider;
import org.example.provider.SettingsProvider;

public class SettingsScreen extends VBox {

    private final MainAppScreen parent;
    private final SettingsProvider settings = SettingsProvider.getInstance();

    public SettingsScreen(MainAppScreen parent) {
        this.parent = parent;
        setFillWidth(true);
        setAlignment(Pos.TOP_CENTER);
        getStyleClass().add("settings-screen");

        VBox panel = buildPanel();
        VBox.setVgrow(panel, Priority.ALWAYS);
        getChildren().add(panel);
    }

    private VBox buildPanel() {
        VBox panel = new VBox(0);
        panel.setMaxWidth(580);
        panel.setMinWidth(400);
        panel.setFillWidth(true);
        VBox.setVgrow(panel, Priority.ALWAYS);

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(20, 20, 12, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("settings-header");

        Label title = new Label("⚙  Ajustes");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.getStyleClass().add("settings-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("close-btn");
        closeBtn.setOnAction(e -> parent.showWelcome());

        header.getChildren().addAll(title, spacer, closeBtn);

        // TabPane
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("settings-tabs");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab profileTab = new Tab("👤  Perfil", buildProfileContent());
        Tab appearanceTab = new Tab("🎨  Apariencia", buildAppearanceContent());
        tabPane.getTabs().addAll(profileTab, appearanceTab);

        panel.getChildren().addAll(header, new Separator(), tabPane);
        return panel;
    }

    private ScrollPane buildProfileContent() {
        User user = AuthProvider.getInstance().getCurrentUser();
        if (user == null)
            user = new org.example.model.User("1", "Usuario", "", "usuario@email.com", "+34 000", "Español", 25);

        VBox form = new VBox(20);
        form.setPadding(new Insets(28, 32, 28, 32));
        form.setFillWidth(true);

        // Avatar circle placeholder
        StackPane avatarSection = new StackPane();
        avatarSection.setAlignment(Pos.CENTER);
        javafx.scene.shape.Circle avatar = new javafx.scene.shape.Circle(44);
        avatar.setFill(Color.web("#3B82F6"));
        Label initials = new Label(user.getName().substring(0, Math.min(2, user.getName().length())).toUpperCase());
        initials.setFont(Font.font("System", FontWeight.BOLD, 22));
        initials.setStyle("-fx-text-fill: white;");
        avatarSection.getChildren().addAll(avatar, initials);
        VBox.setMargin(avatarSection, new Insets(0, 0, 8, 0));

        // Form fields - each field wrapped in labeled VBox
        final User finalUser = user;
        TextField nameField = new TextField(finalUser.getName());
        TextField emailField = new TextField(finalUser.getEmail());
        TextField phoneField = new TextField(finalUser.getPhone());
        TextField ageField = new TextField(String.valueOf(finalUser.getAge()));
        styleTextField(nameField);
        styleTextField(emailField);
        styleTextField(phoneField);
        styleTextField(ageField);

        Label langLabel = new Label("Idioma");
        langLabel.getStyleClass().add("field-label");
        ComboBox<String> langCombo = new ComboBox<>();
        langCombo.getItems().addAll("Español", "English", "Français", "Deutsch", "Italiano", "日本語");
        langCombo.setValue(finalUser.getLanguage() != null ? finalUser.getLanguage() : "Español");
        langCombo.setMaxWidth(Double.MAX_VALUE);
        langCombo.getStyleClass().add("custom-combo");

        // VCard QR Code
        VBox qrBox = new VBox(8);
        qrBox.setAlignment(Pos.CENTER);
        Label qrLabel = new Label("Tu código QR para agregar como contacto:");
        qrLabel.getStyleClass().add("field-label");

        javafx.scene.image.ImageView qrView = new javafx.scene.image.ImageView();
        qrView.setFitWidth(150);
        qrView.setFitHeight(150);
        qrBox.getChildren().addAll(qrLabel, qrView);

        Runnable updateQR = () -> {
            String surname = finalUser.getSurnames() != null ? finalUser.getSurnames() : "";
            String vcard = "BEGIN:VCARD\nVERSION:3.0\n" +
                    "N:" + surname + ";" + nameField.getText().trim() + "\n" +
                    "FN:" + nameField.getText().trim() + " " + surname + "\n" +
                    "TEL;TYPE=CELL:" + phoneField.getText().trim() + "\n" +
                    "EMAIL:" + emailField.getText().trim() + "\n" +
                    "END:VCARD";
            try {
                String encodedVcard = java.net.URLEncoder.encode(vcard, "UTF-8");
                String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + encodedVcard;
                qrView.setImage(new javafx.scene.image.Image(qrUrl, true));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };
        updateQR.run(); // Initial generation

        // Save button
        Button saveBtn = new Button("Guardar cambios");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setPrefHeight(46);
        saveBtn.getStyleClass().addAll("button", "accent");
        saveBtn.setOnAction(e -> {
            try {
                int age = Integer.parseInt(ageField.getText().trim());
                AuthProvider.getInstance().updateUser(
                        nameField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        langCombo.getValue(),
                        age);
                initials.setText(
                        nameField.getText().substring(0, Math.min(2, nameField.getText().length())).toUpperCase());
                updateQR.run();
                showSaveSuccess(saveBtn);
            } catch (NumberFormatException ex) {
                saveBtn.setText("⚠ Edad inválida");
            }
        });

        form.getChildren().addAll(
                avatarSection,
                buildLabeledField("Nombre", nameField),
                buildLabeledField("Correo", emailField),
                buildLabeledField("Teléfono", phoneField),
                new VBox(6, new Label("Idioma") {
                    {
                        getStyleClass().add("field-label");
                    }
                }, langCombo),
                buildLabeledField("Edad", ageField),
                qrBox,
                saveBtn);

        // Logout button
        Button logoutBtn = new Button("Cerrar sesión");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setPrefHeight(46);
        logoutBtn.getStyleClass().addAll("button", "danger");
        logoutBtn.setOnAction(e -> {
            AuthProvider.getInstance().logout();
            // Navigate back to login globally
            NavigationManager.getInstance().navigateGlobal(new LoginScreen());
        });

        VBox fullForm = new VBox(12, form, new Separator(), new VBox(16, logoutBtn));
        ((VBox) fullForm.getChildren().get(2)).setPadding(new Insets(12, 32, 24, 32));

        ScrollPane scroll = new ScrollPane(fullForm);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("settings-scroll");
        return scroll;
    }

    private void styleTextField(TextField field) {
        field.getStyleClass().add("custom-field");
        field.setPrefHeight(42);
        field.setMaxWidth(Double.MAX_VALUE);
    }

    private VBox buildLabeledField(String labelText, TextField field) {
        VBox box = new VBox(6);
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        box.getChildren().addAll(label, field);
        return box;
    }

    private void showSaveSuccess(Button btn) {
        String original = btn.getText();
        btn.setText("✓ Guardado correctamente");
        btn.setDisable(true);
        new javafx.animation.Timeline(new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(2),
                e -> {
                    btn.setText(original);
                    btn.setDisable(false);
                })).play();
    }

    private ScrollPane buildAppearanceContent() {
        VBox content = new VBox(28);
        content.setPadding(new Insets(28, 32, 28, 32));

        // Theme selector
        Label themeLabel = new Label("Tema de la aplicación");
        themeLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        themeLabel.getStyleClass().add("settings-section-label");

        HBox themeRow = new HBox(12);
        themeRow.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup themeGroup = new ToggleGroup();
        ToggleButton darkBtn = new ToggleButton("🌙  Oscuro");
        darkBtn.getStyleClass().addAll("theme-toggle");
        darkBtn.setToggleGroup(themeGroup);
        darkBtn.setSelected("dark".equals(settings.getTheme()));

        ToggleButton lightBtn = new ToggleButton("☀  Claro");
        lightBtn.getStyleClass().addAll("theme-toggle");
        lightBtn.setToggleGroup(themeGroup);
        lightBtn.setSelected("light".equals(settings.getTheme()));

        darkBtn.setOnAction(e -> applyTheme("dark"));
        lightBtn.setOnAction(e -> applyTheme("light"));

        themeRow.getChildren().addAll(darkBtn, lightBtn);

        // Background selector
        Label bgLabel = new Label("Fondo del chat");
        bgLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        bgLabel.getStyleClass().add("settings-section-label");

        Label bgSub = new Label("Selecciona el patrón de fondo para la ventana de chat");
        bgSub.getStyleClass().add("subtitle-label");

        HBox bgRow = new HBox(12);
        bgRow.setAlignment(Pos.CENTER_LEFT);

        String[] bgNames = { "Sin fondo", "Burbujas", "Diagonal", "Cuadrícula", "Ondas" };
        String[] bgColors = { "#1e1e1e", "#1a2a3a", "#1a1a2a", "#0d1a0d", "#1a0d2a" };

        for (int i = 0; i < bgNames.length; i++) {
            final int index = i;
            VBox bgOpt = new VBox(8);
            bgOpt.setAlignment(Pos.TOP_CENTER);
            bgOpt.setStyle("-fx-cursor: hand;");

            Rectangle preview = new Rectangle(70, 50);
            preview.setArcWidth(10);
            preview.setArcHeight(10);
            preview.setFill(Color.web(bgColors[i]));
            preview.setStroke(index == settings.getChatBackgroundIndex()
                    ? Color.web("#3B82F6")
                    : Color.web("#333"));
            preview.setStrokeWidth(index == settings.getChatBackgroundIndex() ? 2.5 : 1);

            Label bgName = new Label(bgNames[i]);
            bgName.getStyleClass().add("bg-option-label");

            bgOpt.getChildren().addAll(preview, bgName);
            bgOpt.setOnMouseClicked(e -> {
                settings.setChatBackgroundIndex(index);
                // Refresh strokes
                bgRow.getChildren().forEach(child -> {
                    if (child instanceof VBox && !((VBox) child).getChildren().isEmpty()) {
                        VBox vb = (VBox) child;
                        if (vb.getChildren().get(0) instanceof Rectangle) {
                            Rectangle r = (Rectangle) vb.getChildren().get(0);
                            r.setStroke(Color.web("#333"));
                            r.setStrokeWidth(1);
                        }
                    }
                });
                preview.setStroke(Color.web("#3B82F6"));
                preview.setStrokeWidth(2.5);
            });

            bgRow.getChildren().add(bgOpt);
        }

        // Colorblind selector
        Label cbLabel = new Label("Modo Accesibilidad (Daltonismo)");
        cbLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        cbLabel.getStyleClass().add("settings-section-label");

        ComboBox<String> cbCombo = new ComboBox<>();
        cbCombo.getItems().addAll("Desactivado", "Protanopia / Deuteranopia", "Tritanopia", "Monocromatismo");
        // Map saved value to string
        String currentCb = settings.getColorblindMode();
        switch (currentCb) {
            case "protanopia":
                cbCombo.setValue("Protanopia / Deuteranopia");
                break;
            case "tritanopia":
                cbCombo.setValue("Tritanopia");
                break;
            case "monochromacy":
                cbCombo.setValue("Monocromatismo");
                break;
            default:
                cbCombo.setValue("Desactivado");
                break;
        }
        cbCombo.setOnAction(e -> {
            String val = cbCombo.getValue();
            if ("Protanopia / Deuteranopia".equals(val)) settings.setColorblindMode("protanopia");
            else if ("Tritanopia".equals(val)) settings.setColorblindMode("tritanopia");
            else if ("Monocromatismo".equals(val)) settings.setColorblindMode("monochromacy");
            else settings.setColorblindMode("none");
            
            applyTheme(settings.getTheme());
        });
        cbCombo.setMaxWidth(Double.MAX_VALUE);
        cbCombo.setPrefHeight(42);
        cbCombo.getStyleClass().add("custom-combo");

        VBox cbRow = new VBox(cbCombo);
        cbRow.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(
                themeLabel, themeRow,
                new Separator(),
                cbLabel, cbRow,
                new Separator(),
                bgLabel, bgSub, bgRow);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    private void applyTheme(String theme) {
        settings.setTheme(theme);
        if ("dark".equals(theme)) {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        }
        // Re-apply our custom CSS
        if (getScene() != null) {
            getScene().getStylesheets().removeIf(s -> s.endsWith("styles.css") || s.endsWith("protanopia.css") || s.endsWith("tritanopia.css"));
            getScene().getStylesheets().add(getClass().getResource("/org/example/styles.css").toExternalForm());
            
            String cbMode = settings.getColorblindMode();
            if ("protanopia".equals(cbMode)) {
                getScene().getStylesheets().add(getClass().getResource("/org/example/protanopia.css").toExternalForm());
            } else if ("tritanopia".equals(cbMode)) {
                getScene().getStylesheets().add(getClass().getResource("/org/example/tritanopia.css").toExternalForm());
            }
            
            if ("monochromacy".equals(cbMode)) {
                javafx.scene.effect.ColorAdjust mono = new javafx.scene.effect.ColorAdjust();
                mono.setSaturation(-1.0);
                getScene().getRoot().setEffect(mono);
            } else {
                getScene().getRoot().setEffect(null);
            }
        }
    }
}
