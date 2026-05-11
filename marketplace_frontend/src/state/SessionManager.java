package state;

import models.User;
import models.Account;
import javafx.beans.property.*;

public class SessionManager {

    private static SessionManager instance;

    private final ObjectProperty<User> currentUser = new SimpleObjectProperty<>(null);
    private final StringProperty token = new SimpleStringProperty(null);
    private final ObjectProperty<Account> account = new SimpleObjectProperty<>(null);
    private final StringProperty theme = new SimpleStringProperty("enterprise-pro");

    private SessionManager() {}

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
    public void setTheme(String t) { theme.set(t); }
    public StringProperty themeProperty() { return theme; }

    public boolean isLoggedIn() {
        return currentUser.get() != null && token.get() != null;
    }

    public void logout() {
        currentUser.set(null);
        token.set(null);
        account.set(null);
    }
}
