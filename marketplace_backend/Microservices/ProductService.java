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
        if (name == null || name.length() < 3) {
            return "ERROR: Product name must be at least 3 characters.";
        }
        if (price <= 0) {
            return "ERROR: Price must be greater than zero.";
        }

        boolean success = productDao.addProduct(sellerId, categoryId, name, price, description);
        return success ? "SUCCESS: Product added." : "ERROR: Failed to add product.";
    }

    public String getProductsBySeller(int sellerId) {
        var products = productDao.getProductsBySellerId(sellerId);
        JSONArray jsonArray = new JSONArray();
        for (var p : products) {
            JSONObject obj = new JSONObject();
            obj.put("productId", p.getProductId());
            obj.put("sellerId", p.getSellerId());
            obj.put("categoryId", p.getCategoryId());
            obj.put("name", p.getName());
            obj.put("price", p.getPrice());
            obj.put("status", p.getStatus());
            jsonArray.put(obj);
        }
        return jsonArray.toString();
    }

    public String getProductDetails(int productId) {
        ProductEntity product = productDao.getProductById(productId);
        if (product == null) {
            return "ERROR: Product not found.";
        }
        JSONObject obj = new JSONObject();
        obj.put("productId", product.getProductId());
        obj.put("sellerId", product.getSellerId());
        obj.put("categoryId", product.getCategoryId());
        obj.put("name", product.getName());
        obj.put("price", product.getPrice());
        obj.put("status", product.getStatus());
        obj.put("description", product.getDescription());
        return obj.toString();
    }

    public String updateProductStatus(int productId, String newStatus) {
        if (!"AVAILABLE".equals(newStatus) && !"SOLD".equals(newStatus)) {
            return "ERROR: Invalid status. Must be AVAILABLE or SOLD.";
        }
        boolean success = productDao.updateProductStatus(productId, newStatus);
        return success ? "SUCCESS: Product status updated." : "ERROR: Failed to update product status.";
    }

    public String getProductInfo(int productId) {
        ProductEntity product = productDao.getProductById(productId);
        if (product == null) {
            return "ERROR: Product not found.";
        }
        JSONObject obj = new JSONObject();
        obj.put("productId", product.getProductId());
        obj.put("sellerId", product.getSellerId());
        obj.put("categoryId", product.getCategoryId());
        obj.put("name", product.getName());
        obj.put("price", product.getPrice());
        obj.put("status", product.getStatus());
        obj.put("description", product.getDescription());
        return obj.toString();
    }

    public boolean removeProduct(int productId) {
        return productDao.deleteProduct(productId);
    }

    public boolean updateProduct(int productId, String name, double price, String description) {
        if (name != null && name.length() < 3) {
            return false;
        }
        if (price < 0) {
            return false;
        }
        return productDao.updateProduct(productId, name, price, description);
    }

}
