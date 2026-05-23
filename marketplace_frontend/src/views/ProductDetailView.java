package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.Product;
import models.Inventory;
import services.ProductApiService;
import services.InventoryApiService;
import services.CartApiService;
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

        Button backBtn = new Button("\u25C0 Back to Browse");
        backBtn.getStyleClass().addAll("button", "button-secondary");
        backBtn.setOnAction(e -> layout.navigateTo("browse"));

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

                // Left: Image
                StackPane imgPlaceholder = new StackPane();
                imgPlaceholder.getStyleClass().add("product-image-placeholder");
                imgPlaceholder.setPrefSize(400, 300);
                imgPlaceholder.setMinSize(400, 300);

                if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                    try {
                        javafx.scene.image.Image img = new javafx.scene.image.Image(product.getImageUrl(), 400, 300, true, true, true);
                        javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
                        imgView.setFitWidth(400);
                        imgView.setFitHeight(300);
                        imgView.setPreserveRatio(true);
                        imgPlaceholder.getChildren().add(imgView);
                    } catch (Exception e) {
                        Label imgIcon = new Label("\uD83D\uDCE6");
                        imgIcon.setStyle("-fx-font-size: 64px;");
                        imgPlaceholder.getChildren().add(imgIcon);
                    }
                } else {
                    Label imgIcon = new Label("\uD83D\uDCE6");
                    imgIcon.setStyle("-fx-font-size: 64px;");
                    imgPlaceholder.getChildren().add(imgIcon);
                }
                imgPlaceholder.setStyle("-fx-background-radius: 12;");

                // Right: Details
                VBox details = new VBox(16);
                details.setPadding(new Insets(0));
                HBox.setHgrow(details, Priority.ALWAYS);

                Label nameLabel = new Label(product.getName());
                nameLabel.getStyleClass().add("heading");
                nameLabel.setWrapText(true);

                Label brandLabel = new Label("Brand: " + (product.getBrand() != null && !product.getBrand().isEmpty() ? product.getBrand() : "Not specified"));
                brandLabel.setStyle("-fx-text-fill: -text-subtle; -fx-font-size: 14px;");

                Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
                priceLabel.getStyleClass().add("product-price");
                priceLabel.setStyle("-fx-font-size: 32px;");

                Label statusLabel = new Label(product.getStatus());
                statusLabel.getStyleClass().addAll("badge", "IN_STOCK".equals(product.getStatus()) ? "badge-available" : "badge-sold");

                Label descLabel = new Label(product.getDescription() != null && !product.getDescription().isEmpty() ? product.getDescription() : "No description available.");
                descLabel.setWrapText(true);
                descLabel.setStyle("-fx-font-size: 14px; -fx-line-spacing: 4;");

                int currentUserId = SessionManager.getInstance().getCurrentUser() != null ? SessionManager.getInstance().getCurrentUser().getUserId() : -1;

                VBox infoCard = new VBox(10);
                infoCard.getStyleClass().add("card");

                // User-friendly stock display
                boolean isOwner = (currentUserId == product.getSellerId());
                boolean hasStock = inventory != null && inventory.getQuantity() > 0;

                HBox stockRow = new HBox(10);
                stockRow.setAlignment(Pos.CENTER_LEFT);
                Label stockIcon = new Label(hasStock ? "\u2705" : "\u274C");
                stockIcon.setStyle("-fx-font-size: 16px;");
                String stockText = isOwner
                    ? "Stock: " + (inventory != null ? inventory.getQuantity() + " units" : "N/A")
                    : (hasStock ? "In Stock \u2014 Available Now" : "Out of Stock");
                Label stockInfo = new Label(stockText);
                stockInfo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                stockRow.getChildren().addAll(stockIcon, stockInfo);

                HBox sellerRow = new HBox(10);
                sellerRow.setAlignment(Pos.CENTER_LEFT);
                Label sellerIcon = new Label("\uD83C\uDFEA");
                sellerIcon.setStyle("-fx-font-size: 16px;");
                Label sellerInfo = new Label(isOwner ? "This is your product" : "Sold by a verified seller");
                sellerInfo.setStyle("-fx-font-size: 14px;");
                sellerRow.getChildren().addAll(sellerIcon, sellerInfo);

                infoCard.getChildren().addAll(stockRow, new Separator(), sellerRow);

                // Action buttons
                HBox actions = new HBox(12);

                if ("IN_STOCK".equals(product.getStatus()) && product.getSellerId() != currentUserId) {
                    // Add to Cart button
                    Button addCartBtn = new Button("\uD83D\uDED2 Add to Cart");
                    addCartBtn.getStyleClass().addAll("button", "button-success");
                    addCartBtn.setStyle("-fx-font-size: 14px; -fx-padding: 12 24;");
                    addCartBtn.setOnAction(e -> {
                        CartApiService cartApi = new CartApiService();
                        JSONObject result = cartApi.addToCart(currentUserId, product.getProductId(), 1);
                        if (result.optBoolean("success")) {
                            int cartCount = result.optInt("cartCount", 0);
                            SessionManager.getInstance().setCartItemCount(cartCount);
                            AlertHelper.showSuccess("Added to cart! (" + cartCount + " items)");
                        } else {
                            AlertHelper.showError("Error", result.optString("error", "Failed to add to cart"));
                        }
                    });
                    actions.getChildren().add(addCartBtn);
                }

                // Message seller button
                Button msgBtn = new Button("\uD83D\uDCAC Message Seller");
                msgBtn.getStyleClass().addAll("button", "button-outline");
                msgBtn.setOnAction(e -> layout.navigateToChat(product.getSellerId()));
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
