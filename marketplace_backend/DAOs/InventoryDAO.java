package DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import Utils.DatabaseConnectionManager;
import Entities.InventoryEntity;

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

    public boolean reduceStock(int productId, int quantity, String warehouseNode) {
        String sql = "UPDATE inventory SET quantity = quantity - ? WHERE product_id = ? AND quantity >= ? AND warehouse_node = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            pstmt.setString(4, warehouseNode);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public boolean addStock(int productId, int quantity, String warehouseNode) {
        String sql = "INSERT INTO inventory (product_id, quantity, warehouse_node) VALUES (?, ?, ?) " +
                     "ON CONFLICT (product_id) DO UPDATE SET quantity = inventory.quantity + EXCLUDED.quantity, warehouse_node = EXCLUDED.warehouse_node";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            pstmt.setInt(2, quantity);
            pstmt.setString(3, warehouseNode);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public InventoryEntity getInventoryByProductId(int productId) {
        String sql = "SELECT inventory_id, product_id, quantity, updated_at, warehouse_node FROM inventory WHERE product_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    InventoryEntity inventory = new InventoryEntity();
                    inventory.setInventoryId(rs.getInt("inventory_id"));
                    inventory.setProductId(rs.getInt("product_id"));
                    inventory.setQuantity(rs.getInt("quantity"));
                    inventory.setUpdated_at(rs.getString("updated_at"));
                    inventory.setWarehouse_node(rs.getString("warehouse_node"));
                    return inventory;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
