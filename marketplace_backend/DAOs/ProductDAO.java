package DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Entities.ProductEntity;
import Utils.DatabaseConnectionManager;

public class ProductDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnectionManager.getNode2ProductsConnection();
    }
    public boolean addProduct(int sellerId, int categoryId, String name, double price, String description) {
        String sql = "INSERT INTO products (seller_id, category_id, name, price, status, description) VALUES (?, ?, ?, ?, 'AVAILABLE', ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sellerId);
            pstmt.setInt(2, categoryId);
            pstmt.setString(3, name);
            pstmt.setDouble(4, price);
            pstmt.setString(5, description);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<ProductEntity> getProductsBySellerId(int sellerId) {
        List<ProductEntity> products = new ArrayList<>();
        String sql = "SELECT product_id, seller_id, category_id, name, price, status FROM products WHERE seller_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sellerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ProductEntity p = new ProductEntity();
                    p.setProductId(rs.getInt("product_id"));
                    p.setSellerId(rs.getInt("seller_id"));
                    p.setCategoryId(rs.getInt("category_id"));
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getDouble("price"));
                    p.setStatus(rs.getString("status"));
                    p.setDescription(rs.getString("description"));
                    products.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    public ProductEntity getProductById(int productId) {
        ProductEntity product = null;
        String sql = "SELECT product_id, seller_id, category_id, name, price, status FROM products WHERE product_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    product = new ProductEntity();
                    product.setProductId(rs.getInt("product_id"));
                    product.setSellerId(rs.getInt("seller_id"));
                    product.setCategoryId(rs.getInt("category_id"));
                    product.setName(rs.getString("name"));
                    product.setPrice(rs.getDouble("price"));
                    product.setStatus(rs.getString("status"));
                    product.setDescription(rs.getString("description"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }
    
    public List<ProductEntity> searchByName(String name) {
        List<ProductEntity> products = new ArrayList<>();
        String sql = "SELECT product_id, seller_id, category_id, name, price, status, description FROM products WHERE name ILIKE ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ProductEntity p = new ProductEntity();
                    p.setProductId(rs.getInt("product_id"));
                    p.setSellerId(rs.getInt("seller_id"));
                    p.setCategoryId(rs.getInt("category_id"));
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getDouble("price"));
                    p.setStatus(rs.getString("status"));
                    p.setDescription(rs.getString("description"));
                    products.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
}
        return products;
    }
    public boolean updateProductStatus(int productId, String newStatus) {
        String sql = "UPDATE products SET status = ? WHERE product_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, productId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateProduct(int productId, String name, double price, String description) {
        String sql = "UPDATE products SET name = ?, price = ?, description = ? WHERE product_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setString(3, description);
            pstmt.setInt(4, productId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}