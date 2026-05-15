package views;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.User;
import models.Account;
import state.SessionManager;
import services.UserApiService;
import org.json.JSONObject;
import org.json.JSONArray;

public class ProfileView {

    private final ScrollPane root;

    public ProfileView() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(0));

        Label title = new Label("\uD83D\uDC64 My Profile");
        title.getStyleClass().add("heading");

        User user = SessionManager.getInstance().getCurrentUser();
        Account account = SessionManager.getInstance().getAccount();

        // User info card
        VBox userCard = new VBox(12);
        userCard.getStyleClass().add("card");

        Label cardTitle = new Label("Account Information");
        cardTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(12);

        addInfoRow(infoGrid, 0, "User ID", user != null ? "#" + user.getUserId() : "N/A");
        addInfoRow(infoGrid, 1, "Username", user != null ? user.getUsername() : "N/A");
        addInfoRow(infoGrid, 2, "Email", user != null ? user.getEmail() : "N/A");
        addInfoRow(infoGrid, 3, "Role", user != null ? user.getRole().toUpperCase() : "N/A");

        userCard.getChildren().addAll(cardTitle, infoGrid);

        // Account card
        VBox accountCard = new VBox(12);
        accountCard.getStyleClass().add("card");

        Label accTitle = new Label("\uD83D\uDCB3 Account Balance");
        accTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        HBox balanceRow = new HBox(16);
        balanceRow.setAlignment(Pos.CENTER_LEFT);
        Label balanceVal = new Label(account != null ? String.format("%.2f %s", account.getBalance(), account.getCurrency()) : "N/A");
        balanceVal.getStyleClass().add("stat-value");
        balanceRow.getChildren().add(balanceVal);

        Button refreshBtn = new Button("\uD83D\uDD04 Refresh");
        refreshBtn.getStyleClass().addAll("button", "button-outline");
        refreshBtn.setOnAction(e -> {
            UserApiService userApi = new UserApiService();
            String token = SessionManager.getInstance().getToken();
            JSONObject info = userApi.getInfo(token);
            if (info != null) {
                Account newAcc = userApi.parseAccount(info);
                if (newAcc != null) {
                    SessionManager.getInstance().setAccount(newAcc);
                    balanceVal.setText(String.format("%.2f %s", newAcc.getBalance(), newAcc.getCurrency()));
                }
            }
        });

        accountCard.getChildren().addAll(accTitle, balanceRow, refreshBtn);

        // Products summary
        VBox productsCard = new VBox(12);
        productsCard.getStyleClass().add("card");

        Label prodTitle = new Label("\uD83D\uDCE6 My Products");
        prodTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        VBox prodList = new VBox(8);
        Label loadingProd = new Label("Loading...");
        prodList.getChildren().add(loadingProd);

        productsCard.getChildren().addAll(prodTitle, prodList);

        // Load from API
        new Thread(() -> {
            UserApiService userApi = new UserApiService();
            String token = SessionManager.getInstance().getToken();
            JSONObject info = userApi.getInfo(token);

            javafx.application.Platform.runLater(() -> {
                prodList.getChildren().clear();
                if (info != null && info.has("products")) {
                    JSONArray prods = info.getJSONArray("products");
                    if (prods.length() == 0) {
                        prodList.getChildren().add(new Label("No products listed."));
                    } else {
                        for (int i = 0; i < prods.length(); i++) {
                            JSONObject p = prods.getJSONObject(i);
                            HBox row = new HBox(12);
                            row.setAlignment(Pos.CENTER_LEFT);
                            row.setPadding(new Insets(4, 0, 4, 0));
                            Label name = new Label(p.optString("name", "Unknown"));
                            name.setStyle("-fx-font-weight: bold;");
                            Label price = new Label("$" + String.format("%.2f", p.optDouble("price", 0)));
                            Label status = new Label(p.optString("status", ""));
                            status.getStyleClass().addAll("badge", "IN_STOCK".equals(p.optString("status")) ? "badge-available" : "badge-sold");
                            row.getChildren().addAll(name, price, status);
                            prodList.getChildren().add(row);
                        }
                    }
                } else {
                    prodList.getChildren().add(new Label("Unable to load products."));
                }
            });
        }).start();

        content.getChildren().addAll(title, userCard, accountCard, productsCard);

        root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent;");
    }

    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748B;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 15px;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    public ScrollPane getRoot() { return root; }
}
