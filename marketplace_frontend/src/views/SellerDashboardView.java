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
        imgPlaceholder.setPrefHeight(140);

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
        status.getStyleClass().addAll("badge", "AVAILABLE".equals(p.getStatus()) ? "badge-available" : "badge-sold");

        // Action buttons
        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button editBtn = new Button("\u270F\uFE0F Edit");
        editBtn.getStyleClass().addAll("button", "button-outline");
        editBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 12px;");
        editBtn.setOnAction(e -> layout.navigateToEditProduct(p.getProductId()));

        Button statusBtn = new Button("AVAILABLE".equals(p.getStatus()) ? "\u274C Mark Sold" : "\u2705 Mark Available");
        statusBtn.getStyleClass().addAll("button", "button-secondary");
        statusBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 12px;");
        statusBtn.setOnAction(e -> {
            String newStatus = "AVAILABLE".equals(p.getStatus()) ? "SOLD" : "AVAILABLE";
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

        info.getChildren().addAll(name, price, status, actions);
        tile.getChildren().addAll(imgPlaceholder, info);

        return tile;
    }

    public ScrollPane getRoot() { return root; }
}
