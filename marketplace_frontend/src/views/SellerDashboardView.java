package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.collections.*;
import models.Product;
import services.ProductApiService;
import state.SessionManager;
import utils.AlertHelper;
import java.util.List;

public class SellerDashboardView {

    private final ScrollPane root;
    private final MainLayout layout;
    private final ProductApiService productApi = new ProductApiService();
    private final TableView<Product> table = new TableView<>();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    public SellerDashboardView(MainLayout layout) {
        this.layout = layout;
        VBox content = new VBox(20);
        content.setPadding(new Insets(0));

        Label title = new Label("\uD83D\uDED2 My Products");
        title.getStyleClass().add("heading");

        Button addBtn = new Button("+ Add New Product");
        addBtn.setOnAction(e -> showAddProductForm());

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, addBtn);

        // Table
        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        idCol.setPrefWidth(60);

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);

        TableColumn<Product, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        TableColumn<Product, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(260);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button statusBtn = new Button("Toggle Status");
            {
                editBtn.getStyleClass().addAll("button", "button-outline");
                editBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 12px;");
                deleteBtn.getStyleClass().addAll("button", "button-danger");
                deleteBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 12px;");
                statusBtn.getStyleClass().addAll("button", "button-secondary");
                statusBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 12px;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Product p = getTableView().getItems().get(getIndex());
                editBtn.setOnAction(e -> showEditForm(p));
                deleteBtn.setOnAction(e -> {
                    if (AlertHelper.showConfirm("Delete Product", "Are you sure you want to delete " + p.getName() + "?")) {
                        String result = productApi.removeProduct(p.getProductId());
                        if (!result.startsWith("ERROR")) { loadProducts(); AlertHelper.showSuccess("Product deleted."); }
                        else AlertHelper.showError("Error", result);
                    }
                });
                statusBtn.setOnAction(e -> {
                    String newStatus = "AVAILABLE".equals(p.getStatus()) ? "SOLD" : "AVAILABLE";
                    String result = productApi.updateProductStatus(p.getProductId(), newStatus);
                    if (!result.startsWith("ERROR")) { loadProducts(); } else AlertHelper.showError("Error", result);
                });
                HBox box = new HBox(6, editBtn, statusBtn, deleteBtn);
                setGraphic(box);
            }
        });

        table.getColumns().addAll(idCol, nameCol, priceCol, statusCol, actionsCol);
        table.setItems(productList);
        table.setPrefHeight(500);

        content.getChildren().addAll(header, table);
        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");

        loadProducts();
    }

    private void loadProducts() {
        int userId = SessionManager.getInstance().getCurrentUser() != null ? SessionManager.getInstance().getCurrentUser().getUserId() : -1;
        new Thread(() -> {
            List<Product> products = productApi.getProductsBySeller(userId);
            javafx.application.Platform.runLater(() -> {
                productList.clear();
                productList.addAll(products);
            });
        }).start();
    }

    private void showAddProductForm() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add New Product");
        dialog.setHeaderText("Enter product details");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField nameField = new TextField(); nameField.setPromptText("Product name");
        TextField priceField = new TextField(); priceField.setPromptText("Price");
        TextField catField = new TextField(); catField.setPromptText("Category ID");
        TextArea descField = new TextArea(); descField.setPromptText("Description"); descField.setPrefRowCount(3);

        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Price:"), 0, 1); grid.add(priceField, 1, 1);
        grid.add(new Label("Category ID:"), 0, 2); grid.add(catField, 1, 2);
        grid.add(new Label("Description:"), 0, 3); grid.add(descField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    int sellerId = SessionManager.getInstance().getCurrentUser().getUserId();
                    String result = productApi.addProduct(sellerId, Integer.parseInt(catField.getText()),
                        nameField.getText(), Double.parseDouble(priceField.getText()), descField.getText());
                    if (!result.startsWith("ERROR")) { loadProducts(); AlertHelper.showSuccess("Product added!"); }
                    else AlertHelper.showError("Error", result);
                } catch (NumberFormatException ex) { AlertHelper.showError("Invalid Input", "Check price and category ID."); }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void showEditForm(Product p) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Product");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField nameField = new TextField(p.getName());
        TextField priceField = new TextField(String.valueOf(p.getPrice()));
        TextArea descField = new TextArea(p.getDescription()); descField.setPrefRowCount(3);

        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Price:"), 0, 1); grid.add(priceField, 1, 1);
        grid.add(new Label("Description:"), 0, 2); grid.add(descField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String result = productApi.updateProduct(p.getProductId(), nameField.getText(),
                        Double.parseDouble(priceField.getText()), descField.getText());
                    if (!result.startsWith("ERROR")) { loadProducts(); AlertHelper.showSuccess("Product updated!"); }
                    else AlertHelper.showError("Error", result);
                } catch (NumberFormatException ex) { AlertHelper.showError("Invalid Input", "Check price value."); }
            }
            return null;
        });
        dialog.showAndWait();
    }

    public ScrollPane getRoot() { return root; }
}
