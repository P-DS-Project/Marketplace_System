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

    public int addProduct(int sellerId, int categoryId, String name, double price, String description, String brand,
            String imageUrl) {
        String sql = "INSERT INTO products (seller_id, category_id, name, price, status, description, brand, image_url) VALUES (?, ?, ?, ?, 'IN_STOCK', ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, sellerId);
            pstmt.setInt(2, categoryId);
            pstmt.setString(3, name);
            pstmt.setDouble(4, price);
            pstmt.setString(5, description);
            pstmt.setString(6, brand);
            pstmt.setString(7, imageUrl);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -2;
        }
    }

    public boolean addProduct(int sellerId, int categoryId, String name, double price, String description) {
        return addProduct(sellerId, categoryId, name, price, description, null, null) > 0;
    }

    public List<ProductEntity> getProductsBySellerId(int sellerId) {
        List<ProductEntity> products = new ArrayList<>();
        String sql = "SELECT product_id, seller_id, category_id, name, brand, price, status, description, image_url, created_at FROM products WHERE seller_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sellerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public ProductEntity getProductById(int productId) {
        String sql = "SELECT product_id, seller_id, category_id, name, brand, price, status, description, image_url, created_at FROM products WHERE product_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ProductEntity> searchByName(String name) {
        List<ProductEntity> products = new ArrayList<>();
        String sql = "SELECT product_id, seller_id, category_id, name, brand, price, status, description, image_url, created_at FROM products WHERE name ILIKE ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public boolean updateProductStatus(int productId, String newStatus) {
        String sql = "UPDATE products SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, productId);
            return pstmt.executeUpdate() > 0;
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
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProduct(int productId, String name, double price, String description) {
        return updateProduct(productId, name, price, description, null, null);
    }

    public boolean updateProduct(int productId, String name, double price, String description, String brand,
            String imageUrl) {
        String sql = "UPDATE products SET name = ?, price = ?, description = ?, brand = ?, image_url = ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setString(3, description);
            pstmt.setString(4, brand);
            pstmt.setString(5, imageUrl);
            pstmt.setInt(6, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ProductEntity> advancedSearch(String keyword, Integer categoryId, Double minPrice, Double maxPrice,
            String brand, Integer sellerId, String startDate, String endDate, String sortBy, String sortOrder,
            int limit, int offset) {
        List<ProductEntity> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT product_id, seller_id, category_id, name, brand, price, status, description, image_url, created_at FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND name ILIKE ?");
            params.add("%" + keyword.trim() + "%");
        }
        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        if (minPrice != null) {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }
        if (brand != null && !brand.trim().isEmpty()) {
            sql.append(" AND brand ILIKE ?");
            params.add("%" + brand.trim() + "%");
        }
        if (sellerId != null) {
            sql.append(" AND seller_id = ?");
            params.add(sellerId);
        }
        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(" AND created_at >= ?::timestamp");
            params.add(startDate);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(" AND created_at <= ?::timestamp");
            params.add(endDate);
        }

        if (sortBy != null && !sortBy.trim().isEmpty()) {
            String safeSortBy = "created_at";
            if (sortBy.equalsIgnoreCase("price"))
                safeSortBy = "price";
            else if (sortBy.equalsIgnoreCase("name"))
                safeSortBy = "name";

            String safeOrder = "DESC";
            if ("ASC".equalsIgnoreCase(sortOrder))
                safeOrder = "ASC";

            sql.append(" ORDER BY ").append(safeSortBy).append(" ").append(safeOrder);
        } else {
            sql.append(" ORDER BY created_at DESC");
        }

        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 20);
        params.add(offset >= 0 ? offset : 0);

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    private ProductEntity mapRow(ResultSet rs) throws SQLException {
        ProductEntity p = new ProductEntity();
        p.setProductId(rs.getInt("product_id"));
        p.setSellerId(rs.getInt("seller_id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setName(rs.getString("name"));
        p.setBrand(rs.getString("brand"));
        p.setPrice(rs.getDouble("price"));
        p.setStatus(rs.getString("status"));
        p.setDescription(rs.getString("description"));
        p.setImageUrl(rs.getString("image_url"));
        try {
            p.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (SQLException ignored) {
        }
        return p;
    }

    // ==================== ADMIN METHODS ====================

    public int countProducts() {
        String sql = "SELECT COUNT(*) FROM products";
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