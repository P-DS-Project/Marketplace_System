package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.Product;
import models.User;
import models.Account;
import services.SearchApiService;
import services.ReportApiService;
import state.SessionManager;
import org.json.JSONObject;
import java.util.List;

public class HomeView {

    private final ScrollPane root;
    private final MainLayout layout;

    public HomeView(MainLayout layout) {
        this.layout = layout;
        VBox content = new VBox(28);
        content.setPadding(new Insets(0));

        User user = SessionManager.getInstance().getCurrentUser();

        // Welcome Banner
        VBox banner = new VBox(8);
        banner.getStyleClass().add("welcome-banner");
        Label welcomeTitle = new Label(
                "Welcome back, " + (user != null ? user.getUsername() : "User") + "! \uD83D\uDC4B");
        welcomeTitle.getStyleClass().add("welcome-title");
        Label welcomeSub = new Label("Explore the marketplace, discover products, and manage your business.");
        welcomeSub.getStyleClass().add("welcome-subtitle");
        banner.getChildren().addAll(welcomeTitle, welcomeSub);

        // Quick Actions
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_LEFT);
        Button browseBtn = new Button("\uD83D\uDCE6 Browse Products");
        browseBtn.setOnAction(e -> layout.navigateTo("browse"));
        Button addProductBtn = new Button("\u2795 Add Product");
        addProductBtn.setOnAction(e -> layout.navigateTo("addproduct"));
        Button chatBtn = new Button("\uD83D\uDCAC Messages");
        chatBtn.getStyleClass().addAll("button", "button-outline");
        chatBtn.setOnAction(e -> layout.navigateTo("chat"));
        Button myShopBtn = new Button("\uD83D\uDCC8 My Shop");
        myShopBtn.getStyleClass().addAll("button", "button-secondary");
        myShopBtn.setOnAction(e -> layout.navigateTo("myshop"));
        actions.getChildren().addAll(browseBtn, addProductBtn, chatBtn, myShopBtn);

        content.getChildren().addAll(banner, actions);

        // Marketplace Insights
        HBox insightsRow = new HBox(16);
        insightsRow.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().add(createSectionHeader("Marketplace Insights", null, null));
        content.getChildren().add(insightsRow);

        // Recent Products section
        Label recentHeader = createSectionHeader("Recent Products", "See More \u25B6",
                () -> layout.navigateTo("browse"));
        FlowPane recentGrid = new FlowPane(16, 16);
        content.getChildren().addAll(recentHeader, recentGrid);

        // Most Popular section
        Label popularHeader = createSectionHeader("Most Popular", "See More \u25B6", () -> layout.navigateTo("browse"));
        FlowPane popularGrid = new FlowPane(16, 16);
        content.getChildren().addAll(popularHeader, popularGrid);

        // Load data
        new Thread(() -> {
            SearchApiService searchApi = new SearchApiService();
            ReportApiService reportApi = new ReportApiService();

            // Recent products
            List<Product> recentProducts = searchApi.filterProducts(null, null, null, null, null, null, null, null,
                    "created_at", "DESC", 4, 0);
            // Popular products (by price desc as a proxy for popularity)
            List<Product> popularProducts = searchApi.filterProducts(null, null, null, null, null, null, null, null,
                    "price", "DESC", 4, 0);

            // Stats
            JSONObject statsReport = reportApi.getSystemStatistics();

            javafx.application.Platform.runLater(() -> {
                // Insights
                insightsRow.getChildren().clear();
                insightsRow.getChildren().addAll(
                        createStatCard("\uD83D\uDEE1\uFE0F", "Payments & Data", "100% Secure"),
                        createStatCard("\u2B50", "Verified Sellers", "Top Rated"),
                        createStatCard("\uD83D\uDE9A", "Nationwide", "Fast Delivery"),
                        createStatCard("\uD83C\uDF1F", "Premium Products", "Quality Assured"));

                // Recent products
                if (recentProducts.isEmpty()) {
                    Label empty = new Label("No products available yet. Start by adding some!");
                    empty.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px;");
                    recentGrid.getChildren().add(empty);
                } else {
                    for (Product p : recentProducts) {
                        recentGrid.getChildren().add(createProductCard(p));
                    }
                }

                // Popular products
                if (popularProducts.isEmpty()) {
                    Label empty = new Label("No popular products yet.");
                    empty.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px;");
                    popularGrid.getChildren().add(empty);
                } else {
                    for (Product p : popularProducts) {
                        popularGrid.getChildren().add(createProductCard(p));
                    }
                }
            });
        }).start();

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");
    }

    private Label createSectionHeader(String text, String actionText, Runnable action) {
        if (actionText == null) {
            Label header = new Label(text);
            header.getStyleClass().add("subheading");
            return header;
        }

        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        Label header = new Label(text);
        header.getStyleClass().add("subheading");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Hyperlink link = new Hyperlink(actionText);
        link.getStyleClass().add("auth-link");
        link.setOnAction(e -> {
            if (action != null)
                action.run();
        });
        row.getChildren().addAll(header, spacer, link);

        // Return just the header label since HBox can't be returned as Label
        // We'll add the HBox to content directly instead
        return header;
    }

    private VBox createStatCard(String icon, String label, String value) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(200);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label valLabel = new Label(value);
        valLabel.getStyleClass().add("stat-value");
        valLabel.setStyle("-fx-font-size: 22px;");

        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(iconLabel, valLabel, nameLabel);
        return card;
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(0);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(220);

        StackPane imgPlaceholder = new StackPane();
        imgPlaceholder.getStyleClass().add("product-image-placeholder");
        imgPlaceholder.setPrefHeight(120);

        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            try {
                javafx.scene.image.Image img = new javafx.scene.image.Image(p.getImageUrl(), 220, 120, true, true,
                        true);
                javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
                imgView.setFitWidth(220);
                imgView.setFitHeight(120);
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

        VBox info = new VBox(6);
        info.setPadding(new Insets(12));

        Label name = new Label(p.getName());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);

        Label price = new Label(String.format("$%.2f", p.getPrice()));
        price.getStyleClass().add("product-price");

        Label brand = new Label(p.getBrand() != null && !p.getBrand().isEmpty() ? p.getBrand() : "No brand");
        brand.getStyleClass().add("product-brand");

        Label status = new Label(p.getStatus());
        status.getStyleClass().addAll("badge", "IN_STOCK".equals(p.getStatus()) ? "badge-available" : "badge-sold");

        info.getChildren().addAll(name, brand, price, status);
        card.getChildren().addAll(imgPlaceholder, info);

        card.setOnMouseClicked(e -> layout.showProductDetail(p.getProductId()));

        return card;
    }

    public ScrollPane getRoot() {
        return root;
    }
}
