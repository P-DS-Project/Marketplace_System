package Handlers;

import Microservices.ProductService;
import org.json.JSONObject;

public class ProductHandler implements ServiceHandler {

    private final ProductService productService;

    public ProductHandler() {
        this.productService = new ProductService();
    }

    @Override
    public String handleRequest(String action, String jsonPayload) {
        JSONObject json;
        try {
            json = new JSONObject(jsonPayload != null ? jsonPayload : "{}");
        } catch (Exception e) {
            return "400 {\"error\":\"Invalid request: " + e.getMessage() + "\"}";
        }

        switch (action.toUpperCase()) {
            case "ADD":
            case "ADD_PRODUCT": {
                int sellerId = json.optInt("sellerId", -1);
                int categoryId = json.optInt("categoryId", -1);
                String name = json.optString("name", "");
                double price = json.optDouble("price", -1.0);
                String description = json.optString("description", "");
                String brand = json.optString("brand", null);
                String imageUrl = json.optString("imageUrl", null);

                String result = productService.addProduct(sellerId, categoryId, name, price, description, brand, imageUrl);
                if (result.startsWith("SUCCESS")) {
                    return "201 " + result.substring(result.indexOf(" ") + 1);
                }
                return "400 {\"error\":\"" + result + "\"}";
            }
            case "GET_PRODUCTS_BY_SELLER": {
                int sellerId = json.optInt("sellerId", -1);
                String result = productService.getProductsBySeller(sellerId);
                return "200 {\"products\":" + result + "}";
            }
            case "GET_PRODUCT_DETAILS":
            case "GET_PRODUCT_INFO": {
                int productId = json.optInt("productId", -1);
                String result = productService.getProductDetails(productId);
                if (result.startsWith("ERROR")) {
                    return "404 {\"error\":\"" + result + "\"}";
                }
                return "200 " + result;
            }
            case "UPDATE_PRODUCT_STATUS": {
                int productId = json.optInt("productId", -1);
                String newStatus = json.optString("status", "");
                String result = productService.updateProductStatus(productId, newStatus);
                if (result.startsWith("ERROR")) {
                    return "400 {\"error\":\"" + result + "\"}";
                }
                return "200 {\"message\":\"" + result + "\"}";
            }
            case "REMOVE_PRODUCT": {
                int productId = json.optInt("productId", -1);
                boolean success = productService.removeProduct(productId);
                if (success) {
                    return "200 {\"message\":\"Product removed successfully\"}";
                }
                return "400 {\"error\":\"Failed to remove product\"}";
            }
            case "UPDATE_PRODUCT": {
                int productId = json.optInt("productId", -1);
                String name = json.optString("name", null);
                double price = json.optDouble("price", -1.0);
                String description = json.optString("description", null);
                String brand = json.optString("brand", null);
                String imageUrl = json.optString("imageUrl", null);

                boolean success = productService.updateProduct(productId, name, price, description, brand, imageUrl);
                if (success) {
                    return "200 {\"message\":\"Product updated successfully\"}";
                }
                return "400 {\"error\":\"Failed to update product\"}";
            }
            default:
                return "400 {\"error\":\"Unknown Product Action: " + action + "\"}";
        }
    }
}
