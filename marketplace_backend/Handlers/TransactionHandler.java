package Handlers;

import Microservices.TransactionService;
import org.json.JSONObject;

public class TransactionHandler implements ServiceHandler {
    private final TransactionService transactionService;

    public TransactionHandler() {
        this.transactionService = new TransactionService();
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
            case "BUY": {
                int buyerId = json.optInt("buyerId", -1);
                int productId = json.optInt("productId", -1);
                int quantity = json.optInt("quantity", 1);

                if (buyerId == -1 || productId == -1) {
                    return "400 {\"error\":\"Missing buyerId or productId\"}";
                }

                String result = transactionService.processPurchase(buyerId, productId, quantity);
                if (result.startsWith("SUCCESS")) {
                    return "200 " + result.substring(result.indexOf(" ") + 1);
                } else {
                    return "400 {\"error\":\"" + result + "\"}";
                }
            }
            case "DEPOSIT": {
                int userId = json.optInt("userId", -1);
                double amount = json.optDouble("amount", 0);

                if (userId == -1) return "400 {\"error\":\"Missing userId\"}";
                if (amount <= 0) return "400 {\"error\":\"Invalid deposit amount\"}";

                String result = transactionService.processDeposit(userId, amount);
                if (result.startsWith("SUCCESS")) {
                    return "200 " + result.substring(result.indexOf(" ") + 1);
                } else {
                    return "400 {\"error\":\"" + result + "\"}";
                }
            }
            case "WITHDRAW": {
                int userId = json.optInt("userId", -1);
                double amount = json.optDouble("amount", 0);

                if (userId == -1) return "400 {\"error\":\"Missing userId\"}";
                if (amount <= 0) return "400 {\"error\":\"Invalid withdrawal amount\"}";

                String result = transactionService.processWithdraw(userId, amount);
                if (result.startsWith("SUCCESS")) {
                    return "200 " + result.substring(result.indexOf(" ") + 1);
                } else {
                    return "400 {\"error\":\"" + result + "\"}";
                }
            }
            default:
                return "400 {\"error\":\"Unknown Transaction Action: " + action + "\"}";
        }
    }
}