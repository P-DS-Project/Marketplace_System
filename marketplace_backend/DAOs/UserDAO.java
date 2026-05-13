package DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Entities.UserEntity;
import Utils.DatabaseConnectionManager;

public class UserDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnectionManager.getNode1UsersConnection();
    }

    public UserEntity findByUsername(String username) {
        String sql = "SELECT user_id, username, email, password_hash, salt, role, is_verified, is_active, avatar_url, created_at FROM users WHERE username = ?";
        return findUserBySql(sql, username);
    }

    public UserEntity findByEmail(String email) {
        String sql = "SELECT user_id, username, email, password_hash, salt, role, is_verified, is_active, avatar_url, created_at FROM users WHERE email = ?";
        return findUserBySql(sql, email);
    }

    public UserEntity findById(int userId) {
        String sql = "SELECT user_id, username, email, password_hash, salt, role, is_verified, is_active, avatar_url, created_at FROM users WHERE user_id = ?";
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
        user.setActive(rs.getBoolean("is_active"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        try {
            java.sql.Timestamp ts = rs.getTimestamp("created_at");
            user.setCreatedAt(ts != null ? ts.toString() : "");
        } catch (SQLException ignored) {
            user.setCreatedAt("");
        }
        return user;
    }

    public int createUser(String username, String email, String passwordHash, String salt, String role) {
        String insertUser = "INSERT INTO users (username, email, password_hash, salt, role) VALUES (?, ?, ?, ?, ?)";
        String insertEmail = "INSERT INTO unique_emails (email, user_id) VALUES (?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                int generatedUserId = -1;

                try (PreparedStatement psMain = conn.prepareStatement(insertUser,
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
                    psMain.setString(1, username);
                    psMain.setString(2, email);
                    psMain.setString(3, passwordHash);
                    psMain.setString(4, salt);
                    psMain.setString(5, role);
                    psMain.executeUpdate();

                    try (ResultSet rs = psMain.getGeneratedKeys()) {
                        if (rs.next()) {
                            generatedUserId = rs.getInt(1);
                        } else {
                            throw new SQLException("Creating user failed, no ID obtained.");
                        }
                    }
                }

                try (PreparedStatement psEmail = conn.prepareStatement(insertEmail)) {
                    psEmail.setString(1, email);
                    psEmail.setInt(2, generatedUserId);
                    psEmail.executeUpdate();
                }

                conn.commit();
                return generatedUserId;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Failed to create user. Email or Username may exist.");
                System.err.println("SQL Error: " + e.getMessage());
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean updateProfile(int userId, String username, String email, String avatarUrl) {
        String sql = "UPDATE users SET username = ?, email = ?, avatar_url = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, avatarUrl);
            pstmt.setInt(4, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(int userId, String newPasswordHash, String newSalt) {
        String sql = "UPDATE users SET password_hash = ?, salt = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPasswordHash);
            pstmt.setString(2, newSalt);
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(int userId) {
        String deleteEmails = "DELETE FROM unique_emails WHERE user_id = ?";
        String deleteCartItems = "DELETE FROM cart_items WHERE user_id = ?";
        String deleteUser = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps0 = conn.prepareStatement(deleteCartItems)) {
                    ps0.setInt(1, userId);
                    ps0.executeUpdate();
                }
                try (PreparedStatement ps1 = conn.prepareStatement(deleteEmails)) {
                    ps1.setInt(1, userId);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement(deleteUser)) {
                    ps2.setInt(1, userId);
                    ps2.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getUsernameById(int userId) {
        String sql = "SELECT username FROM users WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "User #" + userId;
    }

    // ==================== ADMIN METHODS ====================

    public List<UserEntity> getAllUsers() {
        List<UserEntity> users = new ArrayList<>();
        String sql = "SELECT user_id, username, email, password_hash, salt, role, is_verified, is_active, avatar_url, created_at FROM users ORDER BY user_id";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<UserEntity> searchUsers(String query) {
        List<UserEntity> users = new ArrayList<>();
        String sql = "SELECT user_id, username, email, password_hash, salt, role, is_verified, is_active, avatar_url, created_at FROM users WHERE username ILIKE ? OR email ILIKE ? OR CAST(user_id AS TEXT) = ? ORDER BY user_id";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String likeParam = "%" + query + "%";
            pstmt.setString(1, likeParam);
            pstmt.setString(2, likeParam);
            pstmt.setString(3, query.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean updateRole(int userId, String newRole) {
        String sql = "UPDATE users SET role = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setActive(int userId, boolean active) {
        String sql = "UPDATE users SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, active);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countAdmins() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
