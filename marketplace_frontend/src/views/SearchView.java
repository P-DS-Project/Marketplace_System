package views;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import models.Product;
import org.json.JSONObject;
import services.CartApiService;
import services.SearchApiService;
import state.SessionManager;
import utils.AlertHelper;
import java.util.List;

public class SearchView {

    private final ScrollPane root;
    private final SearchApiService searchApi = new SearchApiService();
    private final CartApiService cartApi = new CartApiService();
    private final MainLayout layout;
    private FlowPane resultGrid;
    private Label resultCount;

    public SearchView(String initialQuery) {
        this(initialQuery, null);
    }

    public SearchView(String initialQuery, MainLayout layout) {
        this.layout = layout;
        VBox content = new VBox(24);
        content.setPadding(new Insets(0));

        // Results header
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("\uD83D\uDD0D");
        icon.setStyle("-fx-font-size: 28px;");
        Label title = new Label("Search Results");
        title.getStyleClass().add("heading");
        resultCount = new Label("");
        resultCount.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #94A3B8;");
        headerRow.getChildren().addAll(icon, title, resultCount);

        // Filters card
        VBox filterPanel = new VBox(12);
        filterPanel.getStyleClass().add("card");

        Label filterTitle = new Label("\u2699 Refine Results");
        filterTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

        HBox filterRow = new HBox(16);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        VBox brandBox = new VBox(4);
        Label brandLbl = new Label("Brand");
        brandLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #94A3B8;");
        TextField brandField = new TextField();
        brandField.setPromptText("Any brand");
        brandField.setPrefWidth(150);
        brandBox.getChildren().addAll(brandLbl, brandField);

        VBox minPriceBox = new VBox(4);
        Label minLbl = new Label("Min Price");
        minLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #94A3B8;");
        TextField minPriceField = new TextField();
        minPriceField.setPromptText("$0");
        minPriceField.setPrefWidth(100);
        minPriceBox.getChildren().addAll(minLbl, minPriceField);

        VBox maxPriceBox = new VBox(4);
        Label maxLbl = new Label("Max Price");
        maxLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #94A3B8;");
        TextField maxPriceField = new TextField();
        maxPriceField.setPromptText("$10000");
        maxPriceField.setPrefWidth(100);
        maxPriceBox.getChildren().addAll(maxLbl, maxPriceField);

        Button applyFilters = new Button("Apply Filters");
        applyFilters.getStyleClass().addAll("button", "button-outline");
        applyFilters.setStyle("-fx-padding: 10 20;");
        applyFilters.setOnAction(e -> {
            String brand = brandField.getText().trim().isEmpty() ? null : brandField.getText().trim();
            Double minPrice = parseDouble(minPriceField.getText());
            Double maxPrice = parseDouble(maxPriceField.getText());
            performSearch(initialQuery, brand, minPrice, maxPrice, null, null);
        });

        Button clearFilters = new Button("Clear");
        clearFilters.getStyleClass().addAll("button", "button-secondary");
        clearFilters.setStyle("-fx-padding: 10 16;");
        clearFilters.setOnAction(e -> {
            brandField.clear();
            minPriceField.clear();
            maxPriceField.clear();
            performSearch(initialQuery, null, null, null, null, null);
        });

        filterRow.getChildren().addAll(brandBox, minPriceBox, maxPriceBox, applyFilters, clearFilters);
        filterPanel.getChildren().addAll(filterTitle, filterRow);

        // Results grid
        resultGrid = new FlowPane(18, 18);

        content.getChildren().addAll(headerRow, filterPanel, resultGrid);

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");

        if (initialQuery != null && !initialQuery.isEmpty()) {
            performSearch(initialQuery, null, null, null, null, null);
        }
    }

    private void performSearch(String keyword, String brand, Double minPrice, Double maxPrice, Integer catId,
            Integer sellerId) {
        resultGrid.getChildren().clear();
        Label loading = new Label("\u23F3 Searching...");
        loading.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px;");
        resultGrid.getChildren().add(loading);

        new Thread(() -> {
            List<Product> products;
            if (keyword != null && !keyword.trim().isEmpty()) {
                products = searchApi.searchByKeyword(keyword.trim(), brand, "created_at", "DESC", 50, 0);
            } else {
                products = searchApi.filterProducts(keyword, catId, minPrice, maxPrice, brand, sellerId, null, null,
                        "created_at", "DESC", 50, 0);
            }
            javafx.application.Platform.runLater(() -> {
                resultGrid.getChildren().clear();
                int count = products.size();
                resultCount.setText("  —  " + count + " result" + (count == 1 ? "" : "s") + " found");

                if (products.isEmpty()) {
                    VBox emptyState = new VBox(12);
                    emptyState.setAlignment(Pos.CENTER);
                    emptyState.setPadding(new Insets(60));
                    Label emptyIcon = new Label("\uD83D\uDD0D");
                    emptyIcon.setStyle("-fx-font-size: 48px;");
                    Label empty = new Label("No products match your search.");
                    empty.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 15px;");
                    emptyState.getChildren().addAll(emptyIcon, empty);
                    resultGrid.getChildren().add(emptyState);
                } else {
                    for (int i = 0; i < products.size(); i++) {
                        Product p = products.get(i);
                        VBox card = createProductCard(p);
                        card.setOpacity(0);
                        card.setTranslateY(16);
                        resultGrid.getChildren().add(card);

                        final int delay = i * 40;
                        javafx.application.Platform.runLater(() -> {
                            FadeTransition fade = new FadeTransition(Duration.millis(300), card);
                            fade.setDelay(Duration.millis(delay));
                            fade.setFromValue(0);
                            fade.setToValue(1);
                            TranslateTransition slide = new TranslateTransition(Duration.millis(300), card);
                            slide.setDelay(Duration.millis(delay));
                            slide.setFromY(16);
                            slide.setToY(0);
                            fade.play();
                            slide.play();
                        });
                    }
                }
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
                imgView.setFitWidth(230); imgView.setFitHeight(160);
                imgView.setPreserveRatio(true);
                imgPlaceholder.getChildren().add(imgView);
            } catch (Exception e) {
                Label icon = new Label("\uD83D\uDDBC");
                icon.setStyle("-fx-font-size: 44px;");
                imgPlaceholder.getChildren().add(icon);
            }
        } else {
            Label icon = new Label("\uD83D\uDDBC");
            icon.setStyle("-fx-font-size: 44px;");
            imgPlaceholder.getChildren().add(icon);
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

        HBox bottomRow = new HBox(8);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        boolean isAvailable = "IN_STOCK".equals(p.getStatus()) || "AVAILABLE".equals(p.getStatus());
        Label status = new Label(isAvailable ? "In Stock" : p.getStatus());
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
                        String err = result != null ? result.optString("error", "Failed") : "Failed.";
                        AlertHelper.showError("Cart Error", err);
                    }
                });
            }).start();
        });

        bottomRow.getChildren().addAll(status, spacer, addToCartBtn);
        info.getChildren().addAll(name, brand, price, bottomRow);
        card.getChildren().addAll(imgPlaceholder, info);

        if (layout != null) {
            card.setOnMouseClicked(e -> layout.showProductDetail(p.getProductId()));
        }

        return card;
    }

    private Double parseDouble(String s) {
        try { return s != null && !s.trim().isEmpty() ? Double.parseDouble(s.trim()) : null; }
        catch (NumberFormatException e) { return null; }
    }

    public ScrollPane getRoot() { return root; }
}
