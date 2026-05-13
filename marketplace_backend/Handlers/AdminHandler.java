package Handlers;

import Microservices.AdminService;
import org.json.JSONObject;

public class AdminHandler implements ServiceHandler {

    private final AdminService adminService;

    public AdminHandler() {
        this.adminService = new AdminService();
    }

    @Override
    public String handleRequest(String action, String jsonPayload) {
        JSONObject json;
        try {
            json = new JSONObject(jsonPayload != null ? jsonPayload : "{}");
        } catch (Exception e) {
            return "400 {\"error\":\"Invalid JSON payload format\"}";
        }

        String token = json.optString("token", null);

        switch (action.toUpperCase()) {

            case "GET_DASHBOARD_STATS": {
                String result = adminService.getDashboardStats(token);
                return formatResponse(result);
            }

            case "GET_ALL_USERS": {
                String result = adminService.getAllUsers(token);
                return formatResponse(result);
            }

            case "SEARCH_USERS": {
                String query = json.optString("query", "");
                String result = adminService.searchUsers(token, query);
                return formatResponse(result);
            }

            case "PROMOTE_USER": {
                int targetUserId = json.optInt("targetUserId", -1);
                if (targetUserId == -1) return "400 {\"error\":\"Missing targetUserId\"}";
                String result = adminService.promoteToAdmin(token, targetUserId);
                return formatResponse(result);
            }

            case "DEMOTE_USER": {
                int targetUserId = json.optInt("targetUserId", -1);
                if (targetUserId == -1) return "400 {\"error\":\"Missing targetUserId\"}";
                String result = adminService.demoteFromAdmin(token, targetUserId);
                return formatResponse(result);
            }

            case "DELETE_USER": {
                int targetUserId = json.optInt("targetUserId", -1);
                if (targetUserId == -1) return "400 {\"error\":\"Missing targetUserId\"}";
                String result = adminService.deleteUser(token, targetUserId);
                return formatResponse(result);
            }

            case "TOGGLE_USER_ACTIVE": {
                int targetUserId = json.optInt("targetUserId", -1);
                boolean active = json.optBoolean("active", true);
                if (targetUserId == -1) return "400 {\"error\":\"Missing targetUserId\"}";
                String result = adminService.toggleUserActive(token, targetUserId, active);
                return formatResponse(result);
            }

            case "GET_USER_DETAILS": {
                int userId = json.optInt("userId", -1);
                if (userId == -1) return "400 {\"error\":\"Missing userId\"}";
                String result = adminService.getUserDetails(token, userId);
                return formatResponse(result);
            }

            case "GET_ALL_PRODUCTS": {
                String result = adminService.getAllProducts(token);
                return formatResponse(result);
            }

            case "DELETE_PRODUCT": {
                int productId = json.optInt("productId", -1);
                if (productId == -1) return "400 {\"error\":\"Missing productId\"}";
                String result = adminService.adminDeleteProduct(token, productId);
                return formatResponse(result);
            }

            case "UPDATE_PRODUCT": {
                int productId = json.optInt("productId", -1);
                if (productId == -1) return "400 {\"error\":\"Missing productId\"}";
                String name = json.optString("name", null);
                double price = json.optDouble("price", -1);
                String description = json.optString("description", null);
                String brand = json.optString("brand", null);
                String imageUrl = json.optString("imageUrl", null);
                String status = json.optString("status", null);
                String result = adminService.adminUpdateProduct(token, productId, name, price, description, brand, imageUrl, status);
                return formatResponse(result);
            }

            case "GET_ALL_TRANSACTIONS": {
                String result = adminService.getAllTransactions(token);
                return formatResponse(result);
            }

            default:
                return "400 {\"error\":\"Unknown Admin Action: " + action + "\"}";
        }
    }

    private String formatResponse(String result) {
        if (result.startsWith("SUCCESS ")) {
            return "200 " + result.substring(8);
        } else if (result.startsWith("SUCCESS:")) {
            return "200 {\"message\":\"" + result + "\"}";
        } else if (result.startsWith("ERROR: Access denied")) {
            return "403 {\"error\":\"" + result + "\"}";
        } else if (result.startsWith("ERROR")) {
            return "400 {\"error\":\"" + result + "\"}";
        }
        return "200 " + result;
    }
}
