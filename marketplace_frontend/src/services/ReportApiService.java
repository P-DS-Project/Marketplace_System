package services;

import network.SocketClient;
import org.json.JSONObject;

public class ReportApiService {

    private final SocketClient client = SocketClient.getInstance();

    public JSONObject getTransactionHistory(int userId) {
        JSONObject payload = new JSONObject();
        payload.put("userId", userId);

        String response = client.sendRequest("REPORT", "GET_TRANSACTION_HISTORY", payload);
        int code = SocketClient.getStatusCode(response);
        if (code == 200) {
            return SocketClient.getResponseBody(response);
        }
        return null;
    }

    public JSONObject getSalesReport(int sellerId) {
        JSONObject payload = new JSONObject();
        payload.put("sellerId", sellerId);

        String response = client.sendRequest("REPORT", "GET_SALES_REPORT", payload);
        int code = SocketClient.getStatusCode(response);
        if (code == 200) {
            return SocketClient.getResponseBody(response);
        }
        return null;
    }

    public JSONObject getInventoryReport(int userId) {
        JSONObject payload = new JSONObject();
        payload.put("userId", userId);
        String response = client.sendRequest("REPORT", "GET_INVENTORY_REPORT", payload);
        int code = SocketClient.getStatusCode(response);
        if (code == 200) {
            return SocketClient.getResponseBody(response);
        }
        return null;
    }

    public JSONObject getSystemStatistics(int userId) {
        JSONObject payload = new JSONObject();
        payload.put("userId", userId);
        String response = client.sendRequest("REPORT", "GET_SYSTEM_STATISTICS", payload);
        int code = SocketClient.getStatusCode(response);
        if (code == 200) {
            return SocketClient.getResponseBody(response);
        }
        return null;
    }
}
