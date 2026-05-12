package DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Entities.CartItemEntity;
import Utils.DatabaseConnectionManager;

public class CartDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnectionManager.getNode1UsersConnection();
    }

    public boolean addToCart(int userId, int productId, int quantity) {
        String sql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?) " +
                "ON CONFLICT (user_id, product_id) DO UPDATE SET quantity = cart_items.quantity + EXCLUDED.quantity";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding to cart: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<CartItemEntity> getCartItems(int userId) {
        List<CartItemEntity> items = new ArrayList<>();
        String sql = "SELECT cart_item_id, user_id, product_id, quantity, added_at FROM cart_items WHERE user_id = ? ORDER BY added_at DESC";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CartItemEntity item = new CartItemEntity();
                    item.setCartItemId(rs.getInt("cart_item_id"));
                    item.setUserId(rs.getInt("user_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setAddedAt(rs.getString("added_at"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting cart: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    public boolean updateQuantity(int cartItemId, int quantity) {
        if (quantity <= 0)
            return removeItem(cartItemId);
        String sql = "UPDATE cart_items SET quantity = ? WHERE cart_item_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, cartItemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating cart quantity: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeItem(int cartItemId) {
        String sql = "DELETE FROM cart_items WHERE cart_item_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartItemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing cart item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean clearCart(int userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error clearing cart: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public int getCartCount(int userId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) as total FROM cart_items WHERE user_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
