package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.Product;
import models.Inventory;
import services.ProductApiService;
import services.InventoryApiService;
import services.TransactionApiService;
import state.SessionManager;
import utils.AlertHelper;
import org.json.JSONObject;

public class ProductDetailView {

    private final ScrollPane root;

    public ProductDetailView(int productId, MainLayout layout) {
        VBox content = new VBox(24);
        content.setPadding(new Insets(0));

        ProductApiService productApi = new ProductApiService();
        InventoryApiService inventoryApi = new InventoryApiService();

        // Back button
        Button backBtn = new Button("\u25C0 Back to Browse");
        backBtn.getStyleClass().addAll("button", "button-secondary");
        backBtn.setOnAction(e -> layout.navigateTo("browse"));

        // Loading state
        Label loading = new Label("Loading product details...");
        content.getChildren().addAll(backBtn, loading);

        new Thread(() -> {
            Product product = productApi.getProductDetails(productId);
            Inventory inventory = inventoryApi.getInventoryDetails(productId);

            javafx.application.Platform.runLater(() -> {
                content.getChildren().clear();
                content.getChildren().add(backBtn);

                if (product == null) {
                    content.getChildren().add(new Label("Product not found."));
                    return;
                }

                HBox mainRow = new HBox(32);

                // Left: Image placeholder
                StackPane imgPlaceholder = new StackPane();
                imgPlaceholder.getStyleClass().add("product-image-placeholder");
                imgPlaceholder.setPrefSize(400, 300);
                imgPlaceholder.setMinSize(400, 300);
                Label imgIcon = new Label("\uD83D\uDCE6");
                imgIcon.setStyle("-fx-font-size: 64px;");
                imgPlaceholder.getChildren().add(imgIcon);
                imgPlaceholder.setStyle("-fx-background-radius: 12;");

                // Right: Details
                VBox details = new VBox(16);
                details.setPadding(new Insets(0));
                HBox.setHgrow(details, Priority.ALWAYS);

                Label nameLabel = new Label(product.getName());
                nameLabel.getStyleClass().add("heading");
                nameLabel.setWrapText(true);

                Label brandLabel = new Label("Brand: " + (product.getBrand() != null && !product.getBrand().isEmpty() ? product.getBrand() : "Not specified"));
                brandLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px;");

                Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
                priceLabel.getStyleClass().add("product-price");
                priceLabel.setStyle("-fx-font-size: 32px;");

                Label statusLabel = new Label(product.getStatus());
                statusLabel.getStyleClass().addAll("badge", "AVAILABLE".equals(product.getStatus()) ? "badge-available" : "badge-sold");

                Label descLabel = new Label(product.getDescription() != null && !product.getDescription().isEmpty() ? product.getDescription() : "No description available.");
                descLabel.setWrapText(true);
                descLabel.setStyle("-fx-font-size: 14px; -fx-line-spacing: 4;");

                // Product info card
                VBox infoCard = new VBox(8);
                infoCard.getStyleClass().add("card");
                infoCard.getChildren().addAll(
                    new Label("Product ID: " + product.getProductId()),
                    new Label("Seller ID: " + product.getSellerId()),
                    new Label("Category ID: " + product.getCategoryId()),
                    new Label("Stock: " + (inventory != null ? inventory.getQuantity() + " units" : "N/A")),
                    new Label("Warehouse: " + (inventory != null ? inventory.getWarehouseNode() : "N/A"))
                );

                // Action buttons
                HBox actions = new HBox(12);
                int currentUserId = SessionManager.getInstance().getCurrentUser() != null ? SessionManager.getInstance().getCurrentUser().getUserId() : -1;

                if ("AVAILABLE".equals(product.getStatus()) && product.getSellerId() != currentUserId) {
                    // Buy button
                    Button buyBtn = new Button("\uD83D\uDED2 Buy Now");
                    buyBtn.getStyleClass().addAll("button", "button-success");
                    buyBtn.setOnAction(e -> {
                        TextInputDialog dialog = new TextInputDialog("1");
                        dialog.setTitle("Purchase Product");
                        dialog.setHeaderText("Buy: " + product.getName());
                        dialog.setContentText("Quantity:");
                        dialog.showAndWait().ifPresent(qty -> {
                            try {
                                int q = Integer.parseInt(qty);
                                TransactionApiService txApi = new TransactionApiService();
                                JSONObject result = txApi.buy(currentUserId, product.getProductId(), q);
                                if (result.optBoolean("success")) {
                                    AlertHelper.showSuccess("Purchase successful! Paid: $" + result.optDouble("totalPaid", 0));
                                } else {
                                    AlertHelper.showError("Purchase Failed", result.optString("error", "Unknown error"));
                                }
                            } catch (NumberFormatException ex) {
                                AlertHelper.showError("Invalid Input", "Please enter a valid number.");
                            }
                        });
                    });
                    actions.getChildren().add(buyBtn);
                }

                // Message seller button
                Button msgBtn = new Button("\uD83D\uDCAC Message Seller");
                msgBtn.getStyleClass().addAll("button", "button-outline");
                msgBtn.setOnAction(e -> layout.navigateTo("chat"));
                actions.getChildren().add(msgBtn);

                details.getChildren().addAll(nameLabel, brandLabel, priceLabel, statusLabel, new Separator(), descLabel, infoCard, actions);

                mainRow.getChildren().addAll(imgPlaceholder, details);
                content.getChildren().add(mainRow);
            });
        }).start();

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");
    }

    public ScrollPane getRoot() { return root; }
}
