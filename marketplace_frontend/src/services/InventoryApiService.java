package services;

import models.Inventory;
import network.SocketClient;
import org.json.JSONObject;

public class InventoryApiService {

    private final SocketClient client = SocketClient.getInstance();

    public String addStock(int productId, int quantity, String warehouseNode) {
        JSONObject payload = new JSONObject();
        payload.put("productId", productId);
        payload.put("quantity", quantity);
        payload.put("warehouseNode", warehouseNode);

        String response = client.sendRequest("INVENTORY", "ADD", payload);
        int code = SocketClient.getStatusCode(response);
        JSONObject body = SocketClient.getResponseBody(response);

        if (code == 200) return "SUCCESS";
        return "ERROR: " + body.optString("error", "Failed to add stock");
    }

    public String reduceStock(int productId, int quantity, String warehouseNode) {
        JSONObject payload = new JSONObject();
        payload.put("productId", productId);
        payload.put("quantity", quantity);
        payload.put("warehouseNode", warehouseNode);

        String response = client.sendRequest("INVENTORY", "REDUCE", payload);
        int code = SocketClient.getStatusCode(response);
        JSONObject body = SocketClient.getResponseBody(response);

        if (code == 200) return "SUCCESS";
        return "ERROR: " + body.optString("error", "Failed to reduce stock");
    }

    public boolean checkStock(int productId, int requiredQuantity) {
        JSONObject payload = new JSONObject();
        payload.put("productId", productId);
        payload.put("requiredQuantity", requiredQuantity);

        String response = client.sendRequest("INVENTORY", "CHECK", payload);
        return SocketClient.getStatusCode(response) == 200;
    }

    public Inventory getInventoryDetails(int productId) {
        JSONObject payload = new JSONObject();
        payload.put("productId", productId);

        String response = client.sendRequest("INVENTORY", "RETRIEVE", payload);
        int code = SocketClient.getStatusCode(response);

        if (code == 200) {
            JSONObject body = SocketClient.getResponseBody(response);
            Inventory inv = new Inventory();
            inv.setProductId(body.optInt("productId", productId));
            inv.setQuantity(body.optInt("quantity", 0));
            inv.setWarehouseNode(body.optString("warehouseNode", "N/A"));
            return inv;
        }
        return null;
    }
}
