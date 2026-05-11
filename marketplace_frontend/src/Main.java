import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import network.SocketClient;
import state.SessionManager;
import utils.ThemeManager;
import views.LoginView;
import views.MainLayout;
import views.NavigationController;

public class Main extends Application {

    private static Stage primaryStage;
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("MarketPlace Pro");
        stage.setWidth(1280);
        stage.setHeight(800);
        stage.setMinWidth(1024);
        stage.setMinHeight(700);

        // Initialize navigation controller
        NavigationController.init(stage, Main::showLogin, Main::showMainApp);

        // Connect to backend
        SocketClient.getInstance().connect(SERVER_HOST, SERVER_PORT);

        showLogin();
        stage.show();
    }

    public static void showLogin() {
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getRoot(), 1280, 800);
        ThemeManager.applyTheme(scene);
        primaryStage.setScene(scene);
    }

    public static void showMainApp() {
        MainLayout mainLayout = new MainLayout();
        Scene scene = new Scene(mainLayout.getRoot(), 1280, 800);
        ThemeManager.applyTheme(scene);
        primaryStage.setScene(scene);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void stop() {
        SocketClient.getInstance().disconnect();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
