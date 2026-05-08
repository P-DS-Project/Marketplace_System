package DAOs;
<<<<<<< HEAD

import Utils.DatabaseConnectionManager;
=======
>>>>>>> 913f260 (Product Services Added)
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
<<<<<<< HEAD

public class InventoryDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnectionManager.getNode2ProductsConnection();
    }

    public boolean checkStock(int productId, int requiredQuantity) {
        String sql = "SELECT quantity FROM inventory WHERE product_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity") >= requiredQuantity;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean reduceStock(int productId, int quantity) {
        String sql = "UPDATE inventory SET quantity = quantity - ? WHERE product_id = ? AND quantity >= ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
=======
import java.util.ArrayList;
import java.util.List;
import Entities.InventoryEntity;
import Utils.DatabaseConnectionManager;
public class InventoryDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnectionManager.getNode3TransactionsConnection();
    }
    public List<InventoryEntity> getInventoryByProductId(int productId) {
        List<InventoryEntity> inventoryList = new ArrayList<>();
        String sql = "SELECT inventory_id, product_id, quantity, updated_at, warehouse_node FROM inventory WHERE product_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    InventoryEntity inv = new InventoryEntity();
                    inv.setInventoryId(rs.getInt("inventory_id"));
                    inv.setProductId(rs.getInt("product_id"));
                    inv.setQuantity(rs.getInt("quantity"));
                    inv.setUpdated_at(rs.getString("updated_at"));
                    inv.setWarehouse_node(rs.getString("warehouse_node"));
                    inventoryList.add(inv);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
}
    
>>>>>>> 913f260 (Product Services Added)
