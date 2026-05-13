package views;

import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import state.SessionManager;
import utils.ThemeManager;
import models.User;

public class MainLayout {

    private final BorderPane root;
    private final StackPane contentArea;
    private final VBox sidebar;
    private Button activeNavButton = null;
    private boolean sidebarCollapsed = false;
    private Label cartBadge;

    public MainLayout() {
        root = new BorderPane();
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");

        sidebar = buildSidebar();
        HBox topBar = buildTopBar();

        HBox footer = new HBox();
        footer.getStyleClass().add("footer");
        footer.setAlignment(Pos.CENTER);
        Label footerLabel = new Label("\u00A9 2026 MarketPlace Pro \u2014 Parallel & Distributed Systems Project");
        footer.getChildren().add(footerLabel);

        root.setTop(topBar);
        root.setLeft(sidebar);
        root.setCenter(contentArea);
        root.setBottom(footer);

        navigateTo("home");
    }

    private HBox buildTopBar() {
        HBox topBar = new HBox(12);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Sidebar toggle
        Button sidebarToggle = new Button("\u2630");
        sidebarToggle.getStyleClass().addAll("button", "sidebar-toggle");
        sidebarToggle.setOnAction(e -> toggleSidebar());

        Label brand = new Label("\uD83C\uDFEA MarketPlace Pro");
        brand.getStyleClass().add("brand-label");

        Region spacerL = new Region();
        HBox.setHgrow(spacerL, Priority.ALWAYS);

        Region spacerR = new Region();
        HBox.setHgrow(spacerR, Priority.ALWAYS);

        // Cart button with badge
        StackPane cartContainer = new StackPane();
        Button cartBtn = new Button("\uD83D\uDED2");
        cartBtn.getStyleClass().addAll("button", "button-icon");
        cartBtn.setStyle("-fx-font-size: 18px;");
        cartBtn.setOnAction(e -> navigateTo("cart"));

        cartBadge = new Label("0");
        cartBadge.getStyleClass().add("cart-badge");
        cartBadge.setVisible(false);
        StackPane.setAlignment(cartBadge, Pos.TOP_RIGHT);

        cartContainer.getChildren().addAll(cartBtn, cartBadge);

        SessionManager.getInstance().cartItemCountProperty().addListener((obs, oldVal, newVal) -> {
            int count = newVal.intValue();
            cartBadge.setText(String.valueOf(count));
            cartBadge.setVisible(count > 0);
        });

        // User menu
        User user = SessionManager.getInstance().getCurrentUser();
        String userName = user != null ? user.getUsername() : "User";
        MenuButton userMenu = new MenuButton("\uD83D\uDC64 " + userName);
        userMenu.getStyleClass().addAll("button", "user-menu-btn");

        MenuItem themeItem = new MenuItem("\uD83C\uDF19 Toggle Theme");
        themeItem.setOnAction(e -> ThemeManager.toggleTheme(root.getScene()));

        MenuItem profileItem = new MenuItem("\uD83D\uDC64 My Account");
        profileItem.setOnAction(e -> navigateTo("myaccount"));

        MenuItem logoutItem = new MenuItem("\uD83D\uDEAA Logout");
        logoutItem.setOnAction(e -> {
            SessionManager.getInstance().logout();
            NavigationController.showLogin();
        });

        userMenu.getItems().addAll(themeItem, profileItem, new SeparatorMenuItem(), logoutItem);

        topBar.getChildren().addAll(sidebarToggle, brand, spacerL, spacerR, cartContainer, userMenu);
        return topBar;
    }

    private VBox buildSidebar() {
        VBox sb = new VBox(2);
        sb.getStyleClass().add("sidebar");

        Button homeBtn = createNavButton("\uD83C\uDFE0  Home", "home");
        Button browseBtn = createNavButton("\uD83D\uDCE6  Browse", "browse");
        Button myProductsBtn = createNavButton("\uD83D\uDED2  My Products", "myproducts");
        Button chatBtn = createNavButton("\uD83D\uDCAC  Messages", "chat");
        Button cartBtn = createNavButton("\uD83D\uDED2  Cart", "cart");
        Button myShopBtn = createNavButton("\uD83D\uDCC8  My Shop", "myshop");
        Button myAccountBtn = createNavButton("\uD83D\uDC64  My Account", "myaccount");

        sb.getChildren().addAll(
            homeBtn, browseBtn, myProductsBtn, chatBtn, cartBtn,
            myShopBtn, myAccountBtn
        );

        return sb;
    }

    private void toggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        double targetWidth = sidebarCollapsed ? 0 : 240;

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(200),
                new KeyValue(sidebar.prefWidthProperty(), targetWidth),
                new KeyValue(sidebar.minWidthProperty(), targetWidth),
                new KeyValue(sidebar.maxWidthProperty(), targetWidth)
            )
        );
        timeline.play();

        if (sidebarCollapsed) {
            sidebar.getStyleClass().add("sidebar-collapsed");
        } else {
            sidebar.getStyleClass().remove("sidebar-collapsed");
        }
    }

    private Button createNavButton(String text, String viewId) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("button", "sidebar-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> {
            navigateTo(viewId);
            setActiveNav(btn);
        });
        if (viewId.equals("home")) {
            setActiveNav(btn);
        }
        return btn;
    }

    private void setActiveNav(Button btn) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("sidebar-item-active");
        }
        btn.getStyleClass().add("sidebar-item-active");
        activeNavButton = btn;
    }

    public void navigateTo(String viewId) {
        Node view;
        switch (viewId) {
            case "home": view = new HomeView(this).getRoot(); break;
            case "browse": view = new BrowseView(this).getRoot(); break;
            case "myproducts": view = new SellerDashboardView(this).getRoot(); break;
            case "chat": view = new ChatView(-1).getRoot(); break;
            case "cart": view = new CartView(this).getRoot(); break;
            case "myshop": view = new MyShopView(this).getRoot(); break;
            case "myaccount": view = new MyAccountView(this).getRoot(); break;
            case "addproduct": view = new AddProductView(this).getRoot(); break;
            case "editproduct": view = new HomeView(this).getRoot(); break; // placeholder, editproduct needs product ID
            default: view = new HomeView(this).getRoot(); break;
        }
        setContent(view);
    }

    public void navigateToEditProduct(int productId) {
        setContent(new EditProductView(productId, this).getRoot());
    }

    public void navigateToChat(int sellerId) {
        setContent(new ChatView(sellerId).getRoot());
    }

    public void setContent(Node node) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(node);
    }

    public void showProductDetail(int productId) {
        setContent(new ProductDetailView(productId, this).getRoot());
    }

    public BorderPane getRoot() {
        return root;
    }
}
