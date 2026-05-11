package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.Product;
import models.User;
import models.Account;
import services.SearchApiService;
import state.SessionManager;
import java.util.List;

public class HomeView {

    private final ScrollPane root;
    private final MainLayout layout;

    public HomeView(MainLayout layout) {
        this.layout = layout;
        VBox content = new VBox(24);
        content.setPadding(new Insets(0));

        User user = SessionManager.getInstance().getCurrentUser();
        Account account = SessionManager.getInstance().getAccount();

        // Welcome Banner
        VBox banner = new VBox(8);
        banner.getStyleClass().add("welcome-banner");
        Label welcomeTitle = new Label("Welcome back, " + (user != null ? user.getUsername() : "User") + "! \uD83D\uDC4B");
        welcomeTitle.getStyleClass().add("welcome-title");
        Label welcomeSub = new Label("Explore the marketplace, discover products, and manage your business.");
        welcomeSub.getStyleClass().add("welcome-subtitle");
        banner.getChildren().addAll(welcomeTitle, welcomeSub);

        // Stats cards
        HBox statsRow = new HBox(16);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        String balanceStr = account != null ? String.format("%.2f %s", account.getBalance(), account.getCurrency()) : "N/A";
        statsRow.getChildren().addAll(
            createStatCard("\uD83D\uDCB0", "Account Balance", balanceStr),
            createStatCard("\uD83D\uDC64", "Role", user != null ? user.getRole().toUpperCase() : "N/A"),
            createStatCard("\uD83D\uDCE7", "Email", user != null ? user.getEmail() : "N/A"),
            createStatCard("\uD83C\uDD94", "User ID", user != null ? "#" + user.getUserId() : "N/A")
        );

        // Quick Actions
        Label actionsTitle = new Label("Quick Actions");
        actionsTitle.getStyleClass().add("subheading");

        HBox actions = new HBox(12);
        Button browseBtnAction = new Button("\uD83D\uDCE6 Browse Products");
        browseBtnAction.setOnAction(e -> layout.navigateTo("browse"));
        Button searchBtnAction = new Button("\uD83D\uDD0D Search Products");
        searchBtnAction.setOnAction(e -> layout.navigateTo("search"));
        Button chatBtnAction = new Button("\uD83D\uDCAC Messages");
        chatBtnAction.getStyleClass().addAll("button", "button-outline");
        chatBtnAction.setOnAction(e -> layout.navigateTo("chat"));
        Button reportBtnAction = new Button("\uD83D\uDCC8 View Reports");
        reportBtnAction.getStyleClass().addAll("button", "button-secondary");
        reportBtnAction.setOnAction(e -> layout.navigateTo("reports"));
        actions.getChildren().addAll(browseBtnAction, searchBtnAction, chatBtnAction, reportBtnAction);

        // Featured Products
        Label featuredTitle = new Label("Featured Products");
        featuredTitle.getStyleClass().add("subheading");

        FlowPane productGrid = new FlowPane(16, 16);
        productGrid.setPadding(new Insets(0));

        // Load latest products
        new Thread(() -> {
            SearchApiService searchApi = new SearchApiService();
            List<Product> products = searchApi.filterProducts(null, null, null, null, null, null, null, "created_at", "DESC", 8, 0);
            javafx.application.Platform.runLater(() -> {
                if (products.isEmpty()) {
                    Label empty = new Label("No products available yet. Start by adding some!");
                    empty.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px;");
                    productGrid.getChildren().add(empty);
                } else {
                    for (Product p : products) {
                        productGrid.getChildren().add(createProductCard(p));
                    }
                }
            });
        }).start();

        content.getChildren().addAll(banner, statsRow, actionsTitle, actions, featuredTitle, productGrid);

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");
    }

    private VBox createStatCard(String icon, String label, String value) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(220);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label valLabel = new Label(value);
        valLabel.getStyleClass().add("stat-value");
        valLabel.setStyle("-fx-font-size: 18px;");

        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(iconLabel, valLabel, nameLabel);
        return card;
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(0);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(220);

        // Image placeholder
        StackPane imgPlaceholder = new StackPane();
        imgPlaceholder.getStyleClass().add("product-image-placeholder");
        imgPlaceholder.setPrefHeight(120);
        Label imgIcon = new Label("\uD83D\uDCE6");
        imgIcon.setStyle("-fx-font-size: 36px;");
        imgPlaceholder.getChildren().add(imgIcon);

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
        status.getStyleClass().addAll("badge", "AVAILABLE".equals(p.getStatus()) ? "badge-available" : "badge-sold");

        info.getChildren().addAll(name, brand, price, status);
        card.getChildren().addAll(imgPlaceholder, info);

        card.setOnMouseClicked(e -> layout.showProductDetail(p.getProductId()));

        return card;
    }

    public ScrollPane getRoot() {
        return root;
    }
}
