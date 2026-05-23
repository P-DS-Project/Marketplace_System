package views;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import models.Product;
import org.json.JSONObject;
import services.SearchApiService;
import services.CartApiService;
import state.SessionManager;
import utils.AlertHelper;
import java.util.List;

public class BrowseView {

    private final ScrollPane root;
    private final MainLayout layout;
    private final SearchApiService searchApi = new SearchApiService();
    private final CartApiService cartApi = new CartApiService();
    private FlowPane productGrid;
    private int selectedCategory = -1;
    private String sortBy = "created_at";
    private String sortOrder = "DESC";
    private int currentPage = 0;
    private static final int PAGE_SIZE = 12;
    private String searchQuery = null;
    private String brandFilter = null;
    private Double minPrice = null;
    private Double maxPrice = null;
    private Label pageLabel;
    private Button prevBtn;
    private Button nextBtn;

    public BrowseView(MainLayout layout) {
        this.layout = layout;
        VBox content = new VBox(20);
        content.setPadding(new Insets(0));

        Label title = new Label("Browse Products");
        title.getStyleClass().add("heading");

        // Search bar (merged from SearchView)
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0D Search products by name...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(400);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> {
            String query = searchField.getText().trim();
            searchQuery = query.isEmpty() ? null : query;
            currentPage = 0;
            loadProducts();
        });
        searchField.setOnAction(e -> searchBtn.fire());

        Button clearSearchBtn = new Button("Clear");
        clearSearchBtn.getStyleClass().addAll("button", "button-secondary");
        clearSearchBtn.setOnAction(e -> {
            searchField.clear();
            searchQuery = null;
            currentPage = 0;
            loadProducts();
        });

        searchRow.getChildren().addAll(searchField, searchBtn, clearSearchBtn);

        // Filter panel (collapsible)
        VBox filterPanel = new VBox(12);
        filterPanel.getStyleClass().add("card");
        filterPanel.setStyle("-fx-padding: 16;");

        HBox filterRow = new HBox(16);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        TextField brandField = new TextField();
        brandField.setPromptText("Brand");
        brandField.setPrefWidth(150);

        TextField minPriceField = new TextField();
        minPriceField.setPromptText("Min $");
        minPriceField.setPrefWidth(80);

        TextField maxPriceField = new TextField();
        maxPriceField.setPromptText("Max $");
        maxPriceField.setPrefWidth(80);

        Button applyFilter = new Button("Apply Filters");
        applyFilter.getStyleClass().addAll("button", "button-outline");
        applyFilter.setStyle("-fx-padding: 8 16;");
        applyFilter.setOnAction(e -> {
            brandFilter = brandField.getText().trim().isEmpty() ? null : brandField.getText().trim();
            try { minPrice = minPriceField.getText().trim().isEmpty() ? null : Double.parseDouble(minPriceField.getText().trim()); }
            catch (NumberFormatException ex) { minPrice = null; }
            try { maxPrice = maxPriceField.getText().trim().isEmpty() ? null : Double.parseDouble(maxPriceField.getText().trim()); }
            catch (NumberFormatException ex) { maxPrice = null; }
            currentPage = 0;
            loadProducts();
        });

        Button resetFilter = new Button("Reset");
        resetFilter.getStyleClass().addAll("button", "button-secondary");
        resetFilter.setStyle("-fx-padding: 8 16;");
        resetFilter.setOnAction(e -> {
            brandField.clear();
            minPriceField.clear();
            maxPriceField.clear();
            brandFilter = null;
            minPrice = null;
            maxPrice = null;
            currentPage = 0;
            loadProducts();
        });

        filterRow.getChildren().addAll(
            new Label("Brand:"), brandField,
            new Label("Price:"), minPriceField, new Label("-"), maxPriceField,
            applyFilter, resetFilter
        );

        filterPanel.getChildren().add(filterRow);

        // Category chips
        HBox categories = new HBox(10);
        categories.setAlignment(Pos.CENTER_LEFT);
        String[] catNames = {"All", "Electronics", "Clothing", "Home & Garden", "Sports", "Books", "Toys", "Automotive"};
        int[] catIds = {-1, 1, 2, 3, 4, 5, 6, 7};
        for (int i = 0; i < catNames.length; i++) {
            final int catId = catIds[i];
            Button chip = new Button(catNames[i]);
            chip.getStyleClass().addAll("button", "category-chip");
            if (catId == selectedCategory) chip.getStyleClass().add("category-chip-active");
            chip.setOnAction(e -> {
                selectedCategory = catId;
                currentPage = 0;
                loadProducts();
                // Update active state
                categories.getChildren().forEach(c -> {
                    c.getStyleClass().remove("category-chip-active");
                });
                chip.getStyleClass().add("category-chip-active");
            });
            categories.getChildren().add(chip);
        }

        // Sort controls
        HBox sortRow = new HBox(12);
        sortRow.setAlignment(Pos.CENTER_LEFT);
        Label sortLabel = new Label("Sort by:");
        sortLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Newest", "Price: Low to High", "Price: High to Low", "Name A-Z");
        sortCombo.setValue("Newest");
        sortCombo.setOnAction(e -> {
            switch (sortCombo.getValue()) {
                case "Price: Low to High": sortBy = "price"; sortOrder = "ASC"; break;
                case "Price: High to Low": sortBy = "price"; sortOrder = "DESC"; break;
                case "Name A-Z": sortBy = "name"; sortOrder = "ASC"; break;
                default: sortBy = "created_at"; sortOrder = "DESC"; break;
            }
            currentPage = 0;
            loadProducts();
        });
        sortRow.getChildren().addAll(sortLabel, sortCombo);

        // Product grid
        productGrid = new FlowPane(16, 16);

        // Pagination
        HBox pagination = new HBox(12);
        pagination.setAlignment(Pos.CENTER);
        prevBtn = new Button("\u25C0 Previous");
        prevBtn.getStyleClass().addAll("button", "button-secondary");
        prevBtn.setOnAction(e -> { if (currentPage > 0) { currentPage--; loadProducts(); } });
        pageLabel = new Label("Page " + (currentPage + 1));
        pageLabel.setStyle("-fx-font-weight: bold;");
        nextBtn = new Button("Next \u25B6");
        nextBtn.getStyleClass().addAll("button", "button-secondary");
        nextBtn.setOnAction(e -> { currentPage++; loadProducts(); });
        pagination.getChildren().addAll(prevBtn, pageLabel, nextBtn);

        content.getChildren().addAll(title, searchRow, filterPanel, categories, sortRow, productGrid, pagination);

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");

        loadProducts();
    }

    private void loadProducts() {
        pageLabel.setText("Page " + (currentPage + 1));

        new Thread(() -> {
            Integer catId = selectedCategory > 0 ? selectedCategory : null;
            List<Product> products = searchApi.filterProducts(
                searchQuery, catId, minPrice, maxPrice, brandFilter,
                null, null, null, sortBy, sortOrder, PAGE_SIZE + 1, currentPage * PAGE_SIZE
            );
            javafx.application.Platform.runLater(() -> {
                productGrid.getChildren().clear();
                boolean hasNext = products.size() > PAGE_SIZE;
                List<Product> displayList = hasNext ? products.subList(0, PAGE_SIZE) : products;

                if (displayList.isEmpty()) {
                    VBox emptyState = new VBox(8);
                    emptyState.setAlignment(Pos.CENTER);
                    emptyState.setPadding(new Insets(40));
                    Label emptyIcon = new Label("📦");
                    emptyIcon.setStyle("-fx-font-size: 40px;");
                    Label empty = new Label("No products found matching your criteria.");
                    empty.setStyle("-fx-text-fill: -text-subtle; -fx-font-size: 14px;");
                    emptyState.getChildren().addAll(emptyIcon, empty);
                    productGrid.getChildren().add(emptyState);
                } else {
                    for (int i = 0; i < displayList.size(); i++) {
                        Product p = displayList.get(i);
                        VBox card = createProductCard(p);
                        card.setOpacity(0);
                        card.setTranslateY(16);
                        productGrid.getChildren().add(card);

                        final int delay = i * 50;
                        javafx.application.Platform.runLater(() -> {
                            FadeTransition fade = new FadeTransition(Duration.millis(350), card);
                            fade.setDelay(Duration.millis(delay));
                            fade.setFromValue(0);
                            fade.setToValue(1);
                            TranslateTransition slide = new TranslateTransition(Duration.millis(350), card);
                            slide.setDelay(Duration.millis(delay));
                            slide.setFromY(16);
                            slide.setToY(0);
                            fade.play();
                            slide.play();
                        });
                    }
                }
                prevBtn.setDisable(currentPage == 0);
                nextBtn.setDisable(!hasNext);
            });
        }).start();
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(0);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(230);

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
                Label imgIcon = new Label("🖼️");
                imgIcon.setStyle("-fx-font-size: 44px;");
                imgPlaceholder.getChildren().add(imgIcon);
            }
        } else {
            Label imgIcon = new Label("🖼️");
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

        Button addToCartBtn = new Button("🛒");
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
