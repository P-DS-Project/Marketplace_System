package daos;

import entities.UserEntity;
import utils.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnectionManager.getNode1UsersConnection();
    }

    public UserEntity findByUsername(String username) {
        String sql = "SELECT user_id, username, email, password_hash, salt, role, is_verified FROM users WHERE username = ?";
        return findUserBySql(sql, username);
    }

    public UserEntity findByEmail(String email) {
        String sql = "SELECT user_id, username, email, password_hash, salt, role, is_verified FROM users WHERE email = ?";
        return findUserBySql(sql, email);
    }

    public UserEntity findById(int userId) {
        String sql = "SELECT user_id, username, email, password_hash, salt, role, is_verified FROM users WHERE user_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching user ID: " + userId);
            e.printStackTrace();
        }
        return null;
    }

    private UserEntity findUserBySql(String sql, String param) {
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, param);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching user: " + param);
            e.printStackTrace();
        }
        return null;
    }

    private UserEntity mapRowToUser(ResultSet rs) throws SQLException {
        UserEntity user = new UserEntity();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setSalt(rs.getString("salt"));
        user.setRole(rs.getString("role"));
        user.setVerified(rs.getBoolean("is_verified"));
        return user;
    }

    // 2. WRITE: Insert a new user (incorporating the unique lookup tables we discussed)
    public int createUser(String username, String email, String passwordHash, String salt, String role) {
        String insertUser = "INSERT INTO users (username, email, password_hash, salt, role) VALUES (?, ?, ?, ?, ?)";
        String insertEmail = "INSERT INTO unique_emails (email, user_id) VALUES (?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Start Transaction

            try {
                int generatedUserId = -1;
                
                // Step A: Insert the actual user data and get the generated user_id
                // Note: The unique constraint on 'users' table will naturally handle username uniqueness
                try (PreparedStatement psMain = conn.prepareStatement(insertUser, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    psMain.setString(1, username);
                    psMain.setString(2, email);
                    psMain.setString(3, passwordHash);
                    psMain.setString(4, salt);
                    psMain.setString(5, role);
                    psMain.executeUpdate();
                    
                    try (ResultSet rs = psMain.getGeneratedKeys()) {
                        if (rs.next()) {
                            // PostgreSQL getGeneratedKeys usually returns all columns or the id depending on the driver
                            generatedUserId = rs.getInt(1); 
                        } else {
                            throw new SQLException("Creating user failed, no ID obtained.");
                        }
                    }
                }

                // Step B: Enforce global email uniqueness by inserting into the lookup table
                try (PreparedStatement psEmail = conn.prepareStatement(insertEmail)) {
                    psEmail.setString(1, email);
                    psEmail.setInt(2, generatedUserId);
                    psEmail.executeUpdate();
                }

                conn.commit(); // Success! Commit inserts.
                return generatedUserId;

            } catch (SQLException e) {
                conn.rollback(); // Conflict! Rollback everything.
                System.err.println("Failed to create user. Email or Username may exist.");
                System.err.println("SQL Error: " + e.getMessage());
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
