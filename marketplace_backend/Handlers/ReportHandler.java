package Handlers;

import Microservices.ReportService;
import org.json.JSONObject;

public class ReportHandler implements ServiceHandler {
    private final ReportService reportService;

    public ReportHandler() {
        this.reportService = new ReportService();
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

            case "GET_TRANSACTION_HISTORY": {
                int userId = json.optInt("userId", -1);
                if (userId == -1) {
                    return "400 {\"error\":\"Missing userId\"}";
                }
                String result = reportService.getTransactionHistory(userId);
                return formatResponse(result);
            }

            case "GET_SALES_REPORT": {
                int sellerId = json.optInt("sellerId", -1);
                if (sellerId == -1) {
                    return "400 {\"error\":\"Missing sellerId\"}";
                }
                String result = reportService.getSalesReport(sellerId);
                return formatResponse(result);
            }

            case "GET_INVENTORY_REPORT": {
                int requestedBy = json.optInt("userId", json.optInt("sellerId", 0));
                String result = reportService.getInventoryReport(requestedBy);
                return formatResponse(result);
            }

            case "GET_SYSTEM_STATISTICS": {
                int requestedBy = json.optInt("userId", 0);
                String result = reportService.getSystemStatistics(requestedBy);
                return formatResponse(result);
            }

            default:
                return "400 {\"error\":\"Unknown Report Action: " + action + "\"}";
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
