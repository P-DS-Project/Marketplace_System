package views;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import state.SessionManager;
import utils.ThemeManager;
import models.User;

public class MainLayout {

    private final BorderPane root;
    private final StackPane contentArea;
    private final VBox sidebar;
    private Button activeNavButton = null;

    public MainLayout() {
        root = new BorderPane();
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");

        sidebar = buildSidebar();
        HBox topBar = buildTopBar();

        // Footer
        HBox footer = new HBox();
        footer.getStyleClass().add("footer");
        footer.setAlignment(Pos.CENTER);
        Label footerLabel = new Label("\u00A9 2026 MarketPlace Pro — Parallel & Distributed Systems Project");
        footer.getChildren().add(footerLabel);

        root.setTop(topBar);
        root.setLeft(sidebar);
        root.setCenter(contentArea);
        root.setBottom(footer);

        // Default view
        navigateTo("home");
    }

    private HBox buildTopBar() {
        HBox topBar = new HBox(16);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label brand = new Label("\uD83C\uDFEA MarketPlace Pro");
        brand.getStyleClass().add("brand-label");

        Region spacerL = new Region();
        HBox.setHgrow(spacerL, Priority.ALWAYS);

        // Search field
        TextField searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("\uD83D\uDD0D Search products...");
        searchField.setOnAction(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                setContent(new SearchView(query).getRoot());
            }
        });

        Region spacerR = new Region();
        HBox.setHgrow(spacerR, Priority.ALWAYS);

        // Theme toggle
        Button themeBtn = new Button("\uD83C\uDF19 Theme");
        themeBtn.getStyleClass().addAll("button", "theme-toggle");
        themeBtn.setOnAction(e -> ThemeManager.toggleTheme(root.getScene()));

        // User menu
        User user = SessionManager.getInstance().getCurrentUser();
        String userName = user != null ? user.getUsername() : "User";
        MenuButton userMenu = new MenuButton("\uD83D\uDC64 " + userName);
        userMenu.getStyleClass().addAll("button", "user-menu-btn");

        MenuItem profileItem = new MenuItem("\uD83D\uDC64 Profile");
        profileItem.setOnAction(e -> navigateTo("profile"));

        MenuItem logoutItem = new MenuItem("\uD83D\uDEAA Logout");
        logoutItem.setOnAction(e -> {
            SessionManager.getInstance().logout();
            NavigationController.showLogin();
        });

        userMenu.getItems().addAll(profileItem, new SeparatorMenuItem(), logoutItem);

        topBar.getChildren().addAll(brand, spacerL, searchField, spacerR, themeBtn, userMenu);
        return topBar;
    }

    private VBox buildSidebar() {
        VBox sb = new VBox(2);
        sb.getStyleClass().add("sidebar");

        // Navigation section
        Label navSection = new Label("NAVIGATION");
        navSection.getStyleClass().add("sidebar-section");

        Button homeBtn = createNavButton("\uD83C\uDFE0  Home", "home");
        Button browseBtn = createNavButton("\uD83D\uDCE6  Browse", "browse");
        Button searchBtn = createNavButton("\uD83D\uDD0D  Search", "search");

        Label manageSection = new Label("MANAGE");
        manageSection.getStyleClass().add("sidebar-section");

        Button myProductsBtn = createNavButton("\uD83D\uDED2  My Products", "seller");
        Button chatBtn = createNavButton("\uD83D\uDCAC  Messages", "chat");
        Button txBtn = createNavButton("\uD83D\uDCB0  Transactions", "transactions");
        Button invBtn = createNavButton("\uD83D\uDCCA  Inventory", "inventory");

        Label analyticsSection = new Label("ANALYTICS");
        analyticsSection.getStyleClass().add("sidebar-section");

        Button reportsBtn = createNavButton("\uD83D\uDCC8  Reports", "reports");
        Button profileBtn = createNavButton("\uD83D\uDC64  Profile", "profile");

        sb.getChildren().addAll(
            navSection, homeBtn, browseBtn, searchBtn,
            new Separator(),
            manageSection, myProductsBtn, chatBtn, txBtn, invBtn,
            new Separator(),
            analyticsSection, reportsBtn, profileBtn
        );

        return sb;
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
            case "search": view = new SearchView(null).getRoot(); break;
            case "seller": view = new SellerDashboardView(this).getRoot(); break;
            case "chat": view = new ChatView().getRoot(); break;
            case "transactions": view = new TransactionView().getRoot(); break;
            case "inventory": view = new InventoryView().getRoot(); break;
            case "reports": view = new ReportsView().getRoot(); break;
            case "profile": view = new ProfileView().getRoot(); break;
            default: view = new HomeView(this).getRoot(); break;
        }
        setContent(view);
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
