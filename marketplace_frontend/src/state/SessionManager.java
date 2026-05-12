package state;

import models.User;
import models.Account;
import javafx.beans.property.*;
import java.util.prefs.Preferences;

public class SessionManager {

    private static SessionManager instance;

    private static final long SESSION_DURATION_MS = 3 * 60 * 60 * 1000; // 3 hours
    private static final String PREF_TOKEN = "mp_token";
    private static final String PREF_TIMESTAMP = "mp_login_ts";
    private static final String PREF_THEME = "mp_theme";

    private final ObjectProperty<User> currentUser = new SimpleObjectProperty<>(null);
    private final StringProperty token = new SimpleStringProperty(null);
    private final ObjectProperty<Account> account = new SimpleObjectProperty<>(null);
    private final StringProperty theme = new SimpleStringProperty("enterprise-pro");
    private final IntegerProperty cartItemCount = new SimpleIntegerProperty(0);
    private long loginTimestamp = 0;

    private SessionManager() {
        String savedTheme = getPrefs().get(PREF_THEME, "enterprise-pro");
        theme.set(savedTheme);
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // User
    public User getCurrentUser() { return currentUser.get(); }
    public void setCurrentUser(User user) { currentUser.set(user); }
    public ObjectProperty<User> currentUserProperty() { return currentUser; }

    // Token
    public String getToken() { return token.get(); }
    public void setToken(String t) { token.set(t); }
    public StringProperty tokenProperty() { return token; }

    // Account
    public Account getAccount() { return account.get(); }
    public void setAccount(Account acc) { account.set(acc); }
    public ObjectProperty<Account> accountProperty() { return account; }

    // Theme
    public String getTheme() { return theme.get(); }
    public void setTheme(String t) {
        theme.set(t);
        getPrefs().put(PREF_THEME, t);
    }
    public StringProperty themeProperty() { return theme; }

    // Cart
    public int getCartItemCount() { return cartItemCount.get(); }
    public void setCartItemCount(int count) { cartItemCount.set(count); }
    public IntegerProperty cartItemCountProperty() { return cartItemCount; }

    // Session persistence
    public void saveSession() {
        Preferences prefs = getPrefs();
        if (token.get() != null) {
            prefs.put(PREF_TOKEN, token.get());
            prefs.putLong(PREF_TIMESTAMP, loginTimestamp);
        }
    }

    public String loadSavedToken() {
        Preferences prefs = getPrefs();
        String savedToken = prefs.get(PREF_TOKEN, null);
        long savedTs = prefs.getLong(PREF_TIMESTAMP, 0);

        if (savedToken != null && savedTs > 0) {
            long elapsed = System.currentTimeMillis() - savedTs;
            if (elapsed < SESSION_DURATION_MS) {
                this.loginTimestamp = savedTs;
                return savedToken;
            } else {
                clearSavedSession();
            }
        }
        return null;
    }

    public void clearSavedSession() {
        Preferences prefs = getPrefs();
        prefs.remove(PREF_TOKEN);
        prefs.remove(PREF_TIMESTAMP);
    }

    public boolean isSessionExpired() {
        if (loginTimestamp <= 0) return true;
        return (System.currentTimeMillis() - loginTimestamp) >= SESSION_DURATION_MS;
    }

    public void markLoginTime() {
        this.loginTimestamp = System.currentTimeMillis();
    }

    public boolean isLoggedIn() {
        return currentUser.get() != null && token.get() != null;
    }

    public void logout() {
        currentUser.set(null);
        token.set(null);
        account.set(null);
        cartItemCount.set(0);
        loginTimestamp = 0;
        clearSavedSession();
    }

    private Preferences getPrefs() {
        return Preferences.userNodeForPackage(SessionManager.class);
    }
}
