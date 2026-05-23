package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.*;
import models.Product;
import services.ProductApiService;
import services.InventoryApiService;
import state.SessionManager;
import utils.AlertHelper;
import java.util.List;

public class SellerDashboardView {

    private final ScrollPane root;
    private final MainLayout layout;
    private final ProductApiService productApi = new ProductApiService();
    private final FlowPane productGrid;

    public SellerDashboardView(MainLayout layout) {
        this.layout = layout;
        VBox content = new VBox(20);
        content.setPadding(new Insets(0));

        Label title = new Label("\uD83D\uDED2 My Products");
        title.getStyleClass().add("heading");

        Button addBtn = new Button("\u2795 Add New Product");
        addBtn.setOnAction(e -> layout.navigateTo("addproduct"));

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, addBtn);

        // Product tiles grid
        productGrid = new FlowPane(16, 16);

        content.getChildren().addAll(header, productGrid);
        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");

        loadProducts();
    }

    private void loadProducts() {
        int userId = SessionManager.getInstance().getCurrentUser() != null ? SessionManager.getInstance().getCurrentUser().getUserId() : -1;
        new Thread(() -> {
            List<Product> products = productApi.getProductsBySeller(userId);
            javafx.application.Platform.runLater(() -> {
                productGrid.getChildren().clear();
                if (products.isEmpty()) {
                    VBox emptyState = new VBox(16);
                    emptyState.setAlignment(Pos.CENTER);
                    emptyState.setPadding(new Insets(60));
                    Label emptyIcon = new Label("\uD83D\uDCE6");
                    emptyIcon.setStyle("-fx-font-size: 64px;");
                    Label emptyText = new Label("No products yet");
                    emptyText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                    Label emptySub = new Label("Click 'Add New Product' to get started");
                    emptySub.setStyle("-fx-text-fill: #64748B;");
                    emptyState.getChildren().addAll(emptyIcon, emptyText, emptySub);
                    productGrid.getChildren().add(emptyState);
                } else {
                    for (Product p : products) {
                        productGrid.getChildren().add(createProductTile(p));
                    }
                }
            });
        }).start();
    }

    private VBox createProductTile(Product p) {
        VBox tile = new VBox(0);
        tile.getStyleClass().add("product-card");
        tile.setPrefWidth(250);

        // Image placeholder
        StackPane imgPlaceholder = new StackPane();
        imgPlaceholder.getStyleClass().add("product-image-placeholder");
        imgPlaceholder.setPrefHeight(160);
        imgPlaceholder.setMinHeight(160);

        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            try {
                javafx.scene.image.Image img = new javafx.scene.image.Image(p.getImageUrl(), 250, 140, true, true, true);
                javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
                imgView.setFitWidth(250);
                imgView.setFitHeight(140);
                imgView.setPreserveRatio(true);
                imgPlaceholder.getChildren().add(imgView);
            } catch (Exception e) {
                Label imgIcon = new Label("\uD83D\uDCE6");
                imgIcon.setStyle("-fx-font-size: 36px;");
                imgPlaceholder.getChildren().add(imgIcon);
            }
        } else {
            Label imgIcon = new Label("\uD83D\uDCE6");
            imgIcon.setStyle("-fx-font-size: 36px;");
            imgPlaceholder.getChildren().add(imgIcon);
        }

        VBox info = new VBox(8);
        info.setPadding(new Insets(14));

        Label name = new Label(p.getName());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);

        Label price = new Label(String.format("$%.2f", p.getPrice()));
        price.getStyleClass().add("product-price");

        Label status = new Label(p.getStatus());
        status.getStyleClass().add("badge");
        if ("IN_STOCK".equals(p.getStatus())) {
            status.getStyleClass().add("badge-available");
            status.setText("In Stock");
        } else if ("OUT_OF_STOCK".equals(p.getStatus())) {
            status.getStyleClass().add("badge-sold");
            status.setText("Out of Stock");
        } else {
            status.getStyleClass().add("badge-sold");
            status.setText("Unavailable");
        }

        // Stock Controls
        HBox stockRow = new HBox(8);
        stockRow.setAlignment(Pos.CENTER_LEFT);
        Label stockLabel = new Label("Stock: ...");
        stockLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-subtle;");
        Button decBtn = new Button("-");
        decBtn.setStyle("-fx-padding: 2 6;");
        Button incBtn = new Button("+");
        incBtn.setStyle("-fx-padding: 2 6;");
        
        stockRow.getChildren().addAll(stockLabel, decBtn, incBtn);
        
        final int[] currentStock = {0};
        new Thread(() -> {
            InventoryApiService invApi = new InventoryApiService();
            models.Inventory inv = invApi.getInventoryDetails(p.getProductId());
            javafx.application.Platform.runLater(() -> {
                if (inv != null) {
                    currentStock[0] = inv.getQuantity();
                    stockLabel.setText("Stock: " + currentStock[0]);
                } else {
                    stockLabel.setText("Stock: N/A");
                }
            });
        }).start();

        decBtn.setOnAction(e -> {
            if (currentStock[0] > 0) {
                new Thread(() -> {
                    InventoryApiService invApi = new InventoryApiService();
                    String res = invApi.reduceStock(p.getProductId(), 1, "Main_Warehouse");
                    if (!res.startsWith("ERROR")) {
                        currentStock[0]--;
                        if (currentStock[0] == 0) {
                            productApi.updateProductStatus(p.getProductId(), "OUT_OF_STOCK");
                        }
                        javafx.application.Platform.runLater(this::loadProducts);
                    }
                }).start();
            }
        });

        incBtn.setOnAction(e -> {
            new Thread(() -> {
                InventoryApiService invApi = new InventoryApiService();
                String res = invApi.addStock(p.getProductId(), 1, "Main_Warehouse");
                if (!res.startsWith("ERROR")) {
                    currentStock[0]++;
                    if (currentStock[0] == 1) {
                        productApi.updateProductStatus(p.getProductId(), "IN_STOCK");
                    }
                    javafx.application.Platform.runLater(this::loadProducts);
                }
            }).start();
        });

        // Action buttons
        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button editBtn = new Button("\u270F\uFE0F Edit");
        editBtn.getStyleClass().addAll("button", "button-outline");
        editBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 12px;");
        editBtn.setOnAction(e -> layout.navigateToEditProduct(p.getProductId()));

        Button statusBtn = new Button("UNAVAILABLE".equals(p.getStatus()) ? "\u2705 Make Available" : "\u274C Make Unavailable");
        statusBtn.getStyleClass().addAll("button", "button-secondary");
        statusBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 12px;");
        statusBtn.setOnAction(e -> {
            String newStatus = "UNAVAILABLE".equals(p.getStatus()) ? (currentStock[0] > 0 ? "IN_STOCK" : "OUT_OF_STOCK") : "UNAVAILABLE";
            String result = productApi.updateProductStatus(p.getProductId(), newStatus);
            if (!result.startsWith("ERROR")) loadProducts();
            else AlertHelper.showError("Error", result);
        });

        Button deleteBtn = new Button("\uD83D\uDDD1\uFE0F");
        deleteBtn.getStyleClass().addAll("button", "button-danger");
        deleteBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 12px;");
        deleteBtn.setOnAction(e -> {
            if (AlertHelper.showConfirm("Delete", "Delete " + p.getName() + "?")) {
                String result = productApi.removeProduct(p.getProductId());
                if (!result.startsWith("ERROR")) {
                    loadProducts();
                    AlertHelper.showSuccess("Product deleted.");
                } else {
                    AlertHelper.showError("Error", result);
                }
            }
        });

        actions.getChildren().addAll(editBtn, statusBtn, deleteBtn);

        info.getChildren().addAll(name, price, status, stockRow, actions);
        tile.getChildren().addAll(imgPlaceholder, info);

        return tile;
    }

    public ScrollPane getRoot() { return root; }
}
