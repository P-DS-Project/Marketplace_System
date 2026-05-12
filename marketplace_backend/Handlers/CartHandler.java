package Handlers;

import Microservices.CartService;
import org.json.JSONObject;

public class CartHandler implements ServiceHandler {

    private final CartService cartService;

    public CartHandler() {
        this.cartService = new CartService();
    }

    @Override
    public String handleRequest(String action, String jsonPayload) {
        JSONObject json;
        try {
            json = new JSONObject(jsonPayload != null ? jsonPayload : "{}");
        } catch (Exception e) {
            return "400 {\"error\":\"Invalid JSON payload format\"}";
        }

        switch (action.toUpperCase()) {
            case "ADD_TO_CART": {
                int userId = json.optInt("userId", -1);
                int productId = json.optInt("productId", -1);
                int quantity = json.optInt("quantity", 1);
                if (userId == -1 || productId == -1) {
                    return "400 {\"error\":\"Missing userId or productId\"}";
                }
                return formatResponse(cartService.addToCart(userId, productId, quantity));
            }
            case "GET_CART": {
                int userId = json.optInt("userId", -1);
                if (userId == -1)
                    return "400 {\"error\":\"Missing userId\"}";
                return formatResponse(cartService.getCart(userId));
            }
            case "UPDATE_CART_ITEM": {
                int cartItemId = json.optInt("cartItemId", -1);
                int quantity = json.optInt("quantity", 0);
                if (cartItemId == -1)
                    return "400 {\"error\":\"Missing cartItemId\"}";
                return formatResponse(cartService.updateQuantity(cartItemId, quantity));
            }
            case "REMOVE_FROM_CART": {
                int cartItemId = json.optInt("cartItemId", -1);
                if (cartItemId == -1)
                    return "400 {\"error\":\"Missing cartItemId\"}";
                return formatResponse(cartService.removeItem(cartItemId));
            }
            case "CLEAR_CART": {
                int userId = json.optInt("userId", -1);
                if (userId == -1)
                    return "400 {\"error\":\"Missing userId\"}";
                return formatResponse(cartService.clearCart(userId));
            }
            case "GET_CART_COUNT": {
                int userId = json.optInt("userId", -1);
                if (userId == -1)
                    return "400 {\"error\":\"Missing userId\"}";
                return formatResponse(cartService.getCartCount(userId));
            }
            default:
                return "400 {\"error\":\"Unknown Cart Action: " + action + "\"}";
        }
    }

    private String formatResponse(String result) {
        if (result.startsWith("SUCCESS ")) {
            return "200 " + result.substring(8);
        } else if (result.startsWith("ERROR")) {
            return "400 {\"error\":\"" + result + "\"}";
        }
        return "200 " + result;
    }
}
