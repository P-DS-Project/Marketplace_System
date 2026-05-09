package Handlers;
import Handlers.ServiceHandler;
import Microservices.ReportService;

public class ReportHandler implements ServiceHandler {
    private final ReportService reportService;

    public ReportHandler() {
        this.reportService = new ReportService();
    }

    @Override
    public String handleRequest(String action, String jsonPayload) {

        switch (action.toUpperCase()) {

            // ==============================
            // TRANSACTION REPORTS
            // ==============================

            case "GENERATE_TRANSACTION_REPORT":
                return reportService.generateTransactionReport(jsonPayload);

            case "GET_TRANSACTION_HISTORY":
                return reportService.getTransactionHistory(jsonPayload);

            case "GET_PURCHASE_REPORT":
                return reportService.getPurchaseReport(jsonPayload);

            case "GET_SALES_REPORT":
                return reportService.getSalesReport(jsonPayload);

            // ==============================
            // INVENTORY REPORTS
            // ==============================

            case "GET_INVENTORY_REPORT":
                return reportService.getInventoryReport(jsonPayload);

            case "GET_AVAILABLE_ITEMS_REPORT":
                return reportService.getAvailableItemsReport(jsonPayload);

            case "GET_SOLD_ITEMS_REPORT":
                return reportService.getSoldItemsReport(jsonPayload);

            // ==============================
            // USER ACCOUNT REPORTS
            // ==============================

            case "GET_ACCOUNT_SUMMARY":
                return reportService.getAccountSummary(jsonPayload);

            case "GET_BALANCE_REPORT":
                return reportService.getBalanceReport(jsonPayload);

            case "GET_USER_ACTIVITY_REPORT":
                return reportService.getUserActivityReport(jsonPayload);

            // ==============================
            // ADMIN REPORTS
            // ==============================

            case "GET_SYSTEM_STATISTICS":
                return reportService.getSystemStatistics(jsonPayload);

            case "GET_TOP_SELLERS_REPORT":
                return reportService.getTopSellersReport(jsonPayload);

            case "GET_TOP_BUYERS_REPORT":
                return reportService.getTopBuyersReport(jsonPayload);

            case "GET_TOTAL_REVENUE_REPORT":
                return reportService.getTotalRevenueReport(jsonPayload);

            // ==============================
            // CHAT / BONUS FEATURE REPORTS
            // ==============================

            case "GET_CHAT_ACTIVITY_REPORT":
                return reportService.getChatActivityReport(jsonPayload);

            // ==============================
            // DEFAULT
            // ==============================

            default:
                return "{ \"status\": \"error\", \"message\": \"Invalid Report Action\" }";
        }
    }
}
