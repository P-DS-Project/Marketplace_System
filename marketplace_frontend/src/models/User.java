package models;

public class User {
    private int userId;
    private String username;
    private String email;
    private String role;
    private String avatarUrl;

    public User() {}

    public User(int userId, String username, String email, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public boolean isSeller() {
        return "seller".equalsIgnoreCase(role) || "EXTERNAL_STORE".equalsIgnoreCase(role);
    }

    public boolean isBuyer() {
        return "buyer".equalsIgnoreCase(role) || "USER".equalsIgnoreCase(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
