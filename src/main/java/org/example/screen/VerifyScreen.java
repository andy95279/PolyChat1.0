package org.example.screen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.shape.Circle;
import org.example.navigation.NavigationManager;
import org.example.provider.AuthProvider;

public class VerifyScreen extends VBox {

    private final TextField[] codeFields = new TextField[6];
    private final Label errorLabel = new Label();

    public VerifyScreen() {
        setSpacing(0);
        setAlignment(Pos.CENTER);
        getStyleClass().add("login-container");

        VBox card = buildCard();
        getChildren().add(card);

        // Auto-fill logic with a slight delay for better UX
        javafx.application.Platform.runLater(() -> {
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // 1 second delay so the user sees the screen before auto-fill
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                javafx.application.Platform.runLater(() -> {
                    String phone = AuthProvider.getInstance().getPendingPhone();
                    if (phone != null) {
                        String code = org.example.db.VerificationDao.getLatestCode(phone);
                        if (code != null && code.length() >= 6) {
                            for (int i = 0; i < 6; i++) {
                                codeFields[i].setText(String.valueOf(code.charAt(i)));
                            }
                        }
                    }
                });
            }).start();
        });
    }

    private VBox buildCard() {
        VBox card = new VBox(24);
        card.setMaxWidth(420);
        card.setPadding(new Insets(48, 48, 48, 48));
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.TOP_CENTER);

        // Icon
        StackPane iconPane = new StackPane();
        Circle circle = new Circle(44);
        circle.getStyleClass().add("logo-circle");
        Text icon = new Text("🔐");
        icon.setFont(Font.font(36));
        iconPane.getChildren().addAll(circle, icon);

        // Title
        Label title = new Label("Verificar Número");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.getStyleClass().add("title-label");

        Label subtitle = new Label("Ingresa el código de 6 dígitos enviado a tu teléfono");
        subtitle.getStyleClass().add("subtitle-label");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(320);

        // Code fields
        HBox codeRow = new HBox(10);
        codeRow.setAlignment(Pos.CENTER);
        for (int i = 0; i < 6; i++) {
            final int idx = i;
            TextField tf = new TextField();
            tf.setPrefWidth(52);
            tf.setPrefHeight(58);
            tf.setMaxWidth(52);
            tf.getStyleClass().addAll("code-field");
            tf.setAlignment(Pos.CENTER);
            tf.setFont(Font.font("System", FontWeight.BOLD, 22));
            tf.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.length() > 1)
                    tf.setText(newVal.substring(newVal.length() - 1));
                if (tf.getText().length() == 1 && idx < 5) {
                    codeFields[idx + 1].requestFocus();
                }
                if (isCodeComplete())
                    autoVerify();
            });
            codeFields[i] = tf;
            codeRow.getChildren().add(tf);
        }

        // Error
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Verify button
        Button verifyBtn = new Button("Verificar →");
        verifyBtn.setMaxWidth(Double.MAX_VALUE);
        verifyBtn.setPrefHeight(50);
        verifyBtn.getStyleClass().addAll("button", "accent", "login-button");
        verifyBtn.setOnAction(e -> handleVerify());

        // Back and Resend links
        HBox footerLinks = new HBox(20);
        footerLinks.setAlignment(Pos.CENTER);

        Hyperlink resend = new Hyperlink("Reenviar código");
        resend.getStyleClass().add("resend-link");
        resend.setOnAction(e -> {
            if (AuthProvider.getInstance().resendCode()) {
                showError("Código reenviado con éxito", false);
            }
        });

        Hyperlink back = new Hyperlink("← Cambiar número");
        back.getStyleClass().add("back-link");
        back.setOnAction(e -> NavigationManager.getInstance().navigateGlobal(new LoginScreen()));

        footerLinks.getChildren().addAll(back, resend);

        card.getChildren().addAll(iconPane, title, subtitle, codeRow, errorLabel, verifyBtn, footerLinks);
        return card;
    }

    private boolean isCodeComplete() {
        for (TextField tf : codeFields) {
            if (tf.getText().isEmpty())
                return false;
        }
        return true;
    }

    private void autoVerify() {
        handleVerify();
    }

    private void handleVerify() {
        StringBuilder sb = new StringBuilder();
        for (TextField tf : codeFields)
            sb.append(tf.getText());
        String code = sb.toString();
        if (code.length() < 6) {
            showError("Completa los 6 dígitos", true);
            return;
        }

        if (AuthProvider.getInstance().verifyCode(code)) {
            NavigationManager.getInstance().navigateGlobal(new MainAppScreen());
        } else {
            showError("el código de verificación no coincide", true);
            for (TextField tf : codeFields) {
                tf.clear();
                tf.getStyleClass().add("code-field-error");
            }
            codeFields[0].requestFocus();
        }
    }

    private void showError(String msg, boolean isError) {
        errorLabel.setText((isError ? "⚠ " : "✓ ") + msg);
        errorLabel.setStyle("-fx-text-fill: " + (isError ? "-color-danger-fg" : "#4ade80") + ";");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
