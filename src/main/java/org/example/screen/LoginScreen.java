package org.example.screen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.example.navigation.NavigationManager;
import org.example.provider.AuthProvider;

public class LoginScreen extends StackPane {

    private final TextField phoneField = new TextField(); // Use for Login
    private final TextField registerPhoneField = new TextField(); // Use for Register
    private final TextField nameField = new TextField();
    private final TextField surnamesField = new TextField();
    private final TextField emailField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final PasswordField confirmPasswordField = new PasswordField();
    private final TextField ageField = new TextField();
    private final ComboBox<String> languageCombo = new ComboBox<>();
    private final Label errorLabel = new Label();
    private boolean isRegistering = false;

    private GridPane registerGrid;
    private VBox loginSection;
    private Label loginTab;
    private Label registerTab;
    private Button loginBtn;
    private VBox demoBox;

    public LoginScreen() {
        getStyleClass().add("login-container");

        VBox card = buildCard();

        StackPane centerWrap = new StackPane(card);
        centerWrap.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(centerWrap);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        getChildren().add(scrollPane);
    }

    private VBox buildCard() {
        VBox card = new VBox(24);
        card.setMaxWidth(420);
        card.setPadding(new Insets(48, 48, 48, 48));
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.TOP_CENTER);

        // Logo
        StackPane logoCircle = new StackPane();
        Circle circle = new Circle(44);
        circle.getStyleClass().add("logo-circle");
        Text logoIcon = new Text("💬");
        logoIcon.setFont(Font.font(36));
        logoCircle.getChildren().addAll(circle, logoIcon);

        // Title
        Label title = new Label("PolyChat");
        title.setFont(Font.font("System", FontWeight.BOLD, 30));
        title.getStyleClass().add("title-label");

        Label subtitle = new Label("Mensajería global sin barreras de idioma");
        subtitle.getStyleClass().add("subtitle-label");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(320);

        // Tabs visual
        HBox tabRow = buildTabRow();

        // --- Login Section ---
        loginSection = new VBox(20);
        VBox phoneOnlySection = buildFieldSection("📞", "Número de teléfono", phoneField, "123 456 7890");
        loginSection.getChildren().add(phoneOnlySection);

        // --- Register Section (GridPane for professional alignment) ---
        registerGrid = new GridPane();
        registerGrid.setHgap(15);
        registerGrid.setVgap(12);
        registerGrid.setMaxWidth(Double.MAX_VALUE);
        registerGrid.setVisible(false);
        registerGrid.setManaged(false);

        // Section 1: Personal
        Label personalHeader = new Label("INFORMACIÓN PERSONAL");
        personalHeader.getStyleClass().add("section-header");
        registerGrid.add(personalHeader, 0, 0, 2, 1);
        GridPane.setMargin(personalHeader, new Insets(5, 0, 5, 0));

        registerGrid.add(buildLabel("👤", "Nombre"), 0, 1);
        registerGrid.add(buildLabel("👤", "Apellidos"), 1, 1);

        nameField.setPromptText("Juan");
        surnamesField.setPromptText("Pérez");
        registerGrid.add(setupField(nameField), 0, 2);
        registerGrid.add(setupField(surnamesField), 1, 2);

        registerGrid.add(buildLabel("📅", "Edad"), 0, 3);
        registerGrid.add(buildLabel("🌐", "Idioma"), 1, 3);

        ageField.setPromptText("18");
        languageCombo.getItems().setAll("es Español", "en English", "fr Français", "de Deutsch");
        languageCombo.setValue("es Español");
        registerGrid.add(setupField(ageField), 0, 4);
        registerGrid.add(setupField(languageCombo), 1, 4);

        // Section 2: Account
        Label accountHeader = new Label("CUENTA Y SEGURIDAD");
        accountHeader.getStyleClass().add("section-header");
        registerGrid.add(accountHeader, 0, 5, 2, 1);
        GridPane.setMargin(accountHeader, new Insets(15, 0, 5, 0));

        registerGrid.add(buildLabel("✉", "Correo electrónico"), 0, 6, 2, 1);
        emailField.setPromptText("juan.perez@ejemplo.com");
        registerGrid.add(setupField(emailField), 0, 7, 2, 1);

        registerGrid.add(buildLabel("📞", "Número de teléfono"), 0, 8, 2, 1);
        registerPhoneField.setPromptText("123 456 7890");
        registerGrid.add(setupField(registerPhoneField), 0, 9, 2, 1);

        registerGrid.add(buildLabel("🔒", "Contraseña"), 0, 10);
        registerGrid.add(buildLabel(null, "Confirmar contraseña"), 1, 10);

        passwordField.setPromptText("••••••••");
        confirmPasswordField.setPromptText("••••••••");
        registerGrid.add(setupField(passwordField), 0, 11);
        registerGrid.add(setupField(confirmPasswordField), 1, 11);

        // Demo info box
        demoBox = buildDemoBox();

        // Error label
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Login button
        loginBtn = new Button("Enviar código de verificación →");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(50);
        loginBtn.getStyleClass().addAll("button", "accent", "login-button");
        loginBtn.setOnAction(e -> handleLogin());

        card.getChildren().addAll(
                logoCircle, title, subtitle,
                new Separator(),
                tabRow,
                loginSection, registerGrid,
                demoBox,
                errorLabel, loginBtn);
        return card;
    }

    private Control setupField(Control field) {
        field.getStyleClass().add("custom-field");
        field.setPrefHeight(46);
        field.setMaxWidth(Double.MAX_VALUE);
        if (field instanceof TextField) {
            ((TextField) field).setMaxWidth(Double.MAX_VALUE);
        }
        return field;
    }

    private VBox buildFieldSection(String icon, String label, Control field, String prompt) {
        VBox section = new VBox(8);
        HBox labelBox = buildLabel(icon, label);
        if (field instanceof TextField) {
            ((TextField) field).setPromptText(prompt);
        }
        field.getStyleClass().add("custom-field");
        field.setPrefHeight(46);
        field.setMaxWidth(Double.MAX_VALUE);
        section.getChildren().addAll(labelBox, field);
        return section;
    }

    private HBox buildLabel(String icon, String text) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        if (icon != null) {
            Label iconLbl = new Label(icon);
            iconLbl.getStyleClass().add("field-icon");
            box.getChildren().add(iconLbl);
        }
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        box.getChildren().add(label);
        return box;
    }

    private HBox buildTabRow() {
        HBox tabRow = new HBox(4);
        tabRow.getStyleClass().add("tab-row");
        tabRow.setPadding(new Insets(4));

        loginTab = new Label("Iniciar Sesión");
        loginTab.getStyleClass().add("tab-active");
        loginTab.setMaxWidth(Double.MAX_VALUE);
        loginTab.setAlignment(Pos.CENTER);
        HBox.setHgrow(loginTab, Priority.ALWAYS);
        loginTab.setPadding(new Insets(8));
        loginTab.setStyle("-fx-cursor: hand;");
        loginTab.setOnMouseClicked(e -> setRegisterMode(false));

        registerTab = new Label("Registrarse");
        registerTab.getStyleClass().add("tab-inactive");
        registerTab.setMaxWidth(Double.MAX_VALUE);
        registerTab.setAlignment(Pos.CENTER);
        HBox.setHgrow(registerTab, Priority.ALWAYS);
        registerTab.setPadding(new Insets(8));
        registerTab.setStyle("-fx-cursor: hand;");
        registerTab.setOnMouseClicked(e -> setRegisterMode(true));

        tabRow.getChildren().addAll(loginTab, registerTab);
        return tabRow;
    }

    private void setRegisterMode(boolean registering) {
        this.isRegistering = registering;
        if (registering) {
            loginTab.getStyleClass().removeAll("tab-active");
            loginTab.getStyleClass().add("tab-inactive");
            registerTab.getStyleClass().removeAll("tab-inactive");
            registerTab.getStyleClass().add("tab-active");

            loginSection.setVisible(false);
            loginSection.setManaged(false);
            registerGrid.setVisible(true);
            registerGrid.setManaged(true);
            demoBox.setVisible(false);
            demoBox.setManaged(false);

            loginBtn.setText("Registrarse →");
        } else {
            registerTab.getStyleClass().removeAll("tab-active");
            registerTab.getStyleClass().add("tab-inactive");
            loginTab.getStyleClass().removeAll("tab-inactive");
            loginTab.getStyleClass().add("tab-active");

            loginSection.setVisible(true);
            loginSection.setManaged(true);
            registerGrid.setVisible(false);
            registerGrid.setManaged(false);
            demoBox.setVisible(true);
            demoBox.setManaged(true);

            loginBtn.setText("Enviar código de verificación →");
        }
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private VBox buildDemoBox() {
        VBox box = new VBox(4);
        box.getStyleClass().add("demo-box");
        box.setPadding(new Insets(16));

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label shield = new Label("🛡️");
        shield.setFont(Font.font(18));

        VBox texts = new VBox(2);
        Label hint1 = new Label("Demo: Tu código llegará pronto a tu dispositivo móvil.");
        hint1.getStyleClass().add("demo-hint");
        texts.getChildren().addAll(hint1);

        row.getChildren().addAll(shield, texts);
        box.getChildren().add(row);
        return box;
    }

    private void handleLogin() {
        if (isRegistering) {
            String phone = registerPhoneField.getText().trim();
            String name = nameField.getText().trim();
            String surnames = surnamesField.getText().trim();
            String email = emailField.getText().trim();
            String pass = passwordField.getText();
            String confirmPass = confirmPasswordField.getText();
            String ageStr = ageField.getText().trim();
            String lang = languageCombo.getValue();

            if (name.isEmpty() || surnames.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                showError("Por favor, completa todos los campos");
                return;
            }
            if (!pass.equals(confirmPass)) {
                showError("Las contraseñas no coinciden");
                return;
            }

            int age = 18;
            try {
                if (!ageStr.isEmpty())
                    age = Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                showError("Edad inválida");
                return;
            }

            if (AuthProvider.getInstance().requestCode(phone, name, surnames, email, age, lang)) {
                NavigationManager.getInstance().navigateGlobal(new VerifyScreen());
            } else {
                showError("Error al procesar el registro");
            }
        } else {
            String phone = phoneField.getText().trim();
            if (AuthProvider.getInstance().requestCode(phone, null, null, null, 0, null)) {
                NavigationManager.getInstance().navigateGlobal(new VerifyScreen());
            } else {
                showError("Ingresa un número de teléfono válido");
            }
        }
    }

    private void showError(String msg) {
        errorLabel.setText("⚠ " + msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

}
