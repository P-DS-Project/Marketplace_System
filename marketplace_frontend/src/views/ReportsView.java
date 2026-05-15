package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.ReportApiService;
import state.SessionManager;
import org.json.JSONObject;
import org.json.JSONArray;

public class ReportsView {

    private final ScrollPane root;
    private final ReportApiService reportApi = new ReportApiService();
    private final VBox reportContent;

    public ReportsView() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(0));

        Label title = new Label("\uD83D\uDCC8 Reports Dashboard");
        title.getStyleClass().add("heading");

        // Report type selector
        HBox reportTabs = new HBox(12);
        reportTabs.setAlignment(Pos.CENTER_LEFT);

        Button txReportBtn = new Button("\uD83D\uDCB0 Transaction History");
        txReportBtn.setOnAction(e -> loadTransactionReport());
        Button salesBtn = new Button("\uD83D\uDCC8 Sales Report");
        salesBtn.setOnAction(e -> loadSalesReport());
        Button invBtn = new Button("\uD83D\uDCE6 Inventory Report");
        invBtn.setOnAction(e -> loadInventoryReport());
        Button statsBtn = new Button("\uD83D\uDCCA System Statistics");
        statsBtn.getStyleClass().addAll("button", "button-secondary");
        statsBtn.setOnAction(e -> loadSystemStats());

        reportTabs.getChildren().addAll(txReportBtn, salesBtn, invBtn, statsBtn);

        reportContent = new VBox(16);
        reportContent.getStyleClass().add("card");
        reportContent.getChildren().add(new Label("Select a report type above to generate."));

        content.getChildren().addAll(title, reportTabs, reportContent);

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");
    }

    private void loadTransactionReport() {
        int userId = SessionManager.getInstance().getCurrentUser() != null ? SessionManager.getInstance().getCurrentUser().getUserId() : -1;
        reportContent.getChildren().clear();
        reportContent.getChildren().add(new Label("Loading..."));

        new Thread(() -> {
            JSONObject report = reportApi.getTransactionHistory(userId);
            javafx.application.Platform.runLater(() -> {
                reportContent.getChildren().clear();
                if (report == null) { reportContent.getChildren().add(new Label("Failed to load report.")); return; }

                Label rTitle = new Label("\uD83D\uDCB0 Transaction History Report");
                rTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

                HBox stats = new HBox(16);
                stats.getChildren().addAll(
                    miniStat("Total Transactions", String.valueOf(report.optInt("totalTransactions", 0))),
                    miniStat("Total Spent", "$" + String.format("%.2f", report.optDouble("totalSpent", 0))),
                    miniStat("Total Earned", "$" + String.format("%.2f", report.optDouble("totalEarned", 0))),
                    miniStat("Purchases", String.valueOf(report.optInt("purchaseCount", 0))),
                    miniStat("Sales", String.valueOf(report.optInt("saleCount", 0)))
                );

                reportContent.getChildren().addAll(rTitle, stats);

                JSONArray txArr = report.optJSONArray("transactions");
                if (txArr != null) {
                    for (int i = 0; i < Math.min(txArr.length(), 50); i++) {
                        JSONObject tx = txArr.getJSONObject(i);
                        HBox row = new HBox(16);
                        row.setPadding(new Insets(6, 0, 6, 0));
                        row.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");
                        row.getChildren().addAll(
                            new Label("#" + tx.optInt("transactionId")),
                            new Label(tx.optString("type", "")),
                            new Label("$" + String.format("%.2f", tx.optDouble("amount", 0))),
                            createBadge(tx.optString("status", ""))
                        );
                        reportContent.getChildren().add(row);
                    }
                }
            });
        }).start();
    }

    private void loadSalesReport() {
        int userId = SessionManager.getInstance().getCurrentUser() != null ? SessionManager.getInstance().getCurrentUser().getUserId() : -1;
        reportContent.getChildren().clear();
        reportContent.getChildren().add(new Label("Loading..."));

        new Thread(() -> {
            JSONObject report = reportApi.getSalesReport(userId);
            javafx.application.Platform.runLater(() -> {
                reportContent.getChildren().clear();
                if (report == null) { reportContent.getChildren().add(new Label("Failed to load report.")); return; }

                Label rTitle = new Label("\uD83D\uDCC8 Sales Report");
                rTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

                HBox stats = new HBox(16);
                stats.getChildren().addAll(
                    miniStat("Total Revenue", "$" + String.format("%.2f", report.optDouble("totalRevenue", 0))),
                    miniStat("Total Sales", String.valueOf(report.optInt("totalSales", 0))),
                    miniStat("Items Sold", String.valueOf(report.optInt("totalItemsSold", 0))),
                    miniStat("Active Products", String.valueOf(report.optInt("activeProducts", 0))),
                    miniStat("Avg Order Value", "$" + String.format("%.2f", report.optDouble("averageOrderValue", 0)))
                );

                reportContent.getChildren().addAll(rTitle, stats);

                JSONArray salesArr = report.optJSONArray("sales");
                if (salesArr != null) {
                    for (int i = 0; i < Math.min(salesArr.length(), 50); i++) {
                        JSONObject s = salesArr.getJSONObject(i);
                        HBox row = new HBox(16);
                        row.setPadding(new Insets(6, 0, 6, 0));
                        row.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");
                        row.getChildren().addAll(
                            new Label("TX #" + s.optInt("transactionId")),
                            new Label("Buyer #" + s.optInt("buyerId")),
                            new Label("Qty: " + s.optInt("quantity")),
                            new Label("$" + String.format("%.2f", s.optDouble("amount", 0))),
                            createBadge(s.optString("status", ""))
                        );
                        reportContent.getChildren().add(row);
                    }
                }
            });
        }).start();
    }

    private void loadInventoryReport() {
        reportContent.getChildren().clear();
        reportContent.getChildren().add(new Label("Loading..."));

        new Thread(() -> {
            JSONObject report = reportApi.getInventoryReport();
            javafx.application.Platform.runLater(() -> {
                reportContent.getChildren().clear();
                if (report == null) { reportContent.getChildren().add(new Label("Failed to load report.")); return; }

                Label rTitle = new Label("\uD83D\uDCE6 Inventory Report");
                rTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

                HBox stats = new HBox(16);
                stats.getChildren().addAll(
                    miniStat("Total Products", String.valueOf(report.optInt("totalProducts", 0))),
                    miniStat("In Stock", String.valueOf(report.optInt("inStockCount", 0))),
                    miniStat("Out of Stock", String.valueOf(report.optInt("outOfStockCount", 0)))
                );

                reportContent.getChildren().addAll(rTitle, stats);

                JSONArray invArr = report.optJSONArray("inventory");
                if (invArr != null) {
                    for (int i = 0; i < Math.min(invArr.length(), 50); i++) {
                        JSONObject item = invArr.getJSONObject(i);
                        HBox row = new HBox(16);
                        row.setPadding(new Insets(6, 0, 6, 0));
                        row.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");
                        row.getChildren().addAll(
                            new Label("#" + item.optInt("productId")),
                            new Label(item.optString("productName", "")),
                            new Label("Qty: " + item.optInt("quantity")),
                            new Label(item.optString("warehouseNode", "N/A")),
                            createBadge(item.optString("status", ""))
                        );
                        reportContent.getChildren().add(row);
                    }
                }
            });
        }).start();
    }

    private void loadSystemStats() {
        reportContent.getChildren().clear();
        reportContent.getChildren().add(new Label("Loading..."));

        new Thread(() -> {
            JSONObject report = reportApi.getSystemStatistics();
            javafx.application.Platform.runLater(() -> {
                reportContent.getChildren().clear();
                if (report == null) { reportContent.getChildren().add(new Label("Failed to load report.")); return; }

                Label rTitle = new Label("\uD83D\uDCCA System Statistics");
                rTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

                HBox stats = new HBox(16);
                stats.getChildren().addAll(
                    miniStat("Total Products", String.valueOf(report.optInt("totalProducts", 0))),
                    miniStat("IN_STOCK", String.valueOf(report.optInt("availableProducts", 0))),
                    miniStat("OUT_OF_STOCK", String.valueOf(report.optInt("soldProducts", 0)))
                );

                reportContent.getChildren().addAll(rTitle, stats);
            });
        }).start();
    }

    private VBox miniStat(String label, String value) {
        VBox stat = new VBox(4);
        stat.getStyleClass().add("stat-card");
        stat.setPrefWidth(160);
        Label val = new Label(value);
        val.getStyleClass().add("stat-value");
        val.setStyle("-fx-font-size: 18px;");
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");
        stat.getChildren().addAll(val, lbl);
        return stat;
    }

    private Label createBadge(String status) {
        Label badge = new Label(status);
        String cls = "badge-pending";
        if ("COMPLETED".equals(status)) cls = "badge-completed";
        else if ("IN_STOCK".equals(status)) cls = "badge-available";
        else if ("OUT_OF_STOCK".equals(status)) cls = "badge-sold";
        badge.getStyleClass().addAll("badge", cls);
        return badge;
    }

    public ScrollPane getRoot() { return root; }
}
