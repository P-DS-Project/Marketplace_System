package views;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Navigation controller used by views to switch scenes.
 * Set by Main.java at startup.
 */
public class NavigationController {

    private static Runnable showLoginAction;
    private static Runnable showMainAppAction;
    private static Stage primaryStage;

    public static void init(Stage stage, Runnable loginAction, Runnable mainAppAction) {
        primaryStage = stage;
        showLoginAction = loginAction;
        showMainAppAction = mainAppAction;
    }

    public static void showLogin() {
        if (showLoginAction != null) showLoginAction.run();
    }

    public static void showMainApp() {
        if (showMainAppAction != null) showMainAppAction.run();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
