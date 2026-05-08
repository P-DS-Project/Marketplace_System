package Handlers;

import Microservices.InventoryService;
import org.json.JSONObject;

public class InventoryHandler implements ServiceHandler {
    private InventoryService inventoryService;

    // Inventory Service constructor need to be fixed for this to work
    public InventoryHandler() {
        this.inventoryService = new InventoryService();
    }

    @Override
    public String handleRequest(String action, String jsonPayload) {
        JSONObject response;
        try {
            response = new JSONObject(jsonPayload != null ? jsonPayload : "{}");
        } catch (Exception e) {
            return "400 {\"error\":\"Invalid JSON payload format\"}";
        }
        switch (action.toUpperCase()) {
            case "ADD":
                int productId = response.optInt("productId", -1);
                int quantity = response.optInt("quantity", -1);
                String warehouseNode = response.optString("warehouseNode", null);

                if (productId == -1 || quantity == -1 || warehouseNode == null) {
                    return "400 {\"error\":\"Missing productId or quantity or warehouseNode\"}";
                }
                String result = inventoryService.addStock(productId, quantity, warehouseNode);
                if (result.startsWith("SUCCESS")) {
                    return "200 " + result.substring(result.indexOf(" ") + 1);
                } else {
                    JSONObject errorJson = new JSONObject();
                    errorJson.put("error", result);
                    return "400 " + errorJson.toString();
                }
            case "REDUCE":
                productId = response.optInt("productId", -1);
                quantity = response.optInt("quantity", -1);
                warehouseNode = response.optString("warehouseNode", null);

                if (productId == -1 || quantity == -1 || warehouseNode == null) {
                    return "400 {\"error\":\"Missing productId or quantity or warehouseNode\"}";
                }
                result = inventoryService.reduceStock(productId, quantity, warehouseNode);
                if (result.startsWith("SUCCESS")) {
                    return "200 " + result.substring(result.indexOf(" ") + 1);
                } else {
                    JSONObject errorJson = new JSONObject();
                    errorJson.put("error", result);
                    return "400 " + errorJson.toString();
                }
            case "CHECK":
                productId = response.optInt("productId", -1);
                int requiredQuantity = response.optInt("requiredQuantity", -1);
                if (productId == -1 || requiredQuantity == -1) {
                    return "400 {\"error\":\"Missing productId or requiredQuantity\"}";
                }
                result = inventoryService.checkStock(productId, requiredQuantity);
                if (result.startsWith("SUCCESS")) {
                    return "200 " + result.substring(result.indexOf(" ") + 1);
                } else {
                    JSONObject errorJson = new JSONObject();
                    errorJson.put("error", result);
                    return "400 " + errorJson.toString();
                }
            case "RETRIEVE":
                productId = response.optInt("productId", -1);
                if (productId == -1) {
                    return "400 {\"error\":\"Missing productId\"}";
                }
                result = inventoryService.getInventoryDetails(productId);
                if (result.startsWith("SUCCESS")) {
                    return "200 " + result.substring(result.indexOf(" ") + 1);
                } else {
                    JSONObject errorJson = new JSONObject();
                    errorJson.put("error", result);
                    return "400 " + errorJson.toString();
                }
            default:
                return "400 {\"error\":\"Unknown Inventory Action: " + action + "\"}";
        }
    }
}
