package views;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.json.JSONArray;
import org.json.JSONObject;
import services.AdminApiService;
import state.SessionManager;
import utils.AlertHelper;

public class AdminUsersView {

    private final VBox root;
    private final MainLayout mainLayout;
    private final AdminApiService adminApi = new AdminApiService();
    private TableView<UserRow> table;
    private ObservableList<UserRow> userData = FXCollections.observableArrayList();

    public AdminUsersView(MainLayout mainLayout) {
        this.mainLayout = mainLayout;
        root = new VBox(16);
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(0));
        buildView();
    }

    private void buildView() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("\uD83D\uDC65  Users Management");
        title.getStyleClass().addAll("label", "heading");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0D Search by name, email, or ID...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(300);
        searchField.setOnAction(e -> searchUsers(searchField.getText().trim()));

        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("button");
        searchBtn.setOnAction(e -> searchUsers(searchField.getText().trim()));

        Button refreshBtn = new Button("\u21BB  Refresh");
        refreshBtn.getStyleClass().addAll("button", "button-secondary");
        refreshBtn.setOnAction(e -> { searchField.clear(); loadAllUsers(); });

        header.getChildren().addAll(title, spacer, searchField, searchBtn, refreshBtn);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        Label countLabel = new Label();
        countLabel.setStyle("-fx-text-fill: -text-subtle; -fx-font-size: 12px;");
        userData.addListener((javafx.collections.ListChangeListener<UserRow>) c ->
            countLabel.setText("Showing " + userData.size() + " users")
        );

        root.getChildren().addAll(header, table, countLabel);

        loadAllUsers();
    }

    @SuppressWarnings("unchecked")
    private TableView<UserRow> buildTable() {
        TableView<UserRow> tv = new TableView<>();
        tv.setItems(userData);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<UserRow, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        idCol.setMaxWidth(60);

        TableColumn<UserRow, String> nameCol = new TableColumn<>("Username");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<UserRow, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<UserRow, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setMaxWidth(130);
        roleCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.getStyleClass().add("badge");
                switch (item) {
                    case "ADMIN": badge.setStyle("-fx-background-color: #EDE9FE; -fx-text-fill: #6D28D9; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;"); break;
                    case "EXTERNAL_STORE": badge.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;"); break;
                    default: badge.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;"); break;
                }
                setGraphic(badge);
                setText(null);
            }
        });

        TableColumn<UserRow, String> activeCol = new TableColumn<>("Status");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setMaxWidth(100);
        activeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                if ("Active".equals(item)) {
                    badge.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");
                } else {
                    badge.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");
                }
                setGraphic(badge);
                setText(null);
            }
        });

        TableColumn<UserRow, String> createdCol = new TableColumn<>("Created");
        createdCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdCol.setMaxWidth(160);

        TableColumn<UserRow, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setMaxWidth(280);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                UserRow row = getTableView().getItems().get(getIndex());
                HBox box = new HBox(6);
                box.setAlignment(Pos.CENTER);

                int currentUserId = SessionManager.getInstance().getCurrentUser().getUserId();
                int rowId = Integer.parseInt(row.getUserId());

                if (rowId != currentUserId) {
                    if ("ADMIN".equals(row.getRole())) {
                        Button demoteBtn = new Button("Demote");
                        demoteBtn.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px; -fx-cursor: hand; -fx-font-weight: bold;");
                        demoteBtn.setOnAction(e -> demoteUser(rowId));
                        box.getChildren().add(demoteBtn);
                    } else {
                        Button promoteBtn = new Button("Promote");
                        promoteBtn.setStyle("-fx-background-color: #EDE9FE; -fx-text-fill: #6D28D9; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px; -fx-cursor: hand; -fx-font-weight: bold;");
                        promoteBtn.setOnAction(e -> promoteUser(rowId));
                        box.getChildren().add(promoteBtn);
                    }

                    if ("Active".equals(row.getActive())) {
                        Button blockBtn = new Button("Block");
                        blockBtn.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px; -fx-cursor: hand; -fx-font-weight: bold;");
                        blockBtn.setOnAction(e -> toggleActive(rowId, false));
                        box.getChildren().add(blockBtn);
                    } else {
                        Button unblockBtn = new Button("Unblock");
                        unblockBtn.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px; -fx-cursor: hand; -fx-font-weight: bold;");
                        unblockBtn.setOnAction(e -> toggleActive(rowId, true));
                        box.getChildren().add(unblockBtn);
                    }

                    Button deleteBtn = new Button("Delete");
                    deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px; -fx-cursor: hand; -fx-font-weight: bold;");
                    deleteBtn.setOnAction(e -> deleteUser(rowId, row.getUsername()));
                    box.getChildren().add(deleteBtn);
                } else {
                    Label youLabel = new Label("(You)");
                    youLabel.setStyle("-fx-text-fill: -text-subtle; -fx-font-style: italic;");
                    box.getChildren().add(youLabel);
                }

                setGraphic(box);
            }
        });

        tv.getColumns().addAll(idCol, nameCol, emailCol, roleCol, activeCol, createdCol, actionsCol);
        return tv;
    }

    private void loadAllUsers() {
        String token = SessionManager.getInstance().getToken();
        JSONObject result = adminApi.getAllUsers(token);
        populateTable(result);
    }

    private void searchUsers(String query) {
        if (query.isEmpty()) { loadAllUsers(); return; }
        String token = SessionManager.getInstance().getToken();
        JSONObject result = adminApi.searchUsers(token, query);
        populateTable(result);
    }

    private void populateTable(JSONObject result) {
        userData.clear();
        if (result == null || !result.has("users")) return;
        JSONArray arr = result.getJSONArray("users");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject u = arr.getJSONObject(i);
            String created = u.optString("createdAt", "");
            if (created.length() > 16) created = created.substring(0, 16);
            userData.add(new UserRow(
                String.valueOf(u.optInt("userId")),
                u.optString("username", ""),
                u.optString("email", ""),
                u.optString("role", "USER"),
                u.optBoolean("isActive", true) ? "Active" : "Disabled",
                created
            ));
        }
    }

    private void promoteUser(int userId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Promote this user to Admin?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Confirm Promotion");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                boolean ok = adminApi.promoteUser(SessionManager.getInstance().getToken(), userId);
                if (ok) loadAllUsers();
                else AlertHelper.showError("Error", "Failed to promote user.");
            }
        });
    }

    private void demoteUser(int userId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Remove admin privileges from this user?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Confirm Demotion");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                boolean ok = adminApi.demoteUser(SessionManager.getInstance().getToken(), userId);
                if (ok) loadAllUsers();
                else AlertHelper.showError("Error", "Failed to demote user.");
            }
        });
    }

    private void toggleActive(int userId, boolean active) {
        String action = active ? "unblock" : "block";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to " + action + " this user?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Confirm " + (active ? "Unblock" : "Block"));
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                boolean ok = adminApi.toggleUserActive(SessionManager.getInstance().getToken(), userId, active);
                if (ok) loadAllUsers();
                else AlertHelper.showError("Error", "Failed to " + action + " user.");
            }
        });
    }

    private void deleteUser(int userId, String username) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "Are you sure you want to permanently delete user \"" + username + "\" (ID: " + userId + ")?\n\nThis action cannot be undone.",
            ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("\u26A0 Delete User");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                boolean ok = adminApi.deleteUser(SessionManager.getInstance().getToken(), userId);
                if (ok) loadAllUsers();
                else AlertHelper.showError("Error", "Failed to delete user.");
            }
        });
    }

    public VBox getRoot() { return root; }

    // JavaFX TableView data class
    public static class UserRow {
        private final SimpleStringProperty userId, username, email, role, active, createdAt;
        public UserRow(String userId, String username, String email, String role, String active, String createdAt) {
            this.userId = new SimpleStringProperty(userId);
            this.username = new SimpleStringProperty(username);
            this.email = new SimpleStringProperty(email);
            this.role = new SimpleStringProperty(role);
            this.active = new SimpleStringProperty(active);
            this.createdAt = new SimpleStringProperty(createdAt);
        }
        public String getUserId() { return userId.get(); }
        public String getUsername() { return username.get(); }
        public String getEmail() { return email.get(); }
        public String getRole() { return role.get(); }
        public String getActive() { return active.get(); }
        public String getCreatedAt() { return createdAt.get(); }
    }
}
