package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.ProductApiService;
import services.InventoryApiService;
import state.SessionManager;
import utils.AlertHelper;

public class AddProductView {

    private final ScrollPane root;
    private final MainLayout layout;

    public AddProductView(MainLayout layout) {
        this.layout = layout;
        VBox content = new VBox(24);
        content.setPadding(new Insets(0));

        // Back button
        Button backBtn = new Button("\u25C0 Back to My Products");
        backBtn.getStyleClass().addAll("button", "button-secondary");
        backBtn.setOnAction(e -> layout.navigateTo("myproducts"));

        Label title = new Label("\u2795 Add New Product");
        title.getStyleClass().add("heading");

        // Form card
        VBox formCard = new VBox(20);
        formCard.getStyleClass().add("card");
        formCard.setMaxWidth(700);

        // Name
        Label nameLabel = new Label("Product Name *");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter product name (min 3 characters)");

        // Price
        Label priceLabel = new Label("Price *");
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        TextField priceField = new TextField();
        priceField.setPromptText("0.00");

        // Category
        Label catLabel = new Label("Category *");
        catLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        ComboBox<String> catCombo = new ComboBox<>();
        catCombo.getItems().addAll("Electronics", "Clothing", "Home & Garden", "Sports", "Books", "Toys", "Automotive");
        catCombo.setPromptText("Select category");
        catCombo.setMaxWidth(Double.MAX_VALUE);

        // Brand
        Label brandLabel = new Label("Brand");
        brandLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        TextField brandField = new TextField();
        brandField.setPromptText("Brand name (optional)");

        // Description
        Label descLabel = new Label("Description");
        descLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        TextArea descField = new TextArea();
        descField.setPromptText("Product description...");
        descField.setPrefRowCount(4);

        // Image URL
        Label imgLabel = new Label("Image URL");
        imgLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        TextField imgField = new TextField();
        imgField.setPromptText("https://example.com/image.jpg (optional)");

        // Initial Quantity
        Label qtyLabel = new Label("Initial Stock Quantity *");
        qtyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        TextField qtyField = new TextField();
        qtyField.setPromptText("Number of items in stock");
        qtyField.setText("1");

        // Error label
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("field-error-label");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        // Submit button
        Button submitBtn = new Button("Add Product");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.getStyleClass().addAll("button", "button-success");
        submitBtn.setStyle("-fx-font-size: 16px; -fx-padding: 14 24;");
        submitBtn.setOnAction(e -> {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            String name = nameField.getText().trim();
            String priceStr = priceField.getText().trim();
            String brand = brandField.getText().trim();
            String description = descField.getText().trim();
            String imageUrl = imgField.getText().trim();
            String qtyStr = qtyField.getText().trim();

            if (name.length() < 3) {
                showError(errorLabel, "Product name must be at least 3 characters.");
                return;
            }
            if (catCombo.getValue() == null) {
                showError(errorLabel, "Please select a category.");
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException ex) {
                showError(errorLabel, "Enter a valid price.");
                return;
            }
            if (price <= 0) {
                showError(errorLabel, "Price must be greater than zero.");
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(qtyStr);
            } catch (NumberFormatException ex) {
                showError(errorLabel, "Enter a valid stock quantity.");
                return;
            }
            if (quantity < 0) {
                showError(errorLabel, "Quantity cannot be negative.");
                return;
            }

            int categoryId = catCombo.getItems().indexOf(catCombo.getValue()) + 1;
            int sellerId = SessionManager.getInstance().getCurrentUser().getUserId();

            ProductApiService productApi = new ProductApiService();
            String result = productApi.addProduct(sellerId, categoryId, name, price, description, brand, imageUrl);

            if (!result.startsWith("ERROR")) {
                if (quantity > 0) {
                    InventoryApiService inventoryApi = new InventoryApiService();
                    try {
                        org.json.JSONObject resJson = new org.json.JSONObject(result);
                        int productId = resJson.optInt("productId", -1);
                        if (productId != -1) {
                            inventoryApi.addStock(productId, quantity, "Main_Warehouse");
                        }
                    } catch (Exception ignored) {
                        System.out.println("Could not parse productId for inventory.");
                    }
                }
                AlertHelper.showSuccess("Product added successfully!");
                layout.navigateTo("myproducts");
            } else {
                showError(errorLabel, result.replace("ERROR: ", ""));
            }
        });

        formCard.getChildren().addAll(
                nameLabel, nameField,
                priceLabel, priceField,
                catLabel, catCombo,
                brandLabel, brandField,
                descLabel, descField,
                imgLabel, imgField,
                qtyLabel, qtyField,
                errorLabel, submitBtn);

        content.getChildren().addAll(backBtn, title, formCard);

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");
    }

    private void showError(Label errorLabel, String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    public ScrollPane getRoot() {
        return root;
    }
}
