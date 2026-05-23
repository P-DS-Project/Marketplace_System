package views;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import models.Product;
import models.User;
import org.json.JSONObject;
import services.SearchApiService;
import services.CartApiService;
import state.SessionManager;
import utils.AlertHelper;
import java.util.List;

public class HomeView {

    private final ScrollPane root;
    private final MainLayout layout;
    private final CartApiService cartApi = new CartApiService();

    public HomeView(MainLayout layout) {
        this.layout = layout;
        VBox content = new VBox(28);
        content.setPadding(new Insets(0));

        User user = SessionManager.getInstance().getCurrentUser();

        // Welcome Banner (matches example: text on left, big icon on right)
        VBox banner = new VBox(10);
        banner.getStyleClass().add("welcome-banner");

        HBox bannerContent = new HBox(20);
        bannerContent.setAlignment(Pos.CENTER_LEFT);

        VBox bannerText = new VBox(8);
        HBox.setHgrow(bannerText, Priority.ALWAYS);

        Label welcomeTitle = new Label(
                "Welcome back, " + (user != null ? user.getUsername() : "User") + "! \uD83D\uDC4B");
        welcomeTitle.getStyleClass().add("welcome-title");
        Label welcomeSub = new Label("Explore the marketplace, discover products, and manage your business.");
        welcomeSub.getStyleClass().add("welcome-subtitle");
        welcomeSub.setWrapText(true);
        bannerText.getChildren().addAll(welcomeTitle, welcomeSub);

        Label bannerIcon = new Label("\uD83D\uDE80");
        bannerIcon.setStyle("-fx-font-size: 56px;");

        bannerContent.getChildren().addAll(bannerText, bannerIcon);
        banner.getChildren().add(bannerContent);

        // Quick Actions
        Label actionsTitle = new Label("\u26A1 Quick Actions");
        actionsTitle.getStyleClass().add("subheading");

        HBox actions = new HBox(14);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button browseBtn = new Button("\uD83D\uDCE6  Browse Products");
        browseBtn.setOnAction(e -> layout.navigateTo("browse"));

        Button addProductBtn = new Button("\u2795 Add Product");
        addProductBtn.setOnAction(e -> layout.navigateTo("addproduct"));

        Button chatBtn = new Button("\uD83D\uDCAC  Messages");
        chatBtn.getStyleClass().addAll("button", "button-outline");
        chatBtn.setOnAction(e -> layout.navigateTo("chat"));

        Button myShopBtn = new Button("\uD83D\uDCC8 My Shop");
        myShopBtn.getStyleClass().addAll("button", "button-secondary");
        myShopBtn.setOnAction(e -> layout.navigateTo("myshop"));

        actions.getChildren().addAll(browseBtn, addProductBtn, chatBtn, myShopBtn);

        // Marketplace Insights stat cards
        Label insightsTitle = new Label("\uD83D\uDCCA Marketplace Insights");
        insightsTitle.getStyleClass().add("subheading");

        HBox insightsRow = new HBox(16);
        insightsRow.setAlignment(Pos.CENTER_LEFT);
        insightsRow.getChildren().addAll(
            createStatCard("\uD83D\uDEE1", "Payments & Data", "100% Secure"),
            createStatCard("\u2B50", "Verified Sellers", "Top Rated"),
            createStatCard("\uD83D\uDE9A", "Nationwide", "Fast Delivery"),
            createStatCard("\uD83C\uDF1F", "Premium Products", "Quality Assured")
        );

        // Featured Products section
        HBox featuredHeader = new HBox();
        featuredHeader.setAlignment(Pos.CENTER_LEFT);
        Label featuredTitle = new Label("\uD83C\uDF1F Recent Products");
        featuredTitle.getStyleClass().add("subheading");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Hyperlink seeMoreLink = new Hyperlink("See More \u25B6");
        seeMoreLink.getStyleClass().add("auth-link");
        seeMoreLink.setOnAction(e -> layout.navigateTo("browse"));
        featuredHeader.getChildren().addAll(featuredTitle, spacer, seeMoreLink);

        FlowPane productGrid = new FlowPane(18, 18);
        productGrid.setPadding(new Insets(0));

        // Loading indicator
        Label loadingLabel = new Label("\u23F3 Loading products...");
        loadingLabel.setStyle("-fx-text-fill: -text-subtle; -fx-font-size: 14px;");
        productGrid.getChildren().add(loadingLabel);

        // Popular Products section
        HBox popularHeader = new HBox();
        popularHeader.setAlignment(Pos.CENTER_LEFT);
        Label popularTitle = new Label("\uD83D\uDD25 Most Popular");
        popularTitle.getStyleClass().add("subheading");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        Hyperlink seeMoreLink2 = new Hyperlink("See More \u25B6");
        seeMoreLink2.getStyleClass().add("auth-link");
        seeMoreLink2.setOnAction(e -> layout.navigateTo("browse"));
        popularHeader.getChildren().addAll(popularTitle, spacer2, seeMoreLink2);

        FlowPane popularGrid = new FlowPane(18, 18);
        popularGrid.setPadding(new Insets(0));

        Label loadingLabel2 = new Label("\u23F3 Loading products...");
        loadingLabel2.setStyle("-fx-text-fill: -text-subtle; -fx-font-size: 14px;");
        popularGrid.getChildren().add(loadingLabel2);

        content.getChildren().addAll(banner, actionsTitle, actions, insightsTitle, insightsRow,
                featuredHeader, productGrid, popularHeader, popularGrid);

        // Load data in background
        new Thread(() -> {
            SearchApiService searchApi = new SearchApiService();
            List<Product> recentProducts = searchApi.filterProducts(null, null, null, null, null, null, null, null,
                    "created_at", "DESC", 4, 0);
            List<Product> popularProducts = searchApi.filterProducts(null, null, null, null, null, null, null, null,
                    "price", "DESC", 4, 0);

            javafx.application.Platform.runLater(() -> {
                // Recent products with staggered animation
                productGrid.getChildren().clear();
                if (recentProducts.isEmpty()) {
                    Label empty = new Label("\uD83D\uDCE6  No products available yet. Start by adding some!");
                    empty.setStyle("-fx-text-fill: -text-subtle; -fx-font-size: 14px; -fx-padding: 20;");
                    productGrid.getChildren().add(empty);
                } else {
                    for (int i = 0; i < recentProducts.size(); i++) {
                        Product p = recentProducts.get(i);
                        VBox card = createProductCard(p);
                        card.setOpacity(0);
                        card.setTranslateY(20);
                        productGrid.getChildren().add(card);

                        final int delay = i * 80;
                        javafx.application.Platform.runLater(() -> {
                            FadeTransition fade = new FadeTransition(Duration.millis(400), card);
                            fade.setDelay(Duration.millis(delay));
                            fade.setFromValue(0);
                            fade.setToValue(1);
                            TranslateTransition slide = new TranslateTransition(Duration.millis(400), card);
                            slide.setDelay(Duration.millis(delay));
                            slide.setFromY(20);
                            slide.setToY(0);
                            fade.play();
                            slide.play();
                        });
                    }
                }

                // Popular products with staggered animation
                popularGrid.getChildren().clear();
                if (popularProducts.isEmpty()) {
                    Label empty = new Label("No popular products yet.");
                    empty.setStyle("-fx-text-fill: -text-subtle; -fx-font-size: 14px;");
                    popularGrid.getChildren().add(empty);
                } else {
                    for (int i = 0; i < popularProducts.size(); i++) {
                        Product p = popularProducts.get(i);
                        VBox card = createProductCard(p);
                        card.setOpacity(0);
                        card.setTranslateY(20);
                        popularGrid.getChildren().add(card);

                        final int delay = i * 80;
                        javafx.application.Platform.runLater(() -> {
                            FadeTransition fade = new FadeTransition(Duration.millis(400), card);
                            fade.setDelay(Duration.millis(delay));
                            fade.setFromValue(0);
                            fade.setToValue(1);
                            TranslateTransition slide = new TranslateTransition(Duration.millis(400), card);
                            slide.setDelay(Duration.millis(delay));
                            slide.setFromY(20);
                            slide.setToY(0);
                            fade.play();
                            slide.play();
                        });
                    }
                }
            });
        }).start();

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");
    }

    private VBox createStatCard(String icon, String label, String value) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(200);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 28px;");

        Label valLabel = new Label(value);
        valLabel.getStyleClass().add("stat-value");
        valLabel.setStyle("-fx-font-size: 20px;");

        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(iconLabel, valLabel, nameLabel);
        return card;
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(0);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(230);

        // Image placeholder
        StackPane imgPlaceholder = new StackPane();
        imgPlaceholder.getStyleClass().add("product-image-placeholder");
        imgPlaceholder.setPrefHeight(160);
        imgPlaceholder.setMinHeight(160);

        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            try {
                javafx.scene.image.Image img = new javafx.scene.image.Image(p.getImageUrl(), 230, 160, true, true, true);
                javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
                imgView.setFitWidth(230);
                imgView.setFitHeight(160);
                imgView.setPreserveRatio(true);
                imgPlaceholder.getChildren().add(imgView);
            } catch (Exception e) {
                Label imgIcon = new Label("\uD83D\uDCE6");
                imgIcon.setStyle("-fx-font-size: 44px;");
                imgPlaceholder.getChildren().add(imgIcon);
            }
        } else {
            Label imgIcon = new Label("\uD83D\uDCE6");
            imgIcon.setStyle("-fx-font-size: 44px;");
            imgPlaceholder.getChildren().add(imgIcon);
        }

        VBox info = new VBox(8);
        info.setPadding(new Insets(14));

        Label name = new Label(p.getName());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);

        Label price = new Label(String.format("$%.2f", p.getPrice()));
        price.getStyleClass().add("product-price");

        Label brand = new Label(p.getBrand() != null && !p.getBrand().isEmpty() ? p.getBrand() : "No brand");
        brand.getStyleClass().add("product-brand");

        // Bottom row: status badge + add to cart button
        HBox bottomRow = new HBox(8);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        boolean isAvailable = "IN_STOCK".equals(p.getStatus()) || "AVAILABLE".equals(p.getStatus());
        Label status = new Label(p.getStatus());
        status.getStyleClass().addAll("badge", isAvailable ? "badge-available" : "badge-sold");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addToCartBtn = new Button("\uD83D\uDED2");
        addToCartBtn.getStyleClass().addAll("button", "button-success");
        addToCartBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 12px;");
        addToCartBtn.setDisable(!isAvailable);
        addToCartBtn.setOnAction(e -> {
            e.consume();
            int userId = SessionManager.getInstance().getCurrentUser() != null
                    ? SessionManager.getInstance().getCurrentUser().getUserId() : -1;
            if (userId == -1) { AlertHelper.showError("Not logged in", "Please log in first."); return; }
            new Thread(() -> {
                JSONObject result = cartApi.addToCart(userId, p.getProductId(), 1);
                javafx.application.Platform.runLater(() -> {
                    if (result != null && result.optBoolean("success")) {
                        AlertHelper.showSuccess(p.getName() + " added to cart!");
                    } else {
                        String err = result != null ? result.optString("error", "Failed") : "Failed to add to cart.";
                        AlertHelper.showError("Cart Error", err);
                    }
                });
            }).start();
        });

        bottomRow.getChildren().addAll(status, spacer, addToCartBtn);

        info.getChildren().addAll(name, brand, price, bottomRow);
        card.getChildren().addAll(imgPlaceholder, info);

        card.setOnMouseClicked(e -> layout.showProductDetail(p.getProductId()));

        return card;
    }

    public ScrollPane getRoot() {
        return root;
    }
}
