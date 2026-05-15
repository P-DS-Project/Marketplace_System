package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import services.ReportApiService;
import services.ProductApiService;
import models.Product;
import state.SessionManager;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.List;

public class MyShopView {

    private final ScrollPane root;
    private final MainLayout layout;
    private final ReportApiService reportApi = new ReportApiService();
    private final ProductApiService productApi = new ProductApiService();
    private final VBox contentBox;

    public MyShopView(MainLayout layout) {
        this.layout = layout;
        contentBox = new VBox(24);
        contentBox.setPadding(new Insets(0));

        // Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("\uD83C\uDFEA My Shop");
        title.getStyleClass().add("heading");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button genReportBtn = new Button("\uD83D\uDCC4 Generate Report");
        genReportBtn.getStyleClass().addAll("button", "button-outline");
        genReportBtn.setOnAction(e -> {
            contentBox.getChildren().clear();
            contentBox.getChildren().add(header);
            Label loadingLabel = new Label("Generating fresh analytics...");
            loadingLabel.getStyleClass().add("subheading");
            loadingLabel.setStyle("-fx-text-fill: -text-subtle;");
            contentBox.getChildren().add(loadingLabel);
            loadDashboard();
        });

        Button manageBtn = new Button("\uD83D\uDCE6 Manage Products");
        manageBtn.setOnAction(e -> layout.navigateTo("myproducts"));
        header.getChildren().addAll(title, spacer, genReportBtn, manageBtn);

        contentBox.getChildren().add(header);

        // Loading state
        Label loadingLabel = new Label("Loading shop analytics...");
        loadingLabel.getStyleClass().add("subheading");
        loadingLabel.setStyle("-fx-text-fill: -text-subtle;");
        contentBox.getChildren().add(loadingLabel);

        root = new ScrollPane(contentBox);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");

        loadDashboard();
    }

    private void loadDashboard() {
        int userId = SessionManager.getInstance().getCurrentUser() != null
                ? SessionManager.getInstance().getCurrentUser().getUserId() : -1;

        new Thread(() -> {
            JSONObject salesReport = reportApi.getSalesReport(userId);
            JSONObject inventoryReport = reportApi.getInventoryReport();
            JSONObject systemStats = reportApi.getSystemStatistics();
            List<Product> myProducts = productApi.getProductsBySeller(userId);

            javafx.application.Platform.runLater(() -> {
                // Remove loading label
                if (contentBox.getChildren().size() > 1) {
                    contentBox.getChildren().remove(1);
                }

                // Sales Analytics Cards
                contentBox.getChildren().add(buildSalesSection(salesReport));

                // Inventory Summary
                contentBox.getChildren().add(buildInventorySection(inventoryReport));

                // Recent Sales
                contentBox.getChildren().add(buildRecentSalesSection(salesReport));

                // Add Chart
                if (salesReport != null) {
                    contentBox.getChildren().add(buildChartSection(salesReport));
                }

                // My Products Preview
                contentBox.getChildren().add(buildProductPreviewSection(myProducts));
            });
        }).start();
    }

    private VBox buildSalesSection(JSONObject salesReport) {
        VBox section = new VBox(16);

        Label sectionTitle = new Label("\uD83D\uDCC8 Sales Analytics");
        sectionTitle.getStyleClass().add("subheading");

        HBox statsRow = new HBox(16);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        double totalRevenue = 0;
        int totalSales = 0;
        int itemsSold = 0;
        int activeProducts = 0;
        double avgOrderValue = 0;

        if (salesReport != null) {
            totalRevenue = salesReport.optDouble("totalRevenue", 0);
            totalSales = salesReport.optInt("totalSales", 0);
            itemsSold = salesReport.optInt("totalItemsSold", 0);
            activeProducts = salesReport.optInt("activeProducts", 0);
            avgOrderValue = salesReport.optDouble("averageOrderValue", 0);
        }

        statsRow.getChildren().addAll(
            createStatCard("\uD83D\uDCB0", "Total Revenue", String.format("$%.2f", totalRevenue), "-success"),
            createStatCard("\uD83D\uDCC8", "Total Sales", String.valueOf(totalSales), "-primary"),
            createStatCard("\uD83D\uDCE6", "Items Sold", String.valueOf(itemsSold), "-accent"),
            createStatCard("\u2705", "Active Listings", String.valueOf(activeProducts), "-secondary"),
            createStatCard("\uD83D\uDCCA", "Avg Order Value", String.format("$%.2f", avgOrderValue), "-warning")
        );

        section.getChildren().addAll(sectionTitle, statsRow);
        return section;
    }

    private VBox createStatCard(String icon, String label, String value, String colorVar) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(180);
        card.setMinWidth(140);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        valueLabel.setStyle("-fx-font-size: 22px;");

        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(iconLabel, valueLabel, nameLabel);
        return card;
    }

    private VBox buildInventorySection(JSONObject inventoryReport) {
        VBox section = new VBox(16);

        Label sectionTitle = new Label("\uD83D\uDCE6 Inventory Summary");
        sectionTitle.getStyleClass().add("subheading");

        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        int totalProducts = 0;
        int inStock = 0;
        int outOfStock = 0;

        if (inventoryReport != null) {
            totalProducts = inventoryReport.optInt("totalProducts", 0);
            inStock = inventoryReport.optInt("inStockCount", 0);
            outOfStock = inventoryReport.optInt("outOfStockCount", 0);
        }

        HBox invRow = new HBox(24);
        invRow.setAlignment(Pos.CENTER_LEFT);

        invRow.getChildren().addAll(
            createMiniStat("Total Products", String.valueOf(totalProducts)),
            createMiniStat("In Stock", String.valueOf(inStock)),
            createMiniStat("Out of Stock", String.valueOf(outOfStock))
        );

        card.getChildren().add(invRow);

        // Low stock alerts
        if (inventoryReport != null) {
            JSONArray invArr = inventoryReport.optJSONArray("inventory");
            if (invArr != null) {
                VBox alertsBox = new VBox(6);
                boolean hasLowStock = false;
                for (int i = 0; i < invArr.length(); i++) {
                    JSONObject item = invArr.getJSONObject(i);
                    int qty = item.optInt("quantity", 0);
                    if (qty > 0 && qty <= 5) {
                        hasLowStock = true;
                        HBox alertRow = new HBox(8);
                        alertRow.setAlignment(Pos.CENTER_LEFT);
                        Label warn = new Label("\u26A0\uFE0F");
                        warn.setStyle("-fx-font-size: 14px;");
                        Label productName = new Label(item.optString("productName", "Unknown"));
                        productName.setStyle("-fx-font-weight: bold;");
                        Label stockInfo = new Label("Only " + qty + " left in stock");
                        stockInfo.setStyle("-fx-text-fill: -warning;");
                        alertRow.getChildren().addAll(warn, productName, stockInfo);
                        alertsBox.getChildren().add(alertRow);
                    }
                }
                if (hasLowStock) {
                    Label alertTitle = new Label("\u26A0\uFE0F Low Stock Alerts");
                    alertTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: -warning; -fx-font-size: 14px;");
                    card.getChildren().addAll(new Separator(), alertTitle, alertsBox);
                }
            }
        }

        section.getChildren().addAll(sectionTitle, card);
        return section;
    }

    private VBox createMiniStat(String label, String value) {
        VBox stat = new VBox(4);
        stat.setPrefWidth(140);
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        val.getStyleClass().add("stat-value");
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");
        stat.getChildren().addAll(val, lbl);
        return stat;
    }

    private VBox buildRecentSalesSection(JSONObject salesReport) {
        VBox section = new VBox(16);

        Label sectionTitle = new Label("\uD83D\uDCB3 Recent Sales");
        sectionTitle.getStyleClass().add("subheading");

        VBox card = new VBox(0);
        card.getStyleClass().add("card");

        if (salesReport != null) {
            JSONArray salesArr = salesReport.optJSONArray("sales");
            if (salesArr != null && salesArr.length() > 0) {
                // Table header
                HBox headerRow = new HBox(16);
                headerRow.setPadding(new Insets(0, 0, 8, 0));
                headerRow.setStyle("-fx-border-color: transparent transparent -border-color transparent; -fx-border-width: 0 0 1 0;");
                Label hTx = new Label("Transaction");
                hTx.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-subtle; -fx-font-size: 12px;");
                hTx.setPrefWidth(100);
                Label hBuyer = new Label("Buyer");
                hBuyer.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-subtle; -fx-font-size: 12px;");
                hBuyer.setPrefWidth(100);
                Label hQty = new Label("Qty");
                hQty.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-subtle; -fx-font-size: 12px;");
                hQty.setPrefWidth(60);
                Label hAmount = new Label("Amount");
                hAmount.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-subtle; -fx-font-size: 12px;");
                hAmount.setPrefWidth(100);
                Label hStatus = new Label("Status");
                hStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-subtle; -fx-font-size: 12px;");
                hStatus.setPrefWidth(100);
                headerRow.getChildren().addAll(hTx, hBuyer, hQty, hAmount, hStatus);
                card.getChildren().add(headerRow);

                int displayCount = Math.min(salesArr.length(), 10);
                for (int i = 0; i < displayCount; i++) {
                    JSONObject sale = salesArr.getJSONObject(i);
                    HBox row = new HBox(16);
                    row.setPadding(new Insets(8, 0, 8, 0));
                    row.setAlignment(Pos.CENTER_LEFT);
                    if (i < displayCount - 1) {
                        row.setStyle("-fx-border-color: transparent transparent -border-color transparent; -fx-border-width: 0 0 1 0;");
                    }

                    Label txId = new Label("TX #" + sale.optInt("transactionId"));
                    txId.setPrefWidth(100);
                    Label buyer = new Label("Buyer #" + sale.optInt("buyerId"));
                    buyer.setPrefWidth(100);
                    Label qty = new Label(String.valueOf(sale.optInt("quantity")));
                    qty.setPrefWidth(60);
                    Label amount = new Label(String.format("$%.2f", sale.optDouble("amount", 0)));
                    amount.setStyle("-fx-font-weight: bold;");
                    amount.setPrefWidth(100);
                    Label status = createBadge(sale.optString("status", ""));
                    status.setPrefWidth(100);

                    row.getChildren().addAll(txId, buyer, qty, amount, status);
                    card.getChildren().add(row);
                }
            } else {
                Label noSales = new Label("No sales recorded yet. Start listing products to see analytics here.");
                noSales.setStyle("-fx-text-fill: -text-subtle;");
                noSales.setWrapText(true);
                card.getChildren().add(noSales);
            }
        } else {
            Label error = new Label("Unable to load sales data. Check your connection.");
            error.setStyle("-fx-text-fill: -danger;");
            card.getChildren().add(error);
        }

        section.getChildren().addAll(sectionTitle, card);
        return section;
    }

    private VBox buildChartSection(JSONObject salesReport) {
        VBox section = new VBox(16);
        Label sectionTitle = new Label("\uD83D\uDCC8 Sales Trend");
        sectionTitle.getStyleClass().add("subheading");

        javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();
        xAxis.setLabel("Transaction");
        javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis();
        yAxis.setLabel("Amount ($)");

        javafx.scene.chart.LineChart<String, Number> lineChart = new javafx.scene.chart.LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Recent Sales Performance");
        lineChart.setLegendVisible(false);

        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();

        JSONArray salesArr = salesReport.optJSONArray("sales");
        if (salesArr != null) {
            int displayCount = Math.min(salesArr.length(), 10);
            for (int i = displayCount - 1; i >= 0; i--) {
                JSONObject sale = salesArr.getJSONObject(i);
                series.getData().add(new javafx.scene.chart.XYChart.Data<>("TX #" + sale.optInt("transactionId"), sale.optDouble("amount", 0)));
            }
        }
        lineChart.getData().add(series);

        VBox card = new VBox(lineChart);
        card.getStyleClass().add("card");

        section.getChildren().addAll(sectionTitle, card);
        return section;
    }

    private VBox buildProductPreviewSection(List<Product> products) {
        VBox section = new VBox(16);

        HBox sectionHeader = new HBox(12);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        Label sectionTitle = new Label("\uD83D\uDED2 My Products");
        sectionTitle.getStyleClass().add("subheading");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button seeMoreBtn = new Button("See All Products \u2192");
        seeMoreBtn.getStyleClass().addAll("button", "button-outline");
        seeMoreBtn.setOnAction(e -> layout.navigateTo("myproducts"));
        sectionHeader.getChildren().addAll(sectionTitle, spacer, seeMoreBtn);

        FlowPane previewGrid = new FlowPane(16, 16);

        if (products != null && !products.isEmpty()) {
            int previewCount = Math.min(products.size(), 4);
            for (int i = 0; i < previewCount; i++) {
                Product p = products.get(i);
                previewGrid.getChildren().add(createProductTile(p));
            }
        } else {
            VBox emptyState = new VBox(12);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(40));
            Label emptyIcon = new Label("\uD83D\uDCE6");
            emptyIcon.setStyle("-fx-font-size: 48px;");
            Label emptyText = new Label("No products listed yet");
            emptyText.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            Button addBtn = new Button("\u2795 Add Your First Product");
            addBtn.setOnAction(e -> layout.navigateTo("addproduct"));
            emptyState.getChildren().addAll(emptyIcon, emptyText, addBtn);
            previewGrid.getChildren().add(emptyState);
        }

        section.getChildren().addAll(sectionHeader, previewGrid);
        return section;
    }

    private VBox createProductTile(Product p) {
        VBox tile = new VBox(0);
        tile.getStyleClass().add("product-card");
        tile.setPrefWidth(220);
        tile.setCursor(javafx.scene.Cursor.HAND);
        tile.setOnMouseClicked(e -> layout.showProductDetail(p.getProductId()));

        StackPane imgPlaceholder = new StackPane();
        imgPlaceholder.getStyleClass().add("product-image-placeholder");
        imgPlaceholder.setPrefHeight(120);

        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            try {
                Image img = new Image(p.getImageUrl(), 220, 120, true, true, true);
                ImageView imgView = new ImageView(img);
                imgView.setFitWidth(220);
                imgView.setFitHeight(120);
                imgView.setPreserveRatio(true);
                imgPlaceholder.getChildren().add(imgView);
            } catch (Exception e) {
                Label imgIcon = new Label("\uD83D\uDCE6");
                imgIcon.setStyle("-fx-font-size: 28px;");
                imgPlaceholder.getChildren().add(imgIcon);
            }
        } else {
            Label imgIcon = new Label("\uD83D\uDCE6");
            imgIcon.setStyle("-fx-font-size: 28px;");
            imgPlaceholder.getChildren().add(imgIcon);
        }

        VBox info = new VBox(6);
        info.setPadding(new Insets(12));

        Label name = new Label(p.getName());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);

        Label price = new Label(String.format("$%.2f", p.getPrice()));
        price.getStyleClass().add("product-price");

        Label status = new Label(p.getStatus());
        status.getStyleClass().addAll("badge", "IN_STOCK".equals(p.getStatus()) ? "badge-available" : "badge-sold");

        info.getChildren().addAll(name, price, status);
        tile.getChildren().addAll(imgPlaceholder, info);

        return tile;
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
