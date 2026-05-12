package Microservices;

import DAOs.ProductDAO;
import Entities.ProductEntity;
import org.json.JSONArray;
import org.json.JSONObject;

public class ProductService {
    private final ProductDAO productDao;

    public ProductService() {
        this.productDao = new ProductDAO();
    }

    public String addProduct(int sellerId, int categoryId, String name, double price, String description) {
        return addProduct(sellerId, categoryId, name, price, description, null, null);
    }

    public String addProduct(int sellerId, int categoryId, String name, double price, String description, String brand, String imageUrl) {
        if (name == null || name.length() < 3) {
            return "ERROR: Product name must be at least 3 characters.";
        }
        if (price <= 0) {
            return "ERROR: Price must be greater than zero.";
        }

        int productId = productDao.addProduct(sellerId, categoryId, name, price, description, brand, imageUrl);
        if (productId > 0) {
            JSONObject res = new JSONObject();
            res.put("message", "Product added successfully");
            res.put("productId", productId);
            return "SUCCESS " + res.toString();
        }
        return "ERROR: Failed to add product.";
    }

    public String getProductsBySeller(int sellerId) {
        var products = productDao.getProductsBySellerId(sellerId);
        JSONArray jsonArray = new JSONArray();
        for (var p : products) {
            jsonArray.put(productToJson(p));
        }
        return jsonArray.toString();
    }

    public String getProductDetails(int productId) {
        ProductEntity product = productDao.getProductById(productId);
        if (product == null) {
            return "ERROR: Product not found.";
        }
        return productToJson(product).toString();
    }

    public String updateProductStatus(int productId, String newStatus) {
        if (!"AVAILABLE".equals(newStatus) && !"SOLD".equals(newStatus)) {
            return "ERROR: Invalid status. Must be AVAILABLE or SOLD.";
        }
        boolean success = productDao.updateProductStatus(productId, newStatus);
        return success ? "SUCCESS: Product status updated." : "ERROR: Failed to update product status.";
    }

    public String getProductInfo(int productId) {
        return getProductDetails(productId);
    }

    public boolean removeProduct(int productId) {
        return productDao.deleteProduct(productId);
    }

    public boolean updateProduct(int productId, String name, double price, String description) {
        return updateProduct(productId, name, price, description, null, null);
    }

    public boolean updateProduct(int productId, String name, double price, String description, String brand, String imageUrl) {
        if (name != null && name.length() < 3) return false;
        if (price < 0) return false;
        return productDao.updateProduct(productId, name, price, description, brand, imageUrl);
    }

    private JSONObject productToJson(ProductEntity p) {
        JSONObject obj = new JSONObject();
        obj.put("productId", p.getProductId());
        obj.put("sellerId", p.getSellerId());
        obj.put("categoryId", p.getCategoryId());
        obj.put("name", p.getName());
        obj.put("brand", p.getBrand() != null ? p.getBrand() : "");
        obj.put("price", p.getPrice());
        obj.put("status", p.getStatus());
        obj.put("description", p.getDescription() != null ? p.getDescription() : "");
        obj.put("imageUrl", p.getImageUrl() != null ? p.getImageUrl() : "");
        if (p.getCreatedAt() != null) obj.put("createdAt", p.getCreatedAt().toString());
        return obj;
    }
}
