package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.CartItem;
import services.CartApiService;
import services.TransactionApiService;
import services.UserApiService;
import state.SessionManager;
import utils.AlertHelper;
import org.json.JSONObject;
import java.util.List;

public class CartView {

    private final ScrollPane root;
    private final MainLayout layout;
    private final CartApiService cartApi = new CartApiService();
    private final VBox cartItemsContainer;
    private final Label totalLabel;
    private final Label subtotalLabel;
    private final Label balanceLabel;

    public CartView(MainLayout layout) {
        this.layout = layout;
        VBox content = new VBox(24);
        content.setPadding(new Insets(0));

        Label title = new Label("\uD83D\uDED2 Shopping Cart");
        title.getStyleClass().add("heading");

        HBox mainRow = new HBox(24);

        // Left: Cart items
        VBox leftCol = new VBox(16);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        cartItemsContainer = new VBox(12);
        leftCol.getChildren().add(cartItemsContainer);

        // Right: Order summary
        VBox rightCol = new VBox(16);
        rightCol.setPrefWidth(340);
        rightCol.setMinWidth(300);

        VBox summaryCard = new VBox(16);
        summaryCard.getStyleClass().add("card");

        Label summaryTitle = new Label("Order Summary");
        summaryTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        subtotalLabel = new Label("Subtotal: $0.00");
        subtotalLabel.setStyle("-fx-font-size: 14px;");

        totalLabel = new Label("Total: $0.00");
        totalLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Separator sep = new Separator();

        // Balance info
        double balance = SessionManager.getInstance().getAccount() != null ? SessionManager.getInstance().getAccount().getBalance() : 0;
        balanceLabel = new Label(String.format("Your Balance: $%.2f", balance));
        balanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #10B981; -fx-font-weight: bold;");

        // Deposit section
        Label depositTitle = new Label("Deposit to Balance");
        depositTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        HBox depositRow = new HBox(8);
        depositRow.setAlignment(Pos.CENTER_LEFT);
        TextField depositField = new TextField();
        depositField.setPromptText("Amount");
        depositField.setPrefWidth(120);
        Button depositBtn = new Button("Deposit");
        depositBtn.getStyleClass().addAll("button", "button-success");
        depositBtn.setStyle("-fx-padding: 8 16; -fx-font-size: 12px;");
        depositBtn.setOnAction(e -> handleDeposit(depositField));
        depositRow.getChildren().addAll(depositField, depositBtn);

        // Place order button
        Button placeOrderBtn = new Button("\uD83D\uDCB3 Place Order");
        placeOrderBtn.setMaxWidth(Double.MAX_VALUE);
        placeOrderBtn.getStyleClass().addAll("button", "button-success");
        placeOrderBtn.setStyle("-fx-font-size: 16px; -fx-padding: 14 24;");
        placeOrderBtn.setOnAction(e -> handlePlaceOrder());

        // Clear cart button
        Button clearBtn = new Button("Clear Cart");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.getStyleClass().addAll("button", "button-secondary");
        clearBtn.setOnAction(e -> handleClearCart());

        summaryCard.getChildren().addAll(summaryTitle, subtotalLabel, sep, totalLabel,
            balanceLabel, new Separator(), depositTitle, depositRow, placeOrderBtn, clearBtn);

        rightCol.getChildren().add(summaryCard);

        mainRow.getChildren().addAll(leftCol, rightCol);
        content.getChildren().addAll(title, mainRow);

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");

        loadCart();
    }

    private void loadCart() {
        int userId = SessionManager.getInstance().getCurrentUser() != null ? SessionManager.getInstance().getCurrentUser().getUserId() : -1;
        if (userId == -1) return;

        new Thread(() -> {
            List<CartItem> items = cartApi.getCart(userId);
            double total = items.stream().mapToDouble(CartItem::getLineTotal).sum();
            int cartCount = items.stream().mapToInt(CartItem::getQuantity).sum();

            javafx.application.Platform.runLater(() -> {
                cartItemsContainer.getChildren().clear();
                SessionManager.getInstance().setCartItemCount(cartCount);

                if (items.isEmpty()) {
                    VBox emptyState = new VBox(16);
                    emptyState.setAlignment(Pos.CENTER);
                    emptyState.setPadding(new Insets(60));
                    Label emptyIcon = new Label("\uD83D\uDED2");
                    emptyIcon.setStyle("-fx-font-size: 64px;");
                    Label emptyText = new Label("Your cart is empty");
                    emptyText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                    Label emptySub = new Label("Browse products and add items to your cart");
                    emptySub.setStyle("-fx-text-fill: #64748B;");
                    Button browseBtn = new Button("Browse Products");
                    browseBtn.setOnAction(e -> layout.navigateTo("browse"));
                    emptyState.getChildren().addAll(emptyIcon, emptyText, emptySub, browseBtn);
                    cartItemsContainer.getChildren().add(emptyState);
                } else {
                    for (CartItem item : items) {
                        cartItemsContainer.getChildren().add(createCartItemRow(item));
                    }
                }

                subtotalLabel.setText(String.format("Subtotal: $%.2f", total));
                totalLabel.setText(String.format("Total: $%.2f", total));
            });
        }).start();
    }

    private HBox createCartItemRow(CartItem item) {
        HBox row = new HBox(16);
        row.getStyleClass().add("card");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16));

        // Image placeholder
        StackPane imgPlaceholder = new StackPane();
        imgPlaceholder.getStyleClass().add("product-image-placeholder");
        imgPlaceholder.setPrefSize(80, 80);
        imgPlaceholder.setMinSize(80, 80);
        imgPlaceholder.setMaxSize(80, 80);
        Label imgIcon = new Label("\uD83D\uDCE6");
        imgIcon.setStyle("-fx-font-size: 28px;");
        imgPlaceholder.getChildren().add(imgIcon);
        imgPlaceholder.setStyle("-fx-background-radius: 8;");

        // Product info
        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(item.getProductName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label price = new Label(String.format("$%.2f each", item.getUnitPrice()));
        price.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px;");
        info.getChildren().addAll(name, price);

        // Quantity controls
        HBox qtyControls = new HBox(8);
        qtyControls.setAlignment(Pos.CENTER);

        Button minusBtn = new Button("-");
        minusBtn.getStyleClass().addAll("button", "button-secondary");
        minusBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 14px; -fx-font-weight: bold;");
        minusBtn.setOnAction(e -> {
            if (item.getQuantity() > 1) {
                cartApi.updateCartQuantity(item.getCartItemId(), item.getQuantity() - 1);
            } else {
                cartApi.removeFromCart(item.getCartItemId());
            }
            loadCart();
        });

        Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
        qtyLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 30; -fx-alignment: center;");

        Button plusBtn = new Button("+");
        plusBtn.getStyleClass().addAll("button", "button-secondary");
        plusBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 14px; -fx-font-weight: bold;");
        plusBtn.setOnAction(e -> {
            cartApi.updateCartQuantity(item.getCartItemId(), item.getQuantity() + 1);
            loadCart();
        });

        qtyControls.getChildren().addAll(minusBtn, qtyLabel, plusBtn);

        // Line total
        Label lineTotal = new Label(String.format("$%.2f", item.getLineTotal()));
        lineTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-min-width: 80; -fx-alignment: center-right;");

        // Remove button
        Button removeBtn = new Button("\u2716");
        removeBtn.getStyleClass().addAll("button", "button-icon");
        removeBtn.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 14px;");
        removeBtn.setOnAction(e -> {
            cartApi.removeFromCart(item.getCartItemId());
            loadCart();
        });

        row.getChildren().addAll(imgPlaceholder, info, qtyControls, lineTotal, removeBtn);
        return row;
    }

    private void handleDeposit(TextField depositField) {
        try {
            double amount = Double.parseDouble(depositField.getText().trim());
            if (amount <= 0) {
                AlertHelper.showError("Invalid Amount", "Deposit amount must be positive.");
                return;
            }
            int userId = SessionManager.getInstance().getCurrentUser().getUserId();
            UserApiService userApi = new UserApiService();
            JSONObject result = userApi.deposit(userId, amount);
            if (result.optBoolean("success")) {
                double newBalance = result.optDouble("newBalance", 0);
                SessionManager.getInstance().getAccount().setBalance(newBalance);
                balanceLabel.setText(String.format("Your Balance: $%.2f", newBalance));
                depositField.clear();
                AlertHelper.showSuccess("Deposit of $" + String.format("%.2f", amount) + " successful!");
            } else {
                AlertHelper.showError("Deposit Failed", result.optString("error", "Unknown error"));
            }
        } catch (NumberFormatException e) {
            AlertHelper.showError("Invalid Input", "Enter a valid amount.");
        }
    }

    private void handlePlaceOrder() {
        int userId = SessionManager.getInstance().getCurrentUser() != null ? SessionManager.getInstance().getCurrentUser().getUserId() : -1;
        if (userId == -1) return;

        List<CartItem> items = cartApi.getCart(userId);
        if (items.isEmpty()) {
            AlertHelper.showError("Empty Cart", "Add items to your cart before placing an order.");
            return;
        }

        double total = items.stream().mapToDouble(CartItem::getLineTotal).sum();
        double balance = SessionManager.getInstance().getAccount() != null ? SessionManager.getInstance().getAccount().getBalance() : 0;

        if (balance < total) {
            AlertHelper.showError("Insufficient Funds",
                String.format("Your balance ($%.2f) is insufficient. Total: $%.2f. Please deposit $%.2f more.",
                    balance, total, total - balance));
            return;
        }

        if (!AlertHelper.showConfirm("Confirm Order",
                String.format("Place order for $%.2f? (%d items)", total, items.size()))) {
            return;
        }

        TransactionApiService txApi = new TransactionApiService();
        boolean allSuccess = true;
        StringBuilder errors = new StringBuilder();

        for (CartItem item : items) {
            JSONObject result = txApi.buy(userId, item.getProductId(), item.getQuantity());
            if (!result.optBoolean("success")) {
                allSuccess = false;
                errors.append(item.getProductName()).append(": ").append(result.optString("error", "Failed")).append("\n");
            }
        }

        if (allSuccess) {
            cartApi.clearCart(userId);
            SessionManager.getInstance().setCartItemCount(0);

            // Refresh account info
            UserApiService userApi = new UserApiService();
            JSONObject info = userApi.getInfo(SessionManager.getInstance().getToken());
            if (info != null) {
                SessionManager.getInstance().setAccount(userApi.parseAccount(info));
            }

            AlertHelper.showSuccess("Order placed successfully! Total: $" + String.format("%.2f", total));
            loadCart();
        } else {
            AlertHelper.showError("Some items failed", errors.toString());
            loadCart();
        }
    }

    private void handleClearCart() {
        int userId = SessionManager.getInstance().getCurrentUser() != null ? SessionManager.getInstance().getCurrentUser().getUserId() : -1;
        if (userId == -1) return;

        if (AlertHelper.showConfirm("Clear Cart", "Remove all items from your cart?")) {
            cartApi.clearCart(userId);
            SessionManager.getInstance().setCartItemCount(0);
            loadCart();
        }
    }

    public ScrollPane getRoot() { return root; }
}
