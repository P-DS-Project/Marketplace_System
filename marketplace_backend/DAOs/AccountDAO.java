package DAOs;

import Entities.AccountEntity;
import Utils.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnectionManager.getNode1UsersConnection();
    }

    public boolean createAccount(int userId) {
        String sql = "INSERT INTO accounts (user_id, balance, currency) VALUES (?, 0.00, 'EGP')";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public AccountEntity getAccountByUserId(int userId) {
        String sql = "SELECT account_id, user_id, balance, currency FROM accounts WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    AccountEntity acc = new AccountEntity();
                    acc.setAccountId(rs.getInt("account_id"));
                    acc.setUserId(rs.getInt("user_id"));
                    acc.setBalance(rs.getDouble("balance"));
                    acc.setCurrency(rs.getString("currency"));
                    return acc;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
