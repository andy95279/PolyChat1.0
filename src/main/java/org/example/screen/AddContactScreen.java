package org.example.screen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.model.Contact;
import org.example.provider.ContactProvider;

import java.util.List;

public class AddContactScreen extends VBox {

    private final VBox resultsContainer = new VBox(10);
    private final TextField searchField = new TextField();

    public AddContactScreen(MainAppScreen parent) {
        setPadding(new Insets(30, 40, 30, 40));
        setSpacing(20);
        getStyleClass().add("add-contact-screen");

        Label title = new Label("Añadir Contacto");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.getStyleClass().add("screen-title");

        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchField.setPromptText("Buscar por nombre...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add("search-field");
        
        Button searchBtn = new Button("Buscar");
        searchBtn.getStyleClass().add("primary-btn");
        searchBtn.setOnAction(e -> performSearch());

        searchBar.getChildren().addAll(searchField, searchBtn);

        ScrollPane scroll = new ScrollPane(resultsContainer);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("results-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(title, searchBar, new Label("Sugerencias / Resultados:"), scroll);
        
        // Initial load or recommendations could go here
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        resultsContainer.getChildren().clear();
        List<Contact> users = ContactProvider.getInstance().searchUsers(query);

        if (users.isEmpty()) {
            resultsContainer.getChildren().add(new Label("No se encontraron usuarios."));
        } else {
            for (Contact u : users) {
                resultsContainer.getChildren().add(buildUserRow(u));
            }
        }
    }

    private HBox buildUserRow(Contact user) {
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
        name.setFont(Font.font("System", FontWeight.BOLD, 14));
        Label lang = new Label(user.getLanguage());
        lang.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        info.getChildren().addAll(name, lang);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Añadir Amigo");
        addBtn.getStyleClass().add("secondary-btn");
        addBtn.setOnAction(e -> {
            org.example.provider.ContactProvider.getInstance().addContact(user.getId());
            addBtn.setText("Añadido");
            addBtn.setDisable(true);
        });

        row.getChildren().addAll(avatarStack, info, spacer, addBtn);
        return row;
    }
}
