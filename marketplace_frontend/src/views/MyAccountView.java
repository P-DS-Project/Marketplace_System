package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.User;
import models.Account;
import models.Transaction;
import services.UserApiService;
import state.SessionManager;
import utils.AlertHelper;
import org.json.JSONObject;
import java.util.List;

public class MyAccountView {

    private final ScrollPane root;
    private final MainLayout layout;
    private final UserApiService userApi = new UserApiService();

    public MyAccountView(MainLayout layout) {
        this.layout = layout;
        VBox content = new VBox(24);
        content.setPadding(new Insets(0));

        Label title = new Label("\uD83D\uDC64 My Account");
        title.getStyleClass().add("heading");

        User user = SessionManager.getInstance().getCurrentUser();
        Account account = SessionManager.getInstance().getAccount();

        HBox mainRow = new HBox(24);

        // Left column: Profile + Account
        VBox leftCol = new VBox(20);
        leftCol.setPrefWidth(400);
        leftCol.setMinWidth(350);

        // Profile card
        VBox profileCard = new VBox(16);
        profileCard.getStyleClass().add("card");

        Label profileTitle = new Label("Profile Information");
        profileTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Avatar placeholder
        StackPane avatarHolder = new StackPane();
        avatarHolder.setPrefSize(80, 80);
        avatarHolder.setMaxSize(80, 80);
        avatarHolder.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #DBEAFE, #E0E7FF); -fx-background-radius: 40;");
        Label avatarIcon = new Label("\uD83D\uDC64");
        avatarIcon.setStyle("-fx-font-size: 36px;");
        avatarHolder.getChildren().add(avatarIcon);

        Label usernameLabel = new Label(user != null ? user.getUsername() : "Unknown");
        usernameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label emailLabel = new Label(user != null ? user.getEmail() : "");
        emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        Label roleLabel = new Label("Role: " + (user != null ? user.getRole().toUpperCase() : "N/A"));
        roleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");

        profileCard.getChildren().addAll(profileTitle, avatarHolder, usernameLabel, emailLabel, roleLabel);

        // Account balance card
        VBox balanceCard = new VBox(16);
        balanceCard.getStyleClass().add("card");

        Label balanceTitle = new Label("Account Balance");
        balanceTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        String balanceStr = account != null ? String.format("$%.2f ", account.getBalance()) : "$0.00";
        Label balanceAmount = new Label(balanceStr);
        balanceAmount.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #10B981;");

        Label depositLabel = new Label("Deposit Funds");
        depositLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox depositRow = new HBox(8);
        depositRow.setAlignment(Pos.CENTER_LEFT);
        TextField depositField = new TextField();
        depositField.setPromptText("Amount");
        depositField.setPrefWidth(140);
        Button depositBtn = new Button("Deposit");
        depositBtn.getStyleClass().addAll("button", "button-success");
        depositBtn.setStyle("-fx-padding: 8 16;");
        depositBtn.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(depositField.getText().trim());
                if (amount <= 0) {
                    AlertHelper.showError("Error", "Amount must be positive.");
                    return;
                }
                int userId = SessionManager.getInstance().getCurrentUser().getUserId();
                JSONObject result = userApi.deposit(userId, amount);
                if (result.optBoolean("success")) {
                    double newBal = result.optDouble("newBalance", 0);
                    SessionManager.getInstance().getAccount().setBalance(newBal);
                    balanceAmount.setText(
                            String.format("$%.2f", newBal));
                    depositField.clear();
                    AlertHelper.showSuccess("Deposited $" + String.format("%.2f", amount));
                } else {
                    AlertHelper.showError("Deposit Failed", result.optString("error", "Unknown error"));
                }
            } catch (NumberFormatException ex) {
                AlertHelper.showError("Invalid Input", "Enter a valid amount.");
            }
        });
        depositRow.getChildren().addAll(depositField, depositBtn);

        Label withdrawLabel = new Label("Withdraw Funds");
        withdrawLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox withdrawRow = new HBox(8);
        withdrawRow.setAlignment(Pos.CENTER_LEFT);
        TextField withdrawField = new TextField();
        withdrawField.setPromptText("Amount");
        withdrawField.setPrefWidth(140);
        Button withdrawBtn = new Button("Withdraw");
        withdrawBtn.getStyleClass().addAll("button", "button-outline");
        withdrawBtn.setStyle("-fx-padding: 8 16;");
        withdrawBtn.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(withdrawField.getText().trim());
                if (amount <= 0) {
                    AlertHelper.showError("Error", "Amount must be positive.");
                    return;
                }
                int userId = SessionManager.getInstance().getCurrentUser().getUserId();
                JSONObject result = userApi.withdraw(userId, amount);
                if (result.optBoolean("success")) {
                    double newBal = result.optDouble("newBalance", 0);
                    SessionManager.getInstance().getAccount().setBalance(newBal);
                    balanceAmount.setText(
                            String.format("$%.2f", newBal));
                    withdrawField.clear();
                    AlertHelper.showSuccess("Withdrew $" + String.format("%.2f", amount));
                } else {
                    AlertHelper.showError("Withdrawal Failed", result.optString("error", "Unknown error"));
                }
            } catch (NumberFormatException ex) {
                AlertHelper.showError("Invalid Input", "Enter a valid amount.");
            }
        });
        withdrawRow.getChildren().addAll(withdrawField, withdrawBtn);

        balanceCard.getChildren().addAll(balanceTitle, balanceAmount, new Separator(), depositLabel, depositRow,
                withdrawLabel, withdrawRow);

        // Account Actions card
        VBox actionsCard = new VBox(12);
        actionsCard.getStyleClass().add("card");

        Label actionsTitle = new Label("Account Actions");
        actionsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button editProfileBtn = new Button("\u270F\uFE0F Edit Profile");
        editProfileBtn.setMaxWidth(Double.MAX_VALUE);
        editProfileBtn.getStyleClass().addAll("button", "button-outline");
        editProfileBtn.setOnAction(e -> showEditProfileDialog());

        Button changePassBtn = new Button("\uD83D\uDD12 Change Password");
        changePassBtn.setMaxWidth(Double.MAX_VALUE);
        changePassBtn.getStyleClass().addAll("button", "button-outline");
        changePassBtn.setOnAction(e -> showChangePasswordDialog());

        Button logoutBtn = new Button("\uD83D\uDEAA Logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.getStyleClass().addAll("button", "button-secondary");
        logoutBtn.setOnAction(e -> {
            SessionManager.getInstance().logout();
            NavigationController.showLogin();
        });

        Button deleteBtn = new Button("\u26A0\uFE0F Delete Account");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.getStyleClass().addAll("button", "button-danger");
        deleteBtn.setOnAction(e -> {
            if (AlertHelper.showConfirm("Delete Account", "This action cannot be undone. Are you sure?")) {
                String result = userApi.deleteAccount(SessionManager.getInstance().getToken());
                if (!result.startsWith("ERROR")) {
                    SessionManager.getInstance().logout();
                    NavigationController.showLogin();
                } else {
                    AlertHelper.showError("Error", result);
                }
            }
        });

        actionsCard.getChildren().addAll(actionsTitle, editProfileBtn, changePassBtn, logoutBtn, new Separator(),
                deleteBtn);

        leftCol.getChildren().addAll(profileCard, balanceCard, actionsCard);

        // Right column: Transaction history
        VBox rightCol = new VBox(16);
        HBox.setHgrow(rightCol, Priority.ALWAYS);

        VBox txCard = new VBox(16);
        txCard.getStyleClass().add("card");

        Label txTitle = new Label("Transaction History");
        txTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Filter controls
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("All Types", "PURCHASE", "DEPOSIT", "WITHDRAWAL");
        typeFilter.setValue("All Types");

        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Newest First", "Oldest First", "Amount: High to Low", "Amount: Low to High");
        sortCombo.setValue("Newest First");

        filterRow.getChildren().addAll(new Label("Filter:"), typeFilter, new Label("Sort:"), sortCombo);

        VBox txList = new VBox(8);

        Runnable loadTransactions = () -> {
            new Thread(() -> {
                JSONObject info = userApi.getInfo(SessionManager.getInstance().getToken());
                List<Transaction> transactions = userApi.parseTransactions(info);

                javafx.application.Platform.runLater(() -> {
                    txList.getChildren().clear();

                    String filterType = typeFilter.getValue();
                    java.util.stream.Stream<Transaction> stream = transactions.stream();

                    if (!"All Types".equals(filterType)) {
                        stream = stream.filter(tx -> filterType.equals(tx.getType()));
                    }

                    String sort = sortCombo.getValue();
                    java.util.Comparator<Transaction> comp;
                    switch (sort) {
                        case "Oldest First":
                            comp = java.util.Comparator.comparing(Transaction::getCreatedAt);
                            break;
                        case "Amount: High to Low":
                            comp = java.util.Comparator.comparingDouble(Transaction::getAmount).reversed();
                            break;
                        case "Amount: Low to High":
                            comp = java.util.Comparator.comparingDouble(Transaction::getAmount);
                            break;
                        default:
                            comp = (a, b) -> {
                                String ca = a.getCreatedAt() != null ? a.getCreatedAt() : "";
                                String cb = b.getCreatedAt() != null ? b.getCreatedAt() : "";
                                return cb.compareTo(ca);
                            };
                            break;
                    }

                    List<Transaction> filtered = stream.sorted(comp).collect(java.util.stream.Collectors.toList());

                    if (filtered.isEmpty()) {
                        Label empty = new Label("No transactions found.");
                        empty.setStyle("-fx-text-fill: #64748B; -fx-padding: 20;");
                        txList.getChildren().add(empty);
                    } else {
                        for (Transaction tx : filtered) {
                            HBox row = new HBox(12);
                            row.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0; -fx-padding: 10 0;");
                            row.setAlignment(Pos.CENTER_LEFT);

                            String typeIcon = "PURCHASE".equals(tx.getType()) ? "\uD83D\uDED2" : "\uD83D\uDCB0";
                            Label icon = new Label(typeIcon);
                            icon.setStyle("-fx-font-size: 18px;");

                            VBox txInfo = new VBox(2);
                            HBox.setHgrow(txInfo, Priority.ALWAYS);
                            Label txType = new Label(tx.getType());
                            txType.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                            Label txDate = new Label(tx.getCreatedAt() != null ? tx.getCreatedAt() : "");
                            txDate.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");
                            txInfo.getChildren().addAll(txType, txDate);

                            Label amount = new Label(String.format("$%.2f", tx.getAmount()));
                            amount.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                            if ("DEPOSIT".equals(tx.getType())) {
                                amount.setStyle(amount.getStyle() + " -fx-text-fill: #10B981;");
                                amount.setText("+" + amount.getText());
                            } else {
                                amount.setStyle(amount.getStyle() + " -fx-text-fill: #EF4444;");
                                amount.setText("-" + amount.getText());
                            }

                            Label statusBadge = new Label(tx.getStatus());
                            statusBadge.getStyleClass().addAll("badge",
                                    "COMPLETED".equals(tx.getStatus()) ? "badge-completed" : "badge-pending");

                            row.getChildren().addAll(icon, txInfo, amount, statusBadge);
                            txList.getChildren().add(row);
                        }
                    }
                });
            }).start();
        };

        typeFilter.setOnAction(e -> loadTransactions.run());
        sortCombo.setOnAction(e -> loadTransactions.run());

        txCard.getChildren().addAll(txTitle, filterRow, txList);
        rightCol.getChildren().add(txCard);

        mainRow.getChildren().addAll(leftCol, rightCol);
        content.getChildren().addAll(title, mainRow);

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");

        loadTransactions.run();
    }

    private void showEditProfileDialog() {
        User user = SessionManager.getInstance().getCurrentUser();
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(user != null ? user.getUsername() : "");
        TextField emailField = new TextField(user != null ? user.getEmail() : "");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String result = userApi.updateProfile(SessionManager.getInstance().getToken(),
                        nameField.getText().trim(), emailField.getText().trim(), null);
                if (!result.startsWith("ERROR")) {
                    if (user != null) {
                        user.setUsername(nameField.getText().trim());
                        user.setEmail(emailField.getText().trim());
                    }
                    AlertHelper.showSuccess("Profile updated!");
                    layout.navigateTo("myaccount");
                } else {
                    AlertHelper.showError("Error", result);
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void showChangePasswordDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Change Password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        PasswordField oldPass = new PasswordField();
        oldPass.setPromptText("Current password");
        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New password (min 8 chars)");
        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirm new password");

        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(oldPass, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPass, 1, 1);
        grid.add(new Label("Confirm:"), 0, 2);
        grid.add(confirmPass, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (!newPass.getText().equals(confirmPass.getText())) {
                    AlertHelper.showError("Error", "Passwords do not match.");
                    return null;
                }
                if (newPass.getText().length() < 8) {
                    AlertHelper.showError("Error", "New password must be at least 8 characters.");
                    return null;
                }
                String result = userApi.changePassword(SessionManager.getInstance().getToken(),
                        oldPass.getText(), newPass.getText());
                if (!result.startsWith("ERROR")) {
                    AlertHelper.showSuccess("Password changed successfully!");
                } else {
                    AlertHelper.showError("Error", result);
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    public ScrollPane getRoot() {
        return root;
    }
}
