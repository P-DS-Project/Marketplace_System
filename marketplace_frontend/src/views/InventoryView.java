package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.Inventory;
import services.InventoryApiService;
import utils.AlertHelper;

public class InventoryView {

    private final ScrollPane root;
    private final InventoryApiService inventoryApi = new InventoryApiService();

    public InventoryView() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(0));

        Label title = new Label("\uD83D\uDCCA Inventory Management");
        title.getStyleClass().add("heading");

        // Check Stock card
        VBox checkCard = new VBox(16);
        checkCard.getStyleClass().add("card");
        Label checkTitle = new Label("\uD83D\uDD0D Check Product Inventory");
        checkTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        HBox checkForm = new HBox(12);
        checkForm.setAlignment(Pos.CENTER_LEFT);
        TextField checkProductId = new TextField();
        checkProductId.setPromptText("Product ID");
        checkProductId.setPrefWidth(150);
        Button checkBtn = new Button("Check");
        checkBtn.getStyleClass().addAll("button", "button-outline");
        VBox checkResult = new VBox(8);

        checkBtn.setOnAction(e -> {
            try {
                int pid = Integer.parseInt(checkProductId.getText().trim());
                Inventory inv = inventoryApi.getInventoryDetails(pid);
                checkResult.getChildren().clear();
                if (inv != null) {
                    checkResult.getChildren().addAll(
                        new Label("Product ID: " + inv.getProductId()),
                        new Label("Quantity: " + inv.getQuantity()),
                        new Label("Warehouse: " + inv.getWarehouseNode())
                    );
                } else {
                    checkResult.getChildren().add(new Label("No inventory found for this product."));
                }
            } catch (NumberFormatException ex) { AlertHelper.showError("Error", "Enter valid product ID."); }
        });

        checkForm.getChildren().addAll(new Label("Product ID:"), checkProductId, checkBtn);
        checkCard.getChildren().addAll(checkTitle, checkForm, checkResult);

        // Add Stock card
        VBox addCard = new VBox(16);
        addCard.getStyleClass().add("card");
        Label addTitle = new Label("\u2795 Add Stock");
        addTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        HBox addForm = new HBox(12);
        addForm.setAlignment(Pos.CENTER_LEFT);
        TextField addPid = new TextField(); addPid.setPromptText("Product ID"); addPid.setPrefWidth(120);
        TextField addQty = new TextField(); addQty.setPromptText("Quantity"); addQty.setPrefWidth(100);
        TextField addWarehouse = new TextField(); addWarehouse.setPromptText("Warehouse node"); addWarehouse.setPrefWidth(150);
        Button addBtn = new Button("Add Stock");
        addBtn.getStyleClass().addAll("button", "button-success");
        addBtn.setOnAction(e -> {
            try {
                String result = inventoryApi.addStock(
                    Integer.parseInt(addPid.getText().trim()),
                    Integer.parseInt(addQty.getText().trim()),
                    addWarehouse.getText().trim()
                );
                if ("SUCCESS".equals(result)) AlertHelper.showSuccess("Stock added successfully!");
                else AlertHelper.showError("Error", result);
            } catch (NumberFormatException ex) { AlertHelper.showError("Error", "Enter valid numbers."); }
        });
        addForm.getChildren().addAll(
            new Label("Product:"), addPid, new Label("Qty:"), addQty,
            new Label("Warehouse:"), addWarehouse, addBtn
        );
        addCard.getChildren().addAll(addTitle, addForm);

        // Reduce Stock card
        VBox reduceCard = new VBox(16);
        reduceCard.getStyleClass().add("card");
        Label reduceTitle = new Label("\u2796 Reduce Stock");
        reduceTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        HBox reduceForm = new HBox(12);
        reduceForm.setAlignment(Pos.CENTER_LEFT);
        TextField redPid = new TextField(); redPid.setPromptText("Product ID"); redPid.setPrefWidth(120);
        TextField redQty = new TextField(); redQty.setPromptText("Quantity"); redQty.setPrefWidth(100);
        TextField redWarehouse = new TextField(); redWarehouse.setPromptText("Warehouse node"); redWarehouse.setPrefWidth(150);
        Button redBtn = new Button("Reduce Stock");
        redBtn.getStyleClass().addAll("button", "button-danger");
        redBtn.setOnAction(e -> {
            try {
                String result = inventoryApi.reduceStock(
                    Integer.parseInt(redPid.getText().trim()),
                    Integer.parseInt(redQty.getText().trim()),
                    redWarehouse.getText().trim()
                );
                if ("SUCCESS".equals(result)) AlertHelper.showSuccess("Stock reduced successfully!");
                else AlertHelper.showError("Error", result);
            } catch (NumberFormatException ex) { AlertHelper.showError("Error", "Enter valid numbers."); }
        });
        reduceForm.getChildren().addAll(
            new Label("Product:"), redPid, new Label("Qty:"), redQty,
            new Label("Warehouse:"), redWarehouse, redBtn
        );
        reduceCard.getChildren().addAll(reduceTitle, reduceForm);

        // Check availability card
        VBox availCard = new VBox(16);
        availCard.getStyleClass().add("card");
        Label availTitle = new Label("\u2705 Check Availability");
        availTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        HBox availForm = new HBox(12);
        availForm.setAlignment(Pos.CENTER_LEFT);
        TextField availPid = new TextField(); availPid.setPromptText("Product ID"); availPid.setPrefWidth(120);
        TextField availQty = new TextField(); availQty.setPromptText("Required Qty"); availQty.setPrefWidth(120);
        Label availResult = new Label("");
        Button availBtn = new Button("Check");
        availBtn.getStyleClass().addAll("button", "button-outline");
        availBtn.setOnAction(e -> {
            try {
                boolean inStock = inventoryApi.checkStock(
                    Integer.parseInt(availPid.getText().trim()),
                    Integer.parseInt(availQty.getText().trim())
                );
                availResult.setText(inStock ? "\u2705 In Stock!" : "\u274C Insufficient Stock");
                availResult.setStyle(inStock ? "-fx-text-fill: #10B981; -fx-font-weight: bold;" : "-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            } catch (NumberFormatException ex) { AlertHelper.showError("Error", "Enter valid numbers."); }
        });
        availForm.getChildren().addAll(new Label("Product:"), availPid, new Label("Need:"), availQty, availBtn, availResult);
        availCard.getChildren().addAll(availTitle, availForm);

        content.getChildren().addAll(title, checkCard, addCard, reduceCard, availCard);

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");
    }

    public ScrollPane getRoot() { return root; }
}
