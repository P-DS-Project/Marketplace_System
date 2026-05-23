package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import services.UserApiService;
import state.SessionManager;
import models.User;
import models.Account;
import org.json.JSONObject;

public class LoginView {

    private final StackPane root;
    private final UserApiService userApi = new UserApiService();
    private boolean navigating = false;

    public LoginView() {
        root = new StackPane();
        root.getStyleClass().add("auth-container");

        VBox card = buildCard();

        // Entrance animation — matches example style
        card.setOpacity(0);
        card.setTranslateY(30);
        root.getChildren().add(card);

        javafx.application.Platform.runLater(() -> {
            FadeTransition fade = new FadeTransition(Duration.millis(600), card);
            fade.setFromValue(0);
            fade.setToValue(1);
            TranslateTransition slide = new TranslateTransition(Duration.millis(600), card);
            slide.setFromY(30);
            slide.setToY(0);
            fade.play();
            slide.play();
        });
    }

    private VBox buildCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("auth-card");
        card.setMaxWidth(560);
        card.setAlignment(Pos.CENTER);

        Label logo = new Label("\uD83C\uDFEA MarketPlace Pro");
        logo.getStyleClass().add("auth-title");
        logo.setTextAlignment(TextAlignment.CENTER);

        Label subtitle = new Label("Sign in to your account");
        subtitle.getStyleClass().add("auth-subtitle");

        Region spacer = new Region();
        spacer.setPrefHeight(8);

        // Email field row
        Label emailLabel = new Label("Email Address");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField emailField = new TextField();
        emailField.setPromptText("you@example.com");
        emailField.setId("login-email");
        Label emailError = new Label();
        emailError.getStyleClass().add("field-error-label");
        emailError.setManaged(false);
        emailError.setVisible(false);

        emailField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                javafx.application.Platform.runLater(() -> {
                    if (navigating) return;
                    String email = emailField.getText().trim();
                    if (email.isEmpty()) {
                        showFieldError(emailField, emailError, "Email is required");
                    } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                        showFieldError(emailField, emailError, "Enter a valid email address");
                    } else {
                        clearFieldError(emailField, emailError);
                    }
                });
            }
        });

        // Password field row
        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");
        passField.setId("login-password");
        Label passError = new Label();
        passError.getStyleClass().add("field-error-label");
        passError.setManaged(false);
        passError.setVisible(false);

        passField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                javafx.application.Platform.runLater(() -> {
                    if (navigating) return;
                    String pass = passField.getText();
                    if (pass.isEmpty()) {
                        showFieldError(passField, passError, "Password is required");
                    } else if (pass.length() < 8) {
                        showFieldError(passField, passError, "Password must be at least 8 characters");
                    } else {
                        clearFieldError(passField, passError);
                    }
                });
            }
        });

        // General error label
        Label generalError = new Label();
        generalError.getStyleClass().add("field-error-label");
        generalError.setManaged(false);
        generalError.setVisible(false);
        generalError.setWrapText(true);
        generalError.setStyle("-fx-font-size: 13px; -fx-text-fill: #EF4444; -fx-padding: 4 0 0 0;");

        // Login button
        Button loginBtn = new Button("Sign In");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setId("login-btn");
        loginBtn.setOnAction(e -> handleLogin(emailField, passField, emailError, passError, generalError));

        passField.setOnAction(e -> handleLogin(emailField, passField, emailError, passError, generalError));

        // Register link
        HBox registerBox = new HBox(4);
        registerBox.setAlignment(Pos.CENTER);
        Label noAccLabel = new Label("Don't have an account?");
        noAccLabel.getStyleClass().add("auth-subtitle");
        Hyperlink regLink = new Hyperlink("Create one");
        regLink.getStyleClass().add("auth-link");
        regLink.setOnMousePressed(e -> {
            navigating = true;
            showRegister();
        });
        registerBox.getChildren().addAll(noAccLabel, regLink);

        card.getChildren().addAll(logo, subtitle, spacer,
            emailLabel, emailField, emailError,
            passLabel, passField, passError,
            generalError, loginBtn, registerBox);

        return card;
    }

    private void handleLogin(TextField emailField, PasswordField passField,
                             Label emailError, Label passError, Label generalError) {
        String email = emailField.getText().trim();
        String password = passField.getText();

        boolean valid = true;

        if (email.isEmpty()) {
            showFieldError(emailField, emailError, "Email is required");
            valid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showFieldError(emailField, emailError, "Enter a valid email address");
            valid = false;
        } else {
            clearFieldError(emailField, emailError);
        }

        if (password.isEmpty()) {
            showFieldError(passField, passError, "Password is required");
            valid = false;
        } else if (password.length() < 8) {
            showFieldError(passField, passError, "Password must be at least 8 characters");
            valid = false;
        } else {
            clearFieldError(passField, passError);
        }

        if (!valid) return;

        generalError.setVisible(false);
        generalError.setManaged(false);

        String token = userApi.login(email, password);
        if (token == null) {
            generalError.setText("Invalid email or password. Please try again.");
            generalError.setVisible(true);
            generalError.setManaged(true);
            return;
        }

        JSONObject info = userApi.getInfo(token);
        if (info == null) {
            generalError.setText("Failed to retrieve account information.");
            generalError.setVisible(true);
            generalError.setManaged(true);
            return;
        }

        User user = userApi.parseUser(info);
        Account account = userApi.parseAccount(info);

        SessionManager sm = SessionManager.getInstance();
        sm.setToken(token);
        sm.setCurrentUser(user);
        sm.setAccount(account);
        sm.markLoginTime();
        sm.saveSession();

        NavigationController.showMainApp();
    }

    private void showFieldError(Control field, Label errorLabel, String message) {
        field.getStyleClass().removeAll("field-valid", "field-invalid");
        field.getStyleClass().add("field-invalid");
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearFieldError(Control field, Label errorLabel) {
        field.getStyleClass().removeAll("field-valid", "field-invalid");
        field.getStyleClass().add("field-valid");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void showRegister() {
        RegisterView regView = new RegisterView();
        javafx.scene.Parent regRoot = regView.getRoot();
        regRoot.setOpacity(0.8);
        regRoot.setTranslateY(5);
        root.getScene().setRoot(regRoot);
        
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.millis(300), regRoot);
        fade.setToValue(1.0);
        fade.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        javafx.animation.TranslateTransition slide = new javafx.animation.TranslateTransition(Duration.millis(300), regRoot);
        slide.setToY(0);
        slide.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(fade, slide);
        pt.play();
    }

    public StackPane getRoot() {
        return root;
    }
}
