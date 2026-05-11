package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import services.UserApiService;
import state.SessionManager;
import models.User;
import models.Account;
import utils.AlertHelper;
import org.json.JSONObject;

public class LoginView {

    private final StackPane root;
    private final UserApiService userApi = new UserApiService();

    public LoginView() {
        root = new StackPane();
        root.getStyleClass().add("auth-container");
        root.getChildren().add(buildCard());
    }

    private VBox buildCard() {
        VBox card = new VBox(20);
        card.getStyleClass().add("auth-card");
        card.setMaxWidth(440);
        card.setAlignment(Pos.CENTER);

        // Logo
        Label logo = new Label("\uD83C\uDFEA MarketPlace Pro");
        logo.getStyleClass().add("auth-title");
        logo.setTextAlignment(TextAlignment.CENTER);

        Label subtitle = new Label("Sign in to your account");
        subtitle.getStyleClass().add("auth-subtitle");

        // Email
        Label emailLabel = new Label("Email Address");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField emailField = new TextField();
        emailField.setPromptText("you@example.com");
        emailField.setId("login-email");

        // Password
        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");
        passField.setId("login-password");

        // Login button
        Button loginBtn = new Button("Sign In");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setId("login-btn");
        loginBtn.setOnAction(e -> handleLogin(emailField.getText(), passField.getText()));

        // Enter key triggers login
        passField.setOnAction(e -> handleLogin(emailField.getText(), passField.getText()));

        // Register link
        HBox registerBox = new HBox(4);
        registerBox.setAlignment(Pos.CENTER);
        Label noAccLabel = new Label("Don't have an account?");
        noAccLabel.getStyleClass().add("auth-subtitle");
        Hyperlink regLink = new Hyperlink("Create one");
        regLink.getStyleClass().add("auth-link");
        regLink.setOnAction(e -> showRegister());
        registerBox.getChildren().addAll(noAccLabel, regLink);

        // Theme toggle at bottom
        Button themeBtn = new Button("\uD83C\uDF19 Toggle Theme");
        themeBtn.getStyleClass().addAll("button", "theme-toggle");
        themeBtn.setOnAction(e -> {
            utils.ThemeManager.toggleTheme(root.getScene());
        });

        Region spacer = new Region();
        spacer.setPrefHeight(8);

        card.getChildren().addAll(logo, subtitle, spacer,
            emailLabel, emailField, passLabel, passField,
            loginBtn, registerBox, themeBtn);

        return card;
    }

    private void handleLogin(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            AlertHelper.showError("Validation Error", "Please enter both email and password.");
            return;
        }

        String token = userApi.login(email.trim(), password.trim());
        if (token == null) {
            AlertHelper.showError("Login Failed", "Invalid email or password. Please try again.");
            return;
        }

        // Get user info
        JSONObject info = userApi.getInfo(token);
        if (info == null) {
            AlertHelper.showError("Error", "Failed to retrieve account information.");
            return;
        }

        User user = userApi.parseUser(info);
        Account account = userApi.parseAccount(info);

        SessionManager sm = SessionManager.getInstance();
        sm.setToken(token);
        sm.setCurrentUser(user);
        sm.setAccount(account);

        NavigationController.showMainApp();
    }

    private void showRegister() {
        RegisterView regView = new RegisterView();
        root.getScene().setRoot(regView.getRoot());
    }

    public StackPane getRoot() {
        return root;
    }
}
