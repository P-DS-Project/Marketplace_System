package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.UserApiService;
import utils.AlertHelper;

public class RegisterView {

    private final StackPane root;
    private final UserApiService userApi = new UserApiService();

    public RegisterView() {
        root = new StackPane();
        root.getStyleClass().add("auth-container");
        root.getChildren().add(buildCard());
    }

    private VBox buildCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("auth-card");
        card.setMaxWidth(440);
        card.setAlignment(Pos.CENTER);

        Label logo = new Label("\uD83C\uDFEA MarketPlace Pro");
        logo.getStyleClass().add("auth-title");

        Label subtitle = new Label("Create your account");
        subtitle.getStyleClass().add("auth-subtitle");

        Label nameLabel = new Label("Username");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField nameField = new TextField();
        nameField.setPromptText("Your username (min 3 chars)");

        Label emailLabel = new Label("Email Address");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField emailField = new TextField();
        emailField.setPromptText("you@example.com");

        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Min 8 characters");

        Label roleLabel = new Label("Account Type");
        roleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("buyer", "seller");
        roleCombo.setValue("buyer");
        roleCombo.setMaxWidth(Double.MAX_VALUE);

        Button registerBtn = new Button("Create Account");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setOnAction(e -> {
            String result = handleRegister(nameField.getText(), emailField.getText(),
                    passField.getText(), roleCombo.getValue());
            if (result != null && !result.startsWith("ERROR")) {
                AlertHelper.showSuccess("Account created! You can now sign in.");
                showLogin();
            } else {
                AlertHelper.showError("Registration Failed", result != null ? result : "Unknown error");
            }
        });

        HBox loginBox = new HBox(4);
        loginBox.setAlignment(Pos.CENTER);
        Label hasAccLabel = new Label("Already have an account?");
        hasAccLabel.getStyleClass().add("auth-subtitle");
        Hyperlink loginLink = new Hyperlink("Sign in");
        loginLink.getStyleClass().add("auth-link");
        loginLink.setOnAction(e -> showLogin());
        loginBox.getChildren().addAll(hasAccLabel, loginLink);

        Region spacer = new Region();
        spacer.setPrefHeight(4);

        card.getChildren().addAll(logo, subtitle, spacer,
            nameLabel, nameField, emailLabel, emailField,
            passLabel, passField, roleLabel, roleCombo,
            registerBtn, loginBox);

        return card;
    }

    private String handleRegister(String username, String email, String password, String role) {
        if (username == null || username.trim().length() < 3) {
            return "ERROR: Username must be at least 3 characters.";
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            return "ERROR: Please enter a valid email address.";
        }
        if (password == null || password.length() < 8) {
            return "ERROR: Password must be at least 8 characters.";
        }
        return userApi.register(username.trim(), email.trim(), password, role);
    }

    private void showLogin() {
        LoginView loginView = new LoginView();
        root.getScene().setRoot(loginView.getRoot());
    }

    public StackPane getRoot() {
        return root;
    }
}
