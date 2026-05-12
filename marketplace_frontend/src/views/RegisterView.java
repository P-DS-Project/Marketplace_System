package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.UserApiService;

public class RegisterView {

    private final StackPane root;
    private final UserApiService userApi = new UserApiService();

    public RegisterView() {
        root = new StackPane();
        root.getStyleClass().add("auth-container");
        root.getChildren().add(buildCard());
    }

    private VBox buildCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("auth-card");
        card.setMaxWidth(560);
        card.setAlignment(Pos.CENTER);

        Label logo = new Label("\uD83C\uDFEA MarketPlace Pro");
        logo.getStyleClass().add("auth-title");

        Label subtitle = new Label("Create your account");
        subtitle.getStyleClass().add("auth-subtitle");

        Region spacer = new Region();
        spacer.setPrefHeight(4);

        // Username
        Label nameLabel = new Label("Username");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField nameField = new TextField();
        nameField.setPromptText("Your username (min 3 chars)");
        Label nameError = new Label();
        nameError.getStyleClass().add("field-error-label");
        nameError.setManaged(false);
        nameError.setVisible(false);

        nameField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showFieldError(nameField, nameError, "Username is required");
                } else if (name.length() < 3) {
                    showFieldError(nameField, nameError, "Username must be at least 3 characters");
                } else {
                    clearFieldError(nameField, nameError);
                }
            }
        });

        // Email
        Label emailLabel = new Label("Email Address");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField emailField = new TextField();
        emailField.setPromptText("you@example.com");
        Label emailError = new Label();
        emailError.getStyleClass().add("field-error-label");
        emailError.setManaged(false);
        emailError.setVisible(false);

        emailField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                String email = emailField.getText().trim();
                if (email.isEmpty()) {
                    showFieldError(emailField, emailError, "Email is required");
                } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    showFieldError(emailField, emailError, "Enter a valid email address");
                } else {
                    clearFieldError(emailField, emailError);
                }
            }
        });

        // Password
        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Min 8 chars, 1 uppercase, 1 digit, 1 special");
        Label passError = new Label();
        passError.getStyleClass().add("field-error-label");
        passError.setManaged(false);
        passError.setVisible(false);

        passField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validatePassword(passField, passError);
            }
        });

        // Confirm Password
        Label confirmLabel = new Label("Confirm Password");
        confirmLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Re-enter your password");
        Label confirmError = new Label();
        confirmError.getStyleClass().add("field-error-label");
        confirmError.setManaged(false);
        confirmError.setVisible(false);

        confirmField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                String confirm = confirmField.getText();
                String pass = passField.getText();
                if (confirm.isEmpty()) {
                    showFieldError(confirmField, confirmError, "Please confirm your password");
                } else if (!confirm.equals(pass)) {
                    showFieldError(confirmField, confirmError, "Passwords do not match");
                } else {
                    clearFieldError(confirmField, confirmError);
                }
            }
        });

        // General error/success
        Label generalError = new Label();
        generalError.getStyleClass().add("field-error-label");
        generalError.setManaged(false);
        generalError.setVisible(false);
        generalError.setWrapText(true);

        Label successLabel = new Label();
        successLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 13px; -fx-font-weight: bold;");
        successLabel.setManaged(false);
        successLabel.setVisible(false);

        Button registerBtn = new Button("Create Account");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setOnAction(e -> {
            handleRegister(nameField, emailField, passField, confirmField,
                           nameError, emailError, passError, confirmError,
                           generalError, successLabel);
        });

        HBox loginBox = new HBox(4);
        loginBox.setAlignment(Pos.CENTER);
        Label hasAccLabel = new Label("Already have an account?");
        hasAccLabel.getStyleClass().add("auth-subtitle");
        Hyperlink loginLink = new Hyperlink("Sign in");
        loginLink.getStyleClass().add("auth-link");
        loginLink.setOnAction(e -> showLogin());
        loginBox.getChildren().addAll(hasAccLabel, loginLink);

        card.getChildren().addAll(logo, subtitle, spacer,
            nameLabel, nameField, nameError,
            emailLabel, emailField, emailError,
            passLabel, passField, passError,
            confirmLabel, confirmField, confirmError,
            generalError, successLabel, registerBtn, loginBox);

        return card;
    }

    private void handleRegister(TextField nameField, TextField emailField,
                                PasswordField passField, PasswordField confirmField,
                                Label nameError, Label emailError,
                                Label passError, Label confirmError,
                                Label generalError, Label successLabel) {
        boolean valid = true;

        String username = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passField.getText();
        String confirm = confirmField.getText();

        // Validate username
        if (username.isEmpty()) {
            showFieldError(nameField, nameError, "Username is required");
            valid = false;
        } else if (username.length() < 3) {
            showFieldError(nameField, nameError, "Username must be at least 3 characters");
            valid = false;
        } else {
            clearFieldError(nameField, nameError);
        }

        // Validate email
        if (email.isEmpty()) {
            showFieldError(emailField, emailError, "Email is required");
            valid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showFieldError(emailField, emailError, "Enter a valid email address");
            valid = false;
        } else {
            clearFieldError(emailField, emailError);
        }

        // Validate password
        if (!validatePassword(passField, passError)) {
            valid = false;
        }

        // Validate confirm password
        if (confirm.isEmpty()) {
            showFieldError(confirmField, confirmError, "Please confirm your password");
            valid = false;
        } else if (!confirm.equals(password)) {
            showFieldError(confirmField, confirmError, "Passwords do not match");
            valid = false;
        } else {
            clearFieldError(confirmField, confirmError);
        }

        if (!valid) return;

        generalError.setVisible(false);
        generalError.setManaged(false);
        successLabel.setVisible(false);
        successLabel.setManaged(false);

        String result = userApi.register(username, email, password, "USER");
        if (result != null && !result.startsWith("ERROR")) {
            successLabel.setText("\u2705 Account created! You can now sign in.");
            successLabel.setVisible(true);
            successLabel.setManaged(true);
        } else {
            generalError.setText(result != null ? result.replace("ERROR: ", "") : "Registration failed. Please try again.");
            generalError.setVisible(true);
            generalError.setManaged(true);
        }
    }

    private boolean validatePassword(PasswordField passField, Label passError) {
        String pass = passField.getText();
        if (pass.isEmpty()) {
            showFieldError(passField, passError, "Password is required");
            return false;
        } else if (pass.length() < 8) {
            showFieldError(passField, passError, "Password must be at least 8 characters");
            return false;
        } else if (!pass.matches(".*[A-Z].*")) {
            showFieldError(passField, passError, "Password must contain at least 1 uppercase letter");
            return false;
        } else if (!pass.matches(".*[0-9].*")) {
            showFieldError(passField, passError, "Password must contain at least 1 digit");
            return false;
        } else if (!pass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            showFieldError(passField, passError, "Password must contain at least 1 special character");
            return false;
        } else {
            clearFieldError(passField, passError);
            return true;
        }
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

    private void showLogin() {
        LoginView loginView = new LoginView();
        root.getScene().setRoot(loginView.getRoot());
    }

    public StackPane getRoot() {
        return root;
    }
}
