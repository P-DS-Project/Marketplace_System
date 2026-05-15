package Microservices;

import DAOs.CartDAO;
import DAOs.ProductDAO;
import Entities.CartItemEntity;
import Entities.ProductEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class CartService {

    private final CartDAO cartDao;
    private final ProductDAO productDao;

    public CartService() {
        this.cartDao = new CartDAO();
        this.productDao = new ProductDAO();
    }

    public String addToCart(int userId, int productId, int quantity) {
        if (userId <= 0 || productId <= 0 || quantity <= 0) {
            return "ERROR: Invalid parameters.";
        }

        ProductEntity product = productDao.getProductById(productId);
        if (product == null) {
            return "ERROR: Product not found.";
        }
        if (!"IN_STOCK".equals(product.getStatus())) {
            return "ERROR: Product is not available.";
        }
        if (product.getSellerId() == userId) {
            return "ERROR: Cannot add your own product to cart.";
        }

        boolean success = cartDao.addToCart(userId, productId, quantity);
        if (success) {
            JSONObject res = new JSONObject();
            res.put("message", "Added to cart");
            res.put("cartCount", cartDao.getCartCount(userId));
            return "SUCCESS " + res.toString();
        }
        return "ERROR: Failed to add to cart.";
    }

    public String getCart(int userId) {
        List<CartItemEntity> items = cartDao.getCartItems(userId);
        JSONArray arr = new JSONArray();
        double total = 0;

        for (CartItemEntity item : items) {
            ProductEntity product = productDao.getProductById(item.getProductId());
            JSONObject obj = new JSONObject();
            obj.put("cartItemId", item.getCartItemId());
            obj.put("productId", item.getProductId());
            obj.put("quantity", item.getQuantity());
            obj.put("addedAt", item.getAddedAt() != null ? item.getAddedAt() : "");

            if (product != null) {
                obj.put("productName", product.getName());
                obj.put("productPrice", product.getPrice());
                obj.put("productImageUrl", product.getImageUrl() != null ? product.getImageUrl() : "");
                obj.put("productStatus", product.getStatus());
                obj.put("sellerId", product.getSellerId());
                obj.put("lineTotal", product.getPrice() * item.getQuantity());
                total += product.getPrice() * item.getQuantity();
            } else {
                obj.put("productName", "Unknown Product");
                obj.put("productPrice", 0);
                obj.put("productImageUrl", "");
                obj.put("productStatus", "REMOVED");
                obj.put("lineTotal", 0);
            }
            arr.put(obj);
        }

        JSONObject res = new JSONObject();
        res.put("items", arr);
        res.put("totalItems", items.size());
        res.put("totalAmount", total);
        res.put("cartCount", cartDao.getCartCount(userId));
        return "SUCCESS " + res.toString();
    }

    public String updateQuantity(int cartItemId, int quantity) {
        boolean success;
        if (quantity <= 0) {
            success = cartDao.removeItem(cartItemId);
        } else {
            success = cartDao.updateQuantity(cartItemId, quantity);
        }
        return success ? "SUCCESS {\"message\":\"Cart updated\"}" : "ERROR: Failed to update cart.";
    }

    public String removeItem(int cartItemId) {
        boolean success = cartDao.removeItem(cartItemId);
        return success ? "SUCCESS {\"message\":\"Item removed\"}" : "ERROR: Failed to remove item.";
    }

    public String clearCart(int userId) {
        boolean success = cartDao.clearCart(userId);
        return success ? "SUCCESS {\"message\":\"Cart cleared\"}" : "ERROR: Failed to clear cart.";
    }

    public String getCartCount(int userId) {
        int count = cartDao.getCartCount(userId);
        return "SUCCESS {\"cartCount\":" + count + "}";
    }
}
