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

public class AdminTransactionsView {

    private final VBox root;
    private final MainLayout mainLayout;
    private final AdminApiService adminApi = new AdminApiService();
    private ObservableList<TxRow> txData = FXCollections.observableArrayList();

    public AdminTransactionsView(MainLayout mainLayout) {
        this.mainLayout = mainLayout;
        root = new VBox(16);
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(0));
        buildView();
    }

    private void buildView() {
        Label title = new Label("\uD83D\uDCB3  Transactions Overview");
        title.getStyleClass().addAll("label", "heading");

        String token = SessionManager.getInstance().getToken();
        JSONObject result = adminApi.getAllTransactions(token);

        HBox statsRow = buildStatsRow(result);
        TableView<TxRow> table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        populateTable(result);

        Button refreshBtn = new Button("\u21BB  Refresh");
        refreshBtn.getStyleClass().addAll("button", "button-secondary");
        refreshBtn.setOnAction(e -> {
            JSONObject r = adminApi.getAllTransactions(SessionManager.getInstance().getToken());
            populateTable(r);
        });

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(title, sp, refreshBtn);

        root.getChildren().addAll(header, statsRow, table);
    }

    private HBox buildStatsRow(JSONObject result) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        int total = result != null ? result.optInt("total", 0) : 0;
        int completed = result != null ? result.optInt("completed", 0) : 0;
        int pending = result != null ? result.optInt("pending", 0) : 0;
        int failed = result != null ? result.optInt("failed", 0) : 0;
        int refunded = result != null ? result.optInt("refunded", 0) : 0;
        double revenue = result != null ? result.optDouble("totalRevenue", 0) : 0;

        row.getChildren().addAll(
            miniStatCard("Total", String.valueOf(total), "-primary"),
            miniStatCard("Completed", String.valueOf(completed), "-success"),
            miniStatCard("Pending", String.valueOf(pending), "-warning"),
            miniStatCard("Failed", String.valueOf(failed), "-danger"),
            miniStatCard("Refunded", String.valueOf(refunded), "-accent"),
            miniStatCard("Revenue", String.format("EGP %.0f", revenue), "-success")
        );

        for (var c : row.getChildren()) HBox.setHgrow(c, Priority.ALWAYS);
        return row;
    }

    private VBox miniStatCard(String label, String value, String color) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(14));
        card.setAlignment(Pos.CENTER_LEFT);
        Label vl = new Label(value);
        vl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label ll = new Label(label);
        ll.getStyleClass().add("stat-label");
        card.getChildren().addAll(vl, ll);
        return card;
    }

    @SuppressWarnings("unchecked")
    private TableView<TxRow> buildTable() {
        TableView<TxRow> tv = new TableView<>();
        tv.setItems(txData);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<TxRow, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("txId"));
        idCol.setMaxWidth(60);

        TableColumn<TxRow, String> buyerCol = new TableColumn<>("Buyer");
        buyerCol.setCellValueFactory(new PropertyValueFactory<>("buyerId"));
        buyerCol.setMaxWidth(80);

        TableColumn<TxRow, String> sellerCol = new TableColumn<>("Seller");
        sellerCol.setCellValueFactory(new PropertyValueFactory<>("sellerId"));
        sellerCol.setMaxWidth(80);

        TableColumn<TxRow, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setMaxWidth(140);

        TableColumn<TxRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setMaxWidth(110);
        typeCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label b = new Label(item);
                b.getStyleClass().add("badge");
                if ("PURCHASE".equals(item)) b.getStyleClass().add("badge-available");
                else if ("DEPOSIT".equals(item)) b.getStyleClass().add("badge-pending");
                else b.setStyle("-fx-background-color: -bg-secondary; -fx-text-fill: -text-secondary; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(b); setText(null);
            }
        });

        TableColumn<TxRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setMaxWidth(110);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label b = new Label(item);
                b.getStyleClass().add("badge");
                switch (item) {
                    case "COMPLETED": b.getStyleClass().add("badge-completed"); break;
                    case "PENDING": b.getStyleClass().add("badge-pending"); break;
                    case "FAILED": b.setStyle("-fx-background-color: -danger-bg; -fx-text-fill: -danger; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;"); break;
                    case "REFUNDED": b.setStyle("-fx-background-color: -primary-light; -fx-text-fill: -primary; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;"); break;
                    default: break;
                }
                setGraphic(b); setText(null);
            }
        });

        TableColumn<TxRow, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        tv.getColumns().addAll(idCol, buyerCol, sellerCol, amountCol, typeCol, statusCol, dateCol);
        return tv;
    }

    private void populateTable(JSONObject result) {
        txData.clear();
        if (result == null || !result.has("transactions")) return;
        JSONArray arr = result.getJSONArray("transactions");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject t = arr.getJSONObject(i);
            String date = t.optString("createdAt", "");
            if (date.length() > 16) date = date.substring(0, 16);
            txData.add(new TxRow(
                String.valueOf(t.optInt("transactionId")),
                String.valueOf(t.optInt("buyerId")),
                String.valueOf(t.optInt("sellerId")),
                String.format("EGP %.2f", t.optDouble("amount", 0)),
                t.optString("type", ""), t.optString("status", ""), date
            ));
        }
    }

    public VBox getRoot() { return root; }

    public static class TxRow {
        private final SimpleStringProperty txId, buyerId, sellerId, amount, type, status, createdAt;
        public TxRow(String id, String b, String s, String a, String t, String st, String d) {
            this.txId = new SimpleStringProperty(id); this.buyerId = new SimpleStringProperty(b);
            this.sellerId = new SimpleStringProperty(s); this.amount = new SimpleStringProperty(a);
            this.type = new SimpleStringProperty(t); this.status = new SimpleStringProperty(st);
            this.createdAt = new SimpleStringProperty(d);
        }
        public String getTxId() { return txId.get(); } public String getBuyerId() { return buyerId.get(); }
        public String getSellerId() { return sellerId.get(); } public String getAmount() { return amount.get(); }
        public String getType() { return type.get(); } public String getStatus() { return status.get(); }
        public String getCreatedAt() { return createdAt.get(); }
    }
}
