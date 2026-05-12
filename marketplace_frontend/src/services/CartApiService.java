package services;

import models.CartItem;
import network.SocketClient;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class CartApiService {

    private final SocketClient client = SocketClient.getInstance();

    public JSONObject addToCart(int userId, int productId, int quantity) {
        JSONObject payload = new JSONObject();
        payload.put("userId", userId);
        payload.put("productId", productId);
        payload.put("quantity", quantity);

        String response = client.sendRequest("CART", "ADD_TO_CART", payload);
        int code = SocketClient.getStatusCode(response);
        JSONObject body = SocketClient.getResponseBody(response);

        JSONObject result = new JSONObject();
        if (code == 200) {
            result.put("success", true);
            result.put("message", body.optString("message", "Added to cart"));
            result.put("cartCount", body.optInt("cartCount", 0));
        } else {
            result.put("success", false);
            result.put("error", body.optString("error", "Failed to add to cart"));
        }
        return result;
    }

    public List<CartItem> getCart(int userId) {
        JSONObject payload = new JSONObject();
        payload.put("userId", userId);

        String response = client.sendRequest("CART", "GET_CART", payload);
        int code = SocketClient.getStatusCode(response);
        List<CartItem> items = new ArrayList<>();

        if (code == 200) {
            JSONObject body = SocketClient.getResponseBody(response);
            JSONArray arr = body.optJSONArray("items");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    CartItem item = new CartItem();
                    item.setCartItemId(obj.optInt("cartItemId"));
                    item.setProductId(obj.optInt("productId"));
                    item.setProductName(obj.optString("productName", ""));
                    item.setProductImageUrl(obj.optString("productImageUrl", ""));
                    item.setUnitPrice(obj.optDouble("productPrice", 0));
                    item.setQuantity(obj.optInt("quantity", 1));
                    item.setLineTotal(obj.optDouble("lineTotal", 0));
                    item.setProductStatus(obj.optString("productStatus", ""));
                    item.setSellerId(obj.optInt("sellerId", 0));
                    items.add(item);
                }
            }
        }
        return items;
    }

    public double getCartTotal(int userId) {
        JSONObject payload = new JSONObject();
        payload.put("userId", userId);

        String response = client.sendRequest("CART", "GET_CART", payload);
        int code = SocketClient.getStatusCode(response);
        if (code == 200) {
            JSONObject body = SocketClient.getResponseBody(response);
            return body.optDouble("totalAmount", 0);
        }
        return 0;
    }

    public boolean updateCartQuantity(int cartItemId, int quantity) {
        JSONObject payload = new JSONObject();
        payload.put("cartItemId", cartItemId);
        payload.put("quantity", quantity);

        String response = client.sendRequest("CART", "UPDATE_CART_ITEM", payload);
        return SocketClient.getStatusCode(response) == 200;
    }

    public boolean removeFromCart(int cartItemId) {
        JSONObject payload = new JSONObject();
        payload.put("cartItemId", cartItemId);

        String response = client.sendRequest("CART", "REMOVE_FROM_CART", payload);
        return SocketClient.getStatusCode(response) == 200;
    }

    public boolean clearCart(int userId) {
        JSONObject payload = new JSONObject();
        payload.put("userId", userId);

        String response = client.sendRequest("CART", "CLEAR_CART", payload);
        return SocketClient.getStatusCode(response) == 200;
    }

    public int getCartCount(int userId) {
        JSONObject payload = new JSONObject();
        payload.put("userId", userId);

        String response = client.sendRequest("CART", "GET_CART_COUNT", payload);
        int code = SocketClient.getStatusCode(response);
        if (code == 200) {
            JSONObject body = SocketClient.getResponseBody(response);
            return body.optInt("cartCount", 0);
        }
        return 0;
    }
}
