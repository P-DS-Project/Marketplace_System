package services;

import network.SocketClient;
import org.json.JSONObject;

public class TransactionApiService {

    private final SocketClient client = SocketClient.getInstance();

    public JSONObject buy(int buyerId, int productId, int quantity) {
        JSONObject payload = new JSONObject();
        payload.put("buyerId", buyerId);
        payload.put("productId", productId);
        payload.put("quantity", quantity);

        String response = client.sendRequest("TRANSACTION", "BUY", payload);
        int code = SocketClient.getStatusCode(response);
        JSONObject body = SocketClient.getResponseBody(response);

        JSONObject result = new JSONObject();
        result.put("success", code == 200);
        if (code == 200) {
            result.put("message", body.optString("message", "Purchase successful"));
            result.put("totalPaid", body.optDouble("totalPaid", 0));
            result.put("remainingBalance", body.optDouble("remainingBalance", 0));
        } else {
            result.put("error", body.optString("error", "Purchase failed"));
        }
        return result;
    }

    public JSONObject deposit(int userId, double amount) {
        JSONObject payload = new JSONObject();
        payload.put("userId", userId);
        payload.put("amount", amount);

        String response = client.sendRequest("TRANSACTION", "DEPOSIT", payload);
        int code = SocketClient.getStatusCode(response);
        JSONObject body = SocketClient.getResponseBody(response);

        JSONObject result = new JSONObject();
        result.put("success", code == 200);
        if (code == 200) {
            result.put("message", body.optString("message", "Deposit successful"));
            result.put("newBalance", body.optDouble("newBalance", 0));
        } else {
            result.put("error", body.optString("error", "Deposit failed"));
        }
        return result;
    }
}
