package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.Product;
import services.SearchApiService;
import java.util.List;

public class BrowseView {

    private final ScrollPane root;
    private final MainLayout layout;
    private final SearchApiService searchApi = new SearchApiService();
    private FlowPane productGrid;
    private int selectedCategory = -1;
    private String sortBy = "created_at";
    private String sortOrder = "DESC";
    private int currentPage = 0;
    private static final int PAGE_SIZE = 12;

    public BrowseView(MainLayout layout) {
        this.layout = layout;
        VBox content = new VBox(20);
        content.setPadding(new Insets(0));

        Label title = new Label("Browse Products");
        title.getStyleClass().add("heading");

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
                // Rebuild categories to update active state
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
        HBox pagination = new HBox(8);
        pagination.setAlignment(Pos.CENTER);
        Button prevBtn = new Button("\u25C0 Previous");
        prevBtn.getStyleClass().addAll("button", "button-secondary");
        prevBtn.setOnAction(e -> { if (currentPage > 0) { currentPage--; loadProducts(); } });
        Label pageLabel = new Label("Page " + (currentPage + 1));
        Button nextBtn = new Button("Next \u25B6");
        nextBtn.getStyleClass().addAll("button", "button-secondary");
        nextBtn.setOnAction(e -> { currentPage++; loadProducts(); });
        pagination.getChildren().addAll(prevBtn, pageLabel, nextBtn);

        content.getChildren().addAll(title, categories, sortRow, productGrid, pagination);

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");

        loadProducts();
    }

    private void loadProducts() {
        new Thread(() -> {
            List<Product> products;
            if (selectedCategory > 0) {
                products = searchApi.getProductsByCategory(selectedCategory, sortBy, sortOrder, PAGE_SIZE, currentPage * PAGE_SIZE);
            } else {
                products = searchApi.filterProducts(null, null, null, null, null, null, null, sortBy, sortOrder, PAGE_SIZE, currentPage * PAGE_SIZE);
            }
            javafx.application.Platform.runLater(() -> {
                productGrid.getChildren().clear();
                if (products.isEmpty()) {
                    Label empty = new Label("No products found in this category.");
                    empty.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px; -fx-padding: 40;");
                    productGrid.getChildren().add(empty);
                } else {
                    for (Product p : products) {
                        productGrid.getChildren().add(createProductCard(p));
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
