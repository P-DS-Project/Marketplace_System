package views;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
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
    private boolean sidebarVisible = false;
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
        // Sidebar is HIDDEN by default — added/removed from layout on toggle
        root.setCenter(contentArea);
        root.setBottom(footer);

        // Cart badge listener
        SessionManager.getInstance().cartItemCountProperty().addListener((obs, oldVal, newVal) -> {
            int count = newVal.intValue();
            cartBadge.setText(String.valueOf(count));
            cartBadge.setVisible(count > 0);
        });

        navigateTo("home");
    }

    private HBox buildTopBar() {
        HBox topBar = new HBox(14);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Hamburger menu button
        Button menuBtn = new Button("\u2630");
        menuBtn.getStyleClass().addAll("button", "button-icon");
        menuBtn.setStyle("-fx-font-size: 22px; -fx-padding: 6 12;");
        menuBtn.setOnAction(e -> toggleSidebar());

        // Brand
        Label brand = new Label("\uD83C\uDFEA MarketPlace Pro");
        brand.getStyleClass().add("brand-label");

        Region spacerL = new Region();
        HBox.setHgrow(spacerL, Priority.ALWAYS);

        // Search field + button (pill style matching example)
        TextField searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("\uD83D\uDD0D  Search products, brands, categories...");
        searchField.setStyle("-fx-pref-width: 400; -fx-min-width: 280; " +
                "-fx-border-radius: 24 0 0 24; -fx-background-radius: 24 0 0 24;");

        Button searchBtn = new Button("\uD83D\uDD0D");
        searchBtn.getStyleClass().add("button");
        searchBtn.setStyle("-fx-padding: 10 16; -fx-background-radius: 0 24 24 0; -fx-font-size: 14px;");
        searchBtn.setOnAction(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                setContent(new SearchView(query, this).getRoot());
            }
        });
        searchField.setOnAction(e -> searchBtn.fire());

        HBox searchBox = new HBox(0, searchField, searchBtn);
        searchBox.setAlignment(Pos.CENTER);

        Region spacerR = new Region();
        HBox.setHgrow(spacerR, Priority.ALWAYS);

        // Admin badge
        User user = SessionManager.getInstance().getCurrentUser();
        Label adminBadge = new Label("\uD83D\uDEE1\uFE0F Admin");
        adminBadge.setStyle("-fx-background-color: #EDE9FE; -fx-text-fill: #6D28D9; -fx-padding: 4 12; " +
                "-fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");
        adminBadge.setVisible(user != null && user.isAdmin());
        adminBadge.setManaged(user != null && user.isAdmin());


        // Dark mode toggle button
        boolean isDark = ThemeManager.isDarkMode();
        Button darkModeBtn = new Button(isDark ? "☀️" : "🌙");
        darkModeBtn.getStyleClass().addAll("button", "dark-mode-btn");
        darkModeBtn.setTooltip(new Tooltip(isDark ? "Switch to Light Mode" : "Switch to Dark Mode"));
        darkModeBtn.setOnAction(e -> {
            ThemeManager.toggleTheme(root.getScene());
            boolean nowDark = ThemeManager.isDarkMode();
            darkModeBtn.setText(nowDark ? "☀️" : "🌙");
            darkModeBtn.getTooltip().setText(nowDark ? "Switch to Light Mode" : "Switch to Dark Mode");
        });

        // Cart button with badge
        StackPane cartContainer = new StackPane();
        Button cartBtn = new Button("\uD83D\uDED2");
        cartBtn.getStyleClass().addAll("button", "button-icon");
        cartBtn.setStyle("-fx-font-size: 20px; -fx-padding: 8 14;");
        cartBtn.setOnAction(e -> navigateTo("cart"));

        cartBadge = new Label("0");
        cartBadge.getStyleClass().add("cart-badge");
        cartBadge.setVisible(false);
        StackPane.setAlignment(cartBadge, Pos.TOP_RIGHT);
        cartContainer.getChildren().addAll(cartBtn, cartBadge);

        // User menu — show initials + username (like example)
        String userName = user != null ? user.getUsername() : "User";
        String initials = userName.length() >= 2 ? userName.substring(0, 2).toUpperCase() : userName.toUpperCase();
        MenuButton userMenu = new MenuButton(initials + "  " + userName);
        userMenu.getStyleClass().addAll("button", "user-menu-btn");


        CheckMenuItem themeItem = new CheckMenuItem("🌙 Dark Mode");
        themeItem.setSelected(ThemeManager.isDarkMode());
        themeItem.setOnAction(e -> {
            ThemeManager.toggleTheme(root.getScene());
            boolean nowDark = ThemeManager.isDarkMode();
            themeItem.setSelected(nowDark);
            darkModeBtn.setText(nowDark ? "☀️" : "🌙");
            darkModeBtn.getTooltip().setText(nowDark ? "Switch to Light Mode" : "Switch to Dark Mode");
        });

        MenuItem profileItem = new MenuItem("👤 My Account");
        profileItem.setOnAction(e -> navigateTo("myaccount"));

        MenuItem logoutItem = new MenuItem("\uD83D\uDEAA Logout");
        logoutItem.setOnAction(e -> {
            SessionManager.getInstance().logout();
            NavigationController.showLogin();
        });

        userMenu.getItems().addAll(themeItem, profileItem, new SeparatorMenuItem(), logoutItem);

        topBar.getChildren().addAll(menuBtn, brand, spacerL, searchBox, spacerR, adminBadge, darkModeBtn, cartContainer, userMenu);
        return topBar;
    }

    private void toggleSidebar() {
        if (sidebarVisible) {
            // Slide out and remove
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(220), sidebar);
            slideOut.setToX(-260);
            slideOut.setOnFinished(e -> {
                root.setLeft(null);
                sidebar.setTranslateX(0);
            });
            slideOut.play();
            sidebarVisible = false;
        } else {
            // Add and slide in
            sidebar.setTranslateX(-260);
            root.setLeft(sidebar);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(220), sidebar);
            slideIn.setFromX(-260);
            slideIn.setToX(0);
            slideIn.play();
            sidebarVisible = true;
        }
    }

    private VBox buildSidebar() {
        VBox sbContent = new VBox(2);
        sbContent.getStyleClass().add("sidebar");

        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(sbContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        // Close button at top
        HBox closeRow = new HBox();
        closeRow.setAlignment(Pos.CENTER_RIGHT);
        closeRow.setPadding(new Insets(8, 16, 0, 0));
        Button closeBtn = new Button("\u2716");
        closeBtn.getStyleClass().addAll("button", "button-icon");
        closeBtn.setStyle("-fx-font-size: 16px; -fx-text-fill: #94A3B8;");
        closeBtn.setOnAction(e -> toggleSidebar());
        closeRow.getChildren().add(closeBtn);

        // Sidebar logo
        Label sidebarBrand = new Label("\uD83D\uDE80");
        sidebarBrand.setStyle("-fx-font-size: 32px; -fx-padding: 4 0 16 28;");

        // Navigation section
        Label navSection = new Label("NAVIGATION");
        navSection.getStyleClass().add("sidebar-section");

        Button homeBtn = createNavButton("\uD83C\uDFE0  Home", "home");
        Button browseBtn = createNavButton("\uD83D\uDCE6  Browse", "browse");
        Button myProductsBtn = createNavButton("\uD83C\uDFF7\uFE0F  My Products", "myproducts");
        Button cartNavBtn = createNavButton("\uD83D\uDED2  Cart", "cart");

        Label manageSection = new Label("MANAGE");
        manageSection.getStyleClass().add("sidebar-section");

        Button chatBtn = createNavButton("\uD83D\uDCAC  Messages", "chat");
        Button myShopBtn = createNavButton("\uD83D\uDCC8  My Shop", "myshop");
        Button myAccountBtn = createNavButton("\uD83D\uDC64  My Account", "myaccount");

        sbContent.getChildren().addAll(
            closeRow, sidebarBrand,
            navSection, homeBtn, browseBtn, myProductsBtn, cartNavBtn,
            new Separator(),
            manageSection, chatBtn, myShopBtn, myAccountBtn
        );

        // Admin section — only visible if the current user is an admin
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null && user.isAdmin()) {
            Label adminSection = new Label("\u2699\uFE0F  ADMIN");
            adminSection.getStyleClass().add("sidebar-section");

            Button adminDashBtn = createNavButton("\uD83D\uDCCA  Dashboard", "admin-dashboard");
            Button adminUsersBtn = createNavButton("\uD83D\uDC65  Users", "admin-users");
            Button adminProductsBtn = createNavButton("\uD83D\uDCE6  Products", "admin-products");
            Button adminTxBtn = createNavButton("\uD83D\uDCB3  Transactions", "admin-transactions");

            sbContent.getChildren().addAll(
                new Separator(), adminSection,
                adminDashBtn, adminUsersBtn, adminProductsBtn, adminTxBtn
            );
        }

        // Sidebar wrapper
        VBox sidebarWrapper = new VBox(scrollPane);
        sidebarWrapper.getStyleClass().add("sidebar");
        sidebarWrapper.setPrefWidth(260);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return sidebarWrapper;
    }

    private Button createNavButton(String text, String viewId) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("button", "sidebar-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> {
            navigateTo(viewId);
            setActiveNav(btn);
            // Auto-close sidebar on navigation (like the example)
            if (sidebarVisible) toggleSidebar();
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
            case "admin-dashboard": view = new AdminDashboardView(this).getRoot(); break;
            case "admin-users": view = new AdminUsersView(this).getRoot(); break;
            case "admin-products": view = new AdminProductsView(this).getRoot(); break;
            case "admin-transactions": view = new AdminTransactionsView(this).getRoot(); break;
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

    /**
     * Animate content transitions with a smooth fade-out then fade+slide-up.
     */
    public void setContent(Node node) {
        if (!contentArea.getChildren().isEmpty()) {
            Node old = contentArea.getChildren().get(0);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(120), old);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                contentArea.getChildren().clear();
                addWithAnimation(node);
            });
            fadeOut.play();
        } else {
            addWithAnimation(node);
        }
    }

    private void addWithAnimation(Node node) {
        node.setOpacity(0.0);
        node.setTranslateY(12);
        contentArea.getChildren().add(node);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), node);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(250), node);
        slideUp.setFromY(12);
        slideUp.setToY(0);

        new ParallelTransition(fadeIn, slideUp).play();
    }

    public void showProductDetail(int productId) {
        setContent(new ProductDetailView(productId, this).getRoot());
    }

    public BorderPane getRoot() {
        return root;
    }
}
