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

public class AdminProductsView {

    private final VBox root;
    private final MainLayout mainLayout;
    private final AdminApiService adminApi = new AdminApiService();
    private ObservableList<ProductRow> productData = FXCollections.observableArrayList();

    public AdminProductsView(MainLayout mainLayout) {
        this.mainLayout = mainLayout;
        root = new VBox(16);
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(0));
        buildView();
    }

    private void buildView() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("\uD83D\uDCE6  Products Management");
        title.getStyleClass().addAll("label", "heading");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button refreshBtn = new Button("\u21BB  Refresh");
        refreshBtn.getStyleClass().addAll("button", "button-secondary");
        refreshBtn.setOnAction(e -> loadAllProducts());
        header.getChildren().addAll(title, spacer, refreshBtn);

        TableView<ProductRow> table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(header, table);
        loadAllProducts();
    }

    @SuppressWarnings("unchecked")
    private TableView<ProductRow> buildTable() {
        TableView<ProductRow> tv = new TableView<>();
        tv.setItems(productData);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ProductRow, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        idCol.setMaxWidth(60);

        TableColumn<ProductRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<ProductRow, String> sellerCol = new TableColumn<>("Seller");
        sellerCol.setCellValueFactory(new PropertyValueFactory<>("sellerId"));
        sellerCol.setMaxWidth(80);

        TableColumn<ProductRow, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setMaxWidth(120);

        TableColumn<ProductRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setMaxWidth(110);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.getStyleClass().add("badge");
                if ("IN_STOCK".equals(item)) badge.getStyleClass().add("badge-available");
                else if ("OUT_OF_STOCK".equals(item)) badge.getStyleClass().add("badge-sold");
                else badge.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #475569; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(badge); setText(null);
            }
        });

        TableColumn<ProductRow, String> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockCol.setMaxWidth(80);
        stockCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                try {
                    int qty = Integer.parseInt(item);
                    if (qty == 0) setStyle("-fx-text-fill: -danger; -fx-font-weight: bold;");
                    else if (qty < 10) setStyle("-fx-text-fill: -warning; -fx-font-weight: bold;");
                    else setStyle("-fx-font-weight: bold;");
                } catch (NumberFormatException e) { setStyle(""); }
            }
        });

        TableColumn<ProductRow, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        brandCol.setMaxWidth(120);

        TableColumn<ProductRow, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setMaxWidth(150);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                ProductRow row = getTableView().getItems().get(getIndex());
                HBox box = new HBox(6);
                box.setAlignment(Pos.CENTER);
                int pid = Integer.parseInt(row.getProductId());
                Button viewBtn = new Button("View");
                viewBtn.setStyle("-fx-background-color: -primary-light; -fx-text-fill: -primary; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px; -fx-cursor: hand; -fx-font-weight: bold;");
                viewBtn.setOnAction(e -> mainLayout.showProductDetail(pid));
                Button deleteBtn = new Button("Delete");
                deleteBtn.setStyle("-fx-background-color: -danger-bg; -fx-text-fill: -danger; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px; -fx-cursor: hand; -fx-font-weight: bold;");
                deleteBtn.setOnAction(e -> deleteProduct(pid, row.getName()));
                box.getChildren().addAll(viewBtn, deleteBtn);
                setGraphic(box);
            }
        });

        tv.getColumns().addAll(idCol, nameCol, sellerCol, priceCol, statusCol, stockCol, brandCol, actionsCol);
        return tv;
    }

    private void loadAllProducts() {
        String token = SessionManager.getInstance().getToken();
        JSONObject result = adminApi.getAllProducts(token);
        productData.clear();
        if (result == null || !result.has("products")) return;
        JSONArray arr = result.getJSONArray("products");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject p = arr.getJSONObject(i);
            productData.add(new ProductRow(
                String.valueOf(p.optInt("productId")), p.optString("name", ""),
                String.valueOf(p.optInt("sellerId")), String.format("EGP %.2f", p.optDouble("price", 0)),
                p.optString("status", ""), String.valueOf(p.optInt("stock", 0)), p.optString("brand", "")
            ));
        }
    }

    private void deleteProduct(int productId, String name) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete product \"" + name + "\"?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Confirm Delete");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                boolean ok = adminApi.deleteProduct(SessionManager.getInstance().getToken(), productId);
                if (ok) loadAllProducts(); else AlertHelper.showError("Error", "Failed to delete product.");
            }
        });
    }

    public VBox getRoot() { return root; }

    public static class ProductRow {
        private final SimpleStringProperty productId, name, sellerId, price, status, stock, brand;
        public ProductRow(String pid, String n, String sid, String pr, String st, String stk, String br) {
            this.productId = new SimpleStringProperty(pid); this.name = new SimpleStringProperty(n);
            this.sellerId = new SimpleStringProperty(sid); this.price = new SimpleStringProperty(pr);
            this.status = new SimpleStringProperty(st); this.stock = new SimpleStringProperty(stk);
            this.brand = new SimpleStringProperty(br);
        }
        public String getProductId() { return productId.get(); } public String getName() { return name.get(); }
        public String getSellerId() { return sellerId.get(); } public String getPrice() { return price.get(); }
        public String getStatus() { return status.get(); } public String getStock() { return stock.get(); }
        public String getBrand() { return brand.get(); }
    }
}
