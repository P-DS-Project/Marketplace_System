package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.TransactionApiService;
import state.SessionManager;
import utils.AlertHelper;
import org.json.JSONObject;
import org.json.JSONArray;
import services.ReportApiService;

public class TransactionView {

    private final ScrollPane root;

    public TransactionView() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(0));

        Label title = new Label("\uD83D\uDCB0 Transactions");
        title.getStyleClass().add("heading");

        // Buy product form
        VBox buyCard = new VBox(16);
        buyCard.getStyleClass().add("card");

        Label buyTitle = new Label("\uD83D\uDED2 Purchase a Product");
        buyTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        HBox buyForm = new HBox(12);
        buyForm.setAlignment(Pos.CENTER_LEFT);
        TextField productIdField = new TextField();
        productIdField.setPromptText("Product ID");
        productIdField.setPrefWidth(150);
        TextField qtyField = new TextField("1");
        qtyField.setPromptText("Quantity");
        qtyField.setPrefWidth(100);
        Button buyBtn = new Button("\uD83D\uDED2 Buy Now");
        buyBtn.getStyleClass().addAll("button", "button-success");
        buyBtn.setOnAction(e -> {
            try {
                int productId = Integer.parseInt(productIdField.getText().trim());
                int qty = Integer.parseInt(qtyField.getText().trim());
                int buyerId = SessionManager.getInstance().getCurrentUser().getUserId();

                TransactionApiService txApi = new TransactionApiService();
                JSONObject result = txApi.buy(buyerId, productId, qty);
                if (result.optBoolean("success")) {
                    AlertHelper.showSuccess("Purchase successful! Paid: $" + String.format("%.2f", result.optDouble("totalPaid", 0))
                        + "\nRemaining balance: $" + String.format("%.2f", result.optDouble("remainingBalance", 0)));
                } else {
                    AlertHelper.showError("Purchase Failed", result.optString("error", "Unknown error"));
                }
            } catch (NumberFormatException ex) {
                AlertHelper.showError("Invalid Input", "Please enter valid numbers.");
            }
        });
        buyForm.getChildren().addAll(
            new Label("Product ID:"), productIdField,
            new Label("Qty:"), qtyField, buyBtn
        );

        buyCard.getChildren().addAll(buyTitle, buyForm);

        // Transaction history
        VBox historyCard = new VBox(16);
        historyCard.getStyleClass().add("card");

        Label histTitle = new Label("\uD83D\uDCC3 Transaction History");
        histTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        VBox historyContent = new VBox(8);
        Label loading = new Label("Loading...");
        historyContent.getChildren().add(loading);

        historyCard.getChildren().addAll(histTitle, historyContent);

        content.getChildren().addAll(title, buyCard, historyCard);

        // Load transaction history
        int userId = SessionManager.getInstance().getCurrentUser() != null ? SessionManager.getInstance().getCurrentUser().getUserId() : -1;
        new Thread(() -> {
            ReportApiService reportApi = new ReportApiService();
            JSONObject report = reportApi.getTransactionHistory(userId);
            javafx.application.Platform.runLater(() -> {
                historyContent.getChildren().clear();
                if (report == null) {
                    historyContent.getChildren().add(new Label("No transaction history available."));
                    return;
                }

                // Summary stats
                HBox stats = new HBox(16);
                stats.getChildren().addAll(
                    createMiniStat("Total", String.valueOf(report.optInt("totalTransactions", 0))),
                    createMiniStat("Spent", "$" + String.format("%.2f", report.optDouble("totalSpent", 0))),
                    createMiniStat("Earned", "$" + String.format("%.2f", report.optDouble("totalEarned", 0))),
                    createMiniStat("Purchases", String.valueOf(report.optInt("purchaseCount", 0))),
                    createMiniStat("Sales", String.valueOf(report.optInt("saleCount", 0)))
                );
                historyContent.getChildren().add(stats);

                JSONArray txArray = report.optJSONArray("transactions");
                if (txArray != null && txArray.length() > 0) {
                    for (int i = 0; i < txArray.length(); i++) {
                        JSONObject tx = txArray.getJSONObject(i);
                        HBox row = new HBox(16);
                        row.setPadding(new Insets(8, 0, 8, 0));
                        row.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");
                        row.setAlignment(Pos.CENTER_LEFT);

                        Label txId = new Label("#" + tx.optInt("transactionId"));
                        txId.setStyle("-fx-font-weight: bold;");
                        Label type = new Label(tx.optString("type", "N/A"));
                        Label amount = new Label("$" + String.format("%.2f", tx.optDouble("amount", 0)));
                        amount.setStyle("-fx-font-weight: bold;");
                        Label status = new Label(tx.optString("status", ""));
                        status.getStyleClass().addAll("badge", "COMPLETED".equals(tx.optString("status")) ? "badge-completed" : "badge-pending");

                        row.getChildren().addAll(txId, type, amount, status);
                        historyContent.getChildren().add(row);
                    }
                } else {
                    historyContent.getChildren().add(new Label("No transactions found."));
                }
            });
        }).start();

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");
    }

    private VBox createMiniStat(String label, String value) {
        VBox stat = new VBox(4);
        stat.getStyleClass().add("stat-card");
        stat.setPrefWidth(140);
        Label val = new Label(value);
        val.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");
        stat.getChildren().addAll(val, lbl);
        return stat;
    }

    public ScrollPane getRoot() { return root; }
}
