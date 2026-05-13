package services;

import network.SocketClient;
import org.json.JSONObject;

public class AdminApiService {

    private final SocketClient client = SocketClient.getInstance();

    public JSONObject getDashboardStats(String token) {
        JSONObject payload = new JSONObject();
        payload.put("token", token);
        String response = client.sendRequest("ADMIN", "GET_DASHBOARD_STATS", payload);
        int code = SocketClient.getStatusCode(response);
        if (code == 200) return SocketClient.getResponseBody(response);
        return null;
    }

    public JSONObject getAllUsers(String token) {
        JSONObject payload = new JSONObject();
        payload.put("token", token);
        String response = client.sendRequest("ADMIN", "GET_ALL_USERS", payload);
        int code = SocketClient.getStatusCode(response);
        if (code == 200) return SocketClient.getResponseBody(response);
        return null;
    }

    public JSONObject searchUsers(String token, String query) {
        JSONObject payload = new JSONObject();
        payload.put("token", token);
        payload.put("query", query);
        String response = client.sendRequest("ADMIN", "SEARCH_USERS", payload);
        int code = SocketClient.getStatusCode(response);
        if (code == 200) return SocketClient.getResponseBody(response);
        return null;
    }

    public boolean promoteUser(String token, int targetUserId) {
        JSONObject payload = new JSONObject();
        payload.put("token", token);
        payload.put("targetUserId", targetUserId);
        String response = client.sendRequest("ADMIN", "PROMOTE_USER", payload);
        return SocketClient.getStatusCode(response) == 200;
    }

    public boolean demoteUser(String token, int targetUserId) {
        JSONObject payload = new JSONObject();
        payload.put("token", token);
        payload.put("targetUserId", targetUserId);
        String response = client.sendRequest("ADMIN", "DEMOTE_USER", payload);
        return SocketClient.getStatusCode(response) == 200;
    }

    public boolean deleteUser(String token, int targetUserId) {
        JSONObject payload = new JSONObject();
        payload.put("token", token);
        payload.put("targetUserId", targetUserId);
        String response = client.sendRequest("ADMIN", "DELETE_USER", payload);
        return SocketClient.getStatusCode(response) == 200;
    }

    public boolean toggleUserActive(String token, int targetUserId, boolean active) {
        JSONObject payload = new JSONObject();
        payload.put("token", token);
        payload.put("targetUserId", targetUserId);
        payload.put("active", active);
        String response = client.sendRequest("ADMIN", "TOGGLE_USER_ACTIVE", payload);
        return SocketClient.getStatusCode(response) == 200;
    }

    public JSONObject getAllProducts(String token) {
        JSONObject payload = new JSONObject();
        payload.put("token", token);
        String response = client.sendRequest("ADMIN", "GET_ALL_PRODUCTS", payload);
        int code = SocketClient.getStatusCode(response);
        if (code == 200) return SocketClient.getResponseBody(response);
        return null;
    }

    public boolean deleteProduct(String token, int productId) {
        JSONObject payload = new JSONObject();
        payload.put("token", token);
        payload.put("productId", productId);
        String response = client.sendRequest("ADMIN", "DELETE_PRODUCT", payload);
        return SocketClient.getStatusCode(response) == 200;
    }

    public boolean updateProduct(String token, int productId, String name, double price, String description, String brand, String imageUrl, String status) {
        JSONObject payload = new JSONObject();
        payload.put("token", token);
        payload.put("productId", productId);
        if (name != null) payload.put("name", name);
        if (price >= 0) payload.put("price", price);
        if (description != null) payload.put("description", description);
        if (brand != null) payload.put("brand", brand);
        if (imageUrl != null) payload.put("imageUrl", imageUrl);
        if (status != null) payload.put("status", status);
        String response = client.sendRequest("ADMIN", "UPDATE_PRODUCT", payload);
        return SocketClient.getStatusCode(response) == 200;
    }

    public JSONObject getAllTransactions(String token) {
        JSONObject payload = new JSONObject();
        payload.put("token", token);
        String response = client.sendRequest("ADMIN", "GET_ALL_TRANSACTIONS", payload);
        int code = SocketClient.getStatusCode(response);
        if (code == 200) return SocketClient.getResponseBody(response);
        return null;
    }
}
