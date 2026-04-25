package org.example;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.db.DatabaseManager;
import org.example.db.DatabaseSeeder;
import org.example.navigation.NavigationManager;
import org.example.screen.LoginScreen;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();
        DatabaseManager.connect();
        org.example.db.SettingsDao.initializeSettingsTable();
        org.example.db.StoryDao.initializeStoriesTable();
        DatabaseSeeder.seedIfNeeded();
    }

    @Override
    public void stop() throws Exception {
        DatabaseManager.disconnect();
        super.stop();
    }

    @Override
    public void start(Stage stage) {
        // Apply AtlantaFX PrimerDark theme
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        // Start with LoginScreen inside a StackPane for NavigationManager
        LoginScreen loginScreen = new LoginScreen();

        StackPane rootPane = new StackPane(loginScreen);
        rootPane.getStyleClass().add("root");

        // Register root for navigation
        NavigationManager.getInstance().setMainContentArea(rootPane);

        Scene scene = new Scene(rootPane, 1100, 700);

        // Load custom CSS stylesheet
        String css = getClass().getResource("/org/example/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        // Apply saved theme and colorblind settings
        org.example.provider.SettingsProvider settings = org.example.provider.SettingsProvider.getInstance();
        if ("light".equals(settings.getTheme())) {
            Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerLight().getUserAgentStylesheet());
        }
        String cbMode = settings.getColorblindMode();
        if ("protanopia".equals(cbMode)) {
            scene.getStylesheets().add(getClass().getResource("/org/example/protanopia.css").toExternalForm());
        } else if ("tritanopia".equals(cbMode)) {
            scene.getStylesheets().add(getClass().getResource("/org/example/tritanopia.css").toExternalForm());
        }
        if ("monochromacy".equals(cbMode)) {
            javafx.scene.effect.ColorAdjust mono = new javafx.scene.effect.ColorAdjust();
            mono.setSaturation(-1.0);
            rootPane.setEffect(mono);
        }

        stage.setTitle("PolyChat Desktop");
        stage.setScene(scene);
        stage.setMinWidth(780);
        stage.setMinHeight(560);

        // Center on screen
        stage.centerOnScreen();
        stage.show();
    }
}