package DAOs;

import Entities.UserEntity;
import Utils.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // Helper method to get the Node 1 connection
    private Connection getConnection() throws SQLException {
        return DatabaseConnectionManager.getNode1UsersConnection();
    }

    // 1. READ: Fetch a user by their username for logging in
    public UserEntity findByUsername(String username) {
        String sql = "SELECT user_id, username, email, password_hash, salt, role, is_verified FROM users WHERE username = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // In a real app, you would create a UserEntity object to hold this data
                    UserEntity user = new UserEntity();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setSalt(rs.getString("salt"));
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching user: " + username);
            e.printStackTrace();
        }
        return null; // User not found
    }

    // 2. WRITE: Insert a new user (incorporating the unique lookup tables we
    // discussed)
    public boolean createUser(String username, String email, String passwordHash, String salt, String role) {
        String insertEmail = "INSERT INTO unique_emails (email) VALUES (?)";
        String insertUser = "INSERT INTO users (username, email, password_hash, salt, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Start Transaction

            try {
                // Step A: Check uniqueness via lookup tables
                try (PreparedStatement psEmail = conn.prepareStatement(insertEmail)) {
                    psEmail.setString(1, email);
                    psEmail.executeUpdate();
                }

                // Step B: Insert the actual user data
                try (PreparedStatement psMain = conn.prepareStatement(insertUser)) {
                    psMain.setString(1, username);
                    psMain.setString(2, email);
                    psMain.setString(3, passwordHash);
                    psMain.setString(4, salt);
                    psMain.setString(5, role);
                    psMain.executeUpdate();
                }

                conn.commit(); // Success! Commit all three inserts.
                return true;

            } catch (SQLException e) {
                conn.rollback(); // Conflict! Rollback everything.
                System.err.println("Failed to create user. Email may exist.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}