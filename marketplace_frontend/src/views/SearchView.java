package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.Product;
import services.SearchApiService;
import java.util.List;

public class SearchView {

    private final ScrollPane root;
    private final SearchApiService searchApi = new SearchApiService();
    private FlowPane resultGrid;
    private Label resultCount;

    public SearchView(String initialQuery) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(0));

        Label title = new Label("\uD83D\uDD0D Advanced Search");
        title.getStyleClass().add("heading");

        // Search bar
        HBox searchBar = new HBox(12);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        TextField keywordField = new TextField(initialQuery != null ? initialQuery : "");
        keywordField.setPromptText("Search by product name...");
        keywordField.setPrefWidth(300);
        keywordField.getStyleClass().add("search-field");

        Button searchBtn = new Button("\uD83D\uDD0D Search");
        searchBtn.setOnAction(e -> performSearch(keywordField.getText(), null, null, null, null, null));
        keywordField.setOnAction(e -> performSearch(keywordField.getText(), null, null, null, null, null));
        searchBar.getChildren().addAll(keywordField, searchBtn);

        // Filter panel
        VBox filterPanel = new VBox(12);
        filterPanel.getStyleClass().add("card");

        Label filterTitle = new Label("\u2699\uFE0F Filters");
        filterTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

        HBox filterRow1 = new HBox(16);
        filterRow1.setAlignment(Pos.CENTER_LEFT);

        VBox brandBox = new VBox(4);
        brandBox.getChildren().addAll(new Label("Brand"), createTextField("Any brand"));
        TextField brandField = (TextField) brandBox.getChildren().get(1);

        VBox minPriceBox = new VBox(4);
        minPriceBox.getChildren().addAll(new Label("Min Price"), createTextField("0"));
        TextField minPriceField = (TextField) minPriceBox.getChildren().get(1);

        VBox maxPriceBox = new VBox(4);
        maxPriceBox.getChildren().addAll(new Label("Max Price"), createTextField("10000"));
        TextField maxPriceField = (TextField) maxPriceBox.getChildren().get(1);

        VBox categoryBox = new VBox(4);
        categoryBox.getChildren().add(new Label("Category ID"));
        TextField catField = new TextField();
        catField.setPromptText("Any");
        categoryBox.getChildren().add(catField);

        filterRow1.getChildren().addAll(brandBox, minPriceBox, maxPriceBox, categoryBox);

        Button applyFilters = new Button("Apply Filters");
        applyFilters.setOnAction(e -> {
            String brand = brandField.getText().trim().isEmpty() ? null : brandField.getText().trim();
            Double minPrice = parseDouble(minPriceField.getText());
            Double maxPrice = parseDouble(maxPriceField.getText());
            Integer catId = parseInt(catField.getText());
            performSearch(keywordField.getText(), brand, minPrice, maxPrice, catId, null);
        });

        Button clearFilters = new Button("Clear");
        clearFilters.getStyleClass().addAll("button", "button-secondary");
        clearFilters.setOnAction(e -> {
            brandField.clear(); minPriceField.clear(); maxPriceField.clear(); catField.clear();
            performSearch(keywordField.getText(), null, null, null, null, null);
        });

        HBox filterActions = new HBox(8);
        filterActions.getChildren().addAll(applyFilters, clearFilters);

        filterPanel.getChildren().addAll(filterTitle, filterRow1, filterActions);

        // Results
        resultCount = new Label("");
        resultCount.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748B;");

        resultGrid = new FlowPane(16, 16);

        content.getChildren().addAll(title, searchBar, filterPanel, resultCount, resultGrid);

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");

        if (initialQuery != null && !initialQuery.isEmpty()) {
            performSearch(initialQuery, null, null, null, null, null);
        }
    }

    private void performSearch(String keyword, String brand, Double minPrice, Double maxPrice, Integer catId, Integer sellerId) {
        new Thread(() -> {
            List<Product> products;
            if (keyword != null && !keyword.trim().isEmpty()) {
                products = searchApi.searchByKeyword(keyword.trim(), brand, "created_at", "DESC", 50, 0);
            } else {
                products = searchApi.filterProducts(catId, minPrice, maxPrice, brand, sellerId, null, null, "created_at", "DESC", 50, 0);
            }
            javafx.application.Platform.runLater(() -> {
                resultGrid.getChildren().clear();
                resultCount.setText(products.size() + " product(s) found");
                if (products.isEmpty()) {
                    Label empty = new Label("No products match your search criteria.");
                    empty.setStyle("-fx-text-fill: #64748B; -fx-padding: 40;");
                    resultGrid.getChildren().add(empty);
                } else {
                    for (Product p : products) {
                        resultGrid.getChildren().add(createProductCard(p));
                    }
                }
            });
        }).start();
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(0);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(220);

        StackPane imgPlaceholder = new StackPane();
        imgPlaceholder.getStyleClass().add("product-image-placeholder");
        imgPlaceholder.setPrefHeight(100);
        Label imgIcon = new Label("\uD83D\uDCE6");
        imgIcon.setStyle("-fx-font-size: 28px;");
        imgPlaceholder.getChildren().add(imgIcon);

        VBox info = new VBox(4);
        info.setPadding(new Insets(10));
        Label name = new Label(p.getName());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);
        Label price = new Label(String.format("$%.2f", p.getPrice()));
        price.getStyleClass().add("product-price");
        Label status = new Label(p.getStatus());
        status.getStyleClass().addAll("badge", "AVAILABLE".equals(p.getStatus()) ? "badge-available" : "badge-sold");
        info.getChildren().addAll(name, price, status);
        card.getChildren().addAll(imgPlaceholder, info);
        return card;
    }

    private TextField createTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(150);
        return tf;
    }

    private Double parseDouble(String s) {
        try { return s != null && !s.trim().isEmpty() ? Double.parseDouble(s.trim()) : null; }
        catch (NumberFormatException e) { return null; }
    }
    private Integer parseInt(String s) {
        try { return s != null && !s.trim().isEmpty() ? Integer.parseInt(s.trim()) : null; }
        catch (NumberFormatException e) { return null; }
    }

    public ScrollPane getRoot() { return root; }
}
