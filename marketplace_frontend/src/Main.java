import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import network.SocketClient;
import state.SessionManager;
import utils.ThemeManager;
import views.LoginView;
import views.MainLayout;
import views.NavigationController;
import services.UserApiService;
import models.User;
import models.Account;
import org.json.JSONObject;

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

        NavigationController.init(stage, Main::showLogin, Main::showMainApp);

        SocketClient.getInstance().connect(SERVER_HOST, SERVER_PORT);

        if (!tryRestoreSession()) {
            showLogin();
        }

        stage.show();
    }

    private boolean tryRestoreSession() {
        SessionManager sm = SessionManager.getInstance();
        String savedToken = sm.loadSavedToken();

        if (savedToken == null) return false;

        try {
            UserApiService userApi = new UserApiService();
            JSONObject info = userApi.getInfo(savedToken);
            if (info == null) {
                sm.clearSavedSession();
                return false;
            }

            User user = userApi.parseUser(info);
            Account account = userApi.parseAccount(info);

            sm.setToken(savedToken);
            sm.setCurrentUser(user);
            sm.setAccount(account);

            showMainApp();
            return true;
        } catch (Exception e) {
            sm.clearSavedSession();
            return false;
        }
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
