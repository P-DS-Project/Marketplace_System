package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.Product;
import services.ProductApiService;
import state.SessionManager;
import utils.AlertHelper;

public class EditProductView {

    private final ScrollPane root;
    private final MainLayout layout;

    public EditProductView(int productId, MainLayout layout) {
        this.layout = layout;
        VBox content = new VBox(24);
        content.setPadding(new Insets(0));

        Button backBtn = new Button("\u25C0 Back to My Products");
        backBtn.getStyleClass().addAll("button", "button-secondary");
        backBtn.setOnAction(e -> layout.navigateTo("myproducts"));

        Label title = new Label("\u270F\uFE0F Edit Product");
        title.getStyleClass().add("heading");

        Label loading = new Label("Loading product...");
        content.getChildren().addAll(backBtn, title, loading);

        ProductApiService productApi = new ProductApiService();

        new Thread(() -> {
            Product product = productApi.getProductDetails(productId);
            javafx.application.Platform.runLater(() -> {
                content.getChildren().clear();
                content.getChildren().addAll(backBtn, title);

                if (product == null) {
                    content.getChildren().add(new Label("Product not found."));
                    return;
                }

                VBox formCard = new VBox(20);
                formCard.getStyleClass().add("card");
                formCard.setMaxWidth(700);

                Label nameLabel = new Label("Product Name *");
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                TextField nameField = new TextField(product.getName());

                Label priceLabel = new Label("Price *");
                priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                TextField priceField = new TextField(String.valueOf(product.getPrice()));

                Label brandLabel = new Label("Brand");
                brandLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                TextField brandField = new TextField(product.getBrand() != null ? product.getBrand() : "");

                Label descLabel = new Label("Description");
                descLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                TextArea descField = new TextArea(product.getDescription() != null ? product.getDescription() : "");
                descField.setPrefRowCount(4);

                Label imgLabel = new Label("Image URL");
                imgLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                TextField imgField = new TextField(product.getImageUrl() != null ? product.getImageUrl() : "");
                imgField.setPromptText("https://example.com/image.jpg");

                Label errorLabel = new Label();
                errorLabel.getStyleClass().add("field-error-label");
                errorLabel.setManaged(false);
                errorLabel.setVisible(false);
                errorLabel.setWrapText(true);

                Button saveBtn = new Button("Save Changes");
                saveBtn.setMaxWidth(Double.MAX_VALUE);
                saveBtn.getStyleClass().addAll("button", "button-success");
                saveBtn.setStyle("-fx-font-size: 16px; -fx-padding: 14 24;");
                saveBtn.setOnAction(e -> {
                    errorLabel.setVisible(false);
                    errorLabel.setManaged(false);

                    String name = nameField.getText().trim();
                    String priceStr = priceField.getText().trim();
                    String brand = brandField.getText().trim();
                    String description = descField.getText().trim();
                    String imageUrl = imgField.getText().trim();

                    if (name.length() < 3) {
                        errorLabel.setText("Product name must be at least 3 characters.");
                        errorLabel.setVisible(true);
                        errorLabel.setManaged(true);
                        return;
                    }

                    double price;
                    try { price = Double.parseDouble(priceStr); } catch (NumberFormatException ex) {
                        errorLabel.setText("Enter a valid price.");
                        errorLabel.setVisible(true);
                        errorLabel.setManaged(true);
                        return;
                    }
                    if (price <= 0) {
                        errorLabel.setText("Price must be greater than zero.");
                        errorLabel.setVisible(true);
                        errorLabel.setManaged(true);
                        return;
                    }

                    String result = productApi.updateProduct(productId, name, price, description, brand, imageUrl);
                    if (!result.startsWith("ERROR")) {
                        AlertHelper.showSuccess("Product updated!");
                        layout.navigateTo("myproducts");
                    } else {
                        errorLabel.setText(result.replace("ERROR: ", ""));
                        errorLabel.setVisible(true);
                        errorLabel.setManaged(true);
                    }
                });

                formCard.getChildren().addAll(
                    nameLabel, nameField,
                    priceLabel, priceField,
                    brandLabel, brandField,
                    descLabel, descField,
                    imgLabel, imgField,
                    errorLabel, saveBtn
                );

                content.getChildren().add(formCard);
            });
        }).start();

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");
    }

    public ScrollPane getRoot() { return root; }
}
