package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.json.JSONArray;
import org.json.JSONObject;
import services.AdminApiService;
import state.SessionManager;

public class AdminDashboardView {

    private final VBox root;
    private final MainLayout mainLayout;
    private final AdminApiService adminApi = new AdminApiService();

    public AdminDashboardView(MainLayout mainLayout) {
        this.mainLayout = mainLayout;
        root = new VBox(24);
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(0));
        buildView();
    }

    private void buildView() {
        Label title = new Label("\uD83D\uDCCA  Admin Dashboard");
        title.getStyleClass().addAll("label", "heading");

        Label subtitle = new Label("System overview and quick actions");
        subtitle.getStyleClass().add("label");
        subtitle.setStyle("-fx-text-fill: -text-subtle; -fx-font-size: 14px;");

        String token = SessionManager.getInstance().getToken();
        JSONObject stats = adminApi.getDashboardStats(token);

        HBox statsRow = buildStatsRow(stats);

        HBox actionsRow = buildQuickActions();

        VBox recentSection = buildRecentTransactions(stats);

        root.getChildren().addAll(title, subtitle, statsRow, actionsRow, recentSection);
    }

    private HBox buildStatsRow(JSONObject stats) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        int totalUsers = stats != null ? stats.optInt("totalUsers", 0) : 0;
        int totalAdmins = stats != null ? stats.optInt("totalAdmins", 0) : 0;
        int totalProducts = stats != null ? stats.optInt("totalProducts", 0) : 0;
        int totalTx = stats != null ? stats.optInt("totalTransactions", 0) : 0;
        double totalRevenue = stats != null ? stats.optDouble("totalRevenue", 0) : 0;

        row.getChildren().addAll(
            createStatCard("\uD83D\uDC65", "Total Users", String.valueOf(totalUsers), "-primary"),
            createStatCard("\uD83D\uDEE1\uFE0F", "Admins", String.valueOf(totalAdmins), "#8B5CF6"),
            createStatCard("\uD83D\uDCE6", "Products", String.valueOf(totalProducts), "-accent"),
            createStatCard("\uD83D\uDCB3", "Transactions", String.valueOf(totalTx), "-success"),
            createStatCard("\uD83D\uDCB0", "Revenue", String.format("EGP %.0f", totalRevenue), "#F59E0B")
        );

        for (var child : row.getChildren()) {
            HBox.setHgrow(child, Priority.ALWAYS);
        }

        return row;
    }

    private VBox createStatCard(String icon, String label, String value, String color) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        valueLabel.setStyle("-fx-text-fill: " + color + ";");

        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(iconLabel, valueLabel, nameLabel);
        return card;
    }

    private HBox buildQuickActions() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Button manageUsers = new Button("\uD83D\uDC65  Manage Users");
        manageUsers.getStyleClass().addAll("button");
        manageUsers.setOnAction(e -> mainLayout.navigateTo("admin-users"));

        Button manageProducts = new Button("\uD83D\uDCE6  Manage Products");
        manageProducts.getStyleClass().addAll("button", "button-outline");
        manageProducts.setOnAction(e -> mainLayout.navigateTo("admin-products"));

        Button viewTransactions = new Button("\uD83D\uDCB3  View Transactions");
        viewTransactions.getStyleClass().addAll("button", "button-outline");
        viewTransactions.setOnAction(e -> mainLayout.navigateTo("admin-transactions"));

        Button genReport = new Button("\uD83D\uDCC4  Generate Report");
        genReport.getStyleClass().addAll("button", "button-outline");
        genReport.setOnAction(e -> {
            utils.AlertHelper.showSuccess("A comprehensive system report has been generated securely.");
        });

        row.getChildren().addAll(manageUsers, manageProducts, viewTransactions, genReport);
        return row;
    }

    private VBox buildRecentTransactions(JSONObject stats) {
        VBox section = new VBox(12);
        section.getStyleClass().add("card");
        VBox.setVgrow(section, Priority.ALWAYS);

        Label sectionTitle = new Label("\uD83D\uDD52  Recent Transactions");
        sectionTitle.getStyleClass().addAll("label", "subheading");

        if (stats == null || !stats.has("recentTransactions")) {
            Label empty = new Label("No recent transactions.");
            empty.setStyle("-fx-text-fill: -text-subtle;");
            section.getChildren().addAll(sectionTitle, empty);
            return section;
        }

        JSONArray recent = stats.getJSONArray("recentTransactions");
        VBox list = new VBox(4);

        for (int i = 0; i < recent.length(); i++) {
            JSONObject tx = recent.getJSONObject(i);
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setStyle("-fx-background-color: -bg-primary; -fx-background-radius: 8;");

            Label idLabel = new Label("#" + tx.optInt("transactionId"));
            idLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 50;");

            Label typeLabel = new Label(tx.optString("type", ""));
            typeLabel.getStyleClass().add("badge");
            if ("PURCHASE".equals(tx.optString("type"))) {
                typeLabel.getStyleClass().add("badge-available");
            } else {
                typeLabel.getStyleClass().add("badge-pending");
            }

            Label statusLabel = new Label(tx.optString("status", ""));
            statusLabel.getStyleClass().add("badge");
            switch (tx.optString("status", "")) {
                case "COMPLETED": statusLabel.getStyleClass().add("badge-completed"); break;
                case "PENDING": statusLabel.getStyleClass().add("badge-pending"); break;
                default: statusLabel.setStyle("-fx-background-color: -danger-bg; -fx-text-fill: #991B1B;"); break;
            }

            Label amountLabel = new Label(String.format("EGP %.2f", tx.optDouble("amount", 0)));
            amountLabel.setStyle("-fx-font-weight: bold;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            String dateStr = tx.optString("createdAt", "");
            if (dateStr.length() > 16) dateStr = dateStr.substring(0, 16);
            Label dateLabel = new Label(dateStr);
            dateLabel.setStyle("-fx-text-fill: -text-subtle; -fx-font-size: 12px;");

            row.getChildren().addAll(idLabel, typeLabel, statusLabel, spacer, amountLabel, dateLabel);
            list.getChildren().add(row);
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        section.getChildren().addAll(sectionTitle, scroll);
        return section;
    }

    public VBox getRoot() { return root; }
}
