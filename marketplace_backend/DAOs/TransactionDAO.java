package DAOs;

import Entities.TransactionEntity;
import Utils.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnectionManager.getNode3TransactionsConnection();
    }

    public List<TransactionEntity> getTransactionsByUserId(int userId) {
        List<TransactionEntity> transactions = new ArrayList<>();
        String sql = "SELECT transaction_id, buyer_id, seller_id, product_id, quantity, amount, status, type FROM transactions WHERE buyer_id = ? OR seller_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TransactionEntity t = new TransactionEntity();
                    t.setTransactionId(rs.getInt("transaction_id"));
                    t.setBuyerId(rs.getInt("buyer_id"));
                    t.setSellerId(rs.getInt("seller_id"));
                    t.setProductId(rs.getInt("product_id"));
                    t.setQuantity(rs.getInt("quantity"));
                    t.setAmount(rs.getDouble("amount"));
                    t.setStatus(rs.getString("status"));
                    t.setType(rs.getString("type"));
                    t.setTimestamp(rs.getTimestamp("created_at"));
                    t.setTimestamp(rs.getTimestamp("completed_at"));
                    transactions.add(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
}
