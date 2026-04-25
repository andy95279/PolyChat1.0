package org.example.navigation;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class NavigationManager {
    private static NavigationManager instance;
    private StackPane mainContentArea; // Top-level container (for Login/MainApp switch)
    private StackPane localContentArea; // Inner container (for Chat/Settings switch)

    private NavigationManager() {
    }

    public static NavigationManager getInstance() {
        if (instance == null)
            instance = new NavigationManager();
        return instance;
    }

    public void setMainContentArea(StackPane pane) {
        this.mainContentArea = pane;
    }

    public void setLocalContentArea(StackPane pane) {
        this.localContentArea = pane;
    }

    /**
     * Navigates globally (replaces everything in the main window)
     */
    public void navigateGlobal(Node view) {
        localContentArea = null; // Clear local area when navigating globally
        performNavigation(mainContentArea, view);
    }

    /**
     * Navigates locally (replaces only the right panel content)
     */
    public void navigate(Node view) {
        if (localContentArea != null) {
            performNavigation(localContentArea, view);
        } else {
            navigateGlobal(view);
        }
    }

    private void performNavigation(StackPane target, Node view) {
        if (target == null)
            return;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(120), target);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            target.getChildren().setAll(view);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), target);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }
}
