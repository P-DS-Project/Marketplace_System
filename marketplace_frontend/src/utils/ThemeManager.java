package utils;

import javafx.scene.Scene;
import state.SessionManager;

public class ThemeManager {

    private static final String ENTERPRISE_PRO = "/styles/enterprise-pro.css";
    private static final String MIDNIGHT_MARKET = "/styles/midnight-market.css";

    public static void applyTheme(Scene scene) {
        String theme = SessionManager.getInstance().getTheme();
        scene.getStylesheets().clear();
        if ("midnight-market".equals(theme)) {
            String css = ThemeManager.class.getResource(MIDNIGHT_MARKET).toExternalForm();
            scene.getStylesheets().add(css);
        } else {
            String css = ThemeManager.class.getResource(ENTERPRISE_PRO).toExternalForm();
            scene.getStylesheets().add(css);
        }
    }

    public static void toggleTheme(Scene scene) {
        SessionManager sm = SessionManager.getInstance();
        if ("enterprise-pro".equals(sm.getTheme())) {
            sm.setTheme("midnight-market");
        } else {
            sm.setTheme("enterprise-pro");
        }
        applyTheme(scene);
    }

    public static String getThemeName() {
        String theme = SessionManager.getInstance().getTheme();
        return "midnight-market".equals(theme) ? "Midnight Market" : "Enterprise Pro";
    }

    public static boolean isDarkMode() {
        return "midnight-market".equals(SessionManager.getInstance().getTheme());
    }
}
