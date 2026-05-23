package Microservices;

import DAOs.TransactionDAO;
import DAOs.ProductDAO;
import DAOs.InventoryDAO;
import DAOs.ReportDAO;

import Entities.TransactionEntity;
import Entities.ProductEntity;
import Entities.InventoryEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class ReportService {

    private final TransactionDAO transactionDao;
    private final ProductDAO productDao;
    private final InventoryDAO inventoryDao;
    private final ReportDAO reportDao;

    public ReportService() {
        this.transactionDao = new TransactionDAO();
        this.productDao = new ProductDAO();
        this.inventoryDao = new InventoryDAO();
        this.reportDao = new ReportDAO();
    }

    public String getTransactionHistory(int userId) {
        try {
            List<TransactionEntity> transactions = transactionDao.getTransactionsByUserId(userId);

            JSONArray txArray = new JSONArray();
            double totalSpent = 0;
            double totalEarned = 0;
            int purchaseCount = 0;
            int saleCount = 0;

            for (TransactionEntity t : transactions) {
                JSONObject tObj = new JSONObject();
                tObj.put("transactionId", t.getTransactionId());
                tObj.put("buyerId", t.getBuyerId());
                tObj.put("sellerId", t.getSellerId());
                tObj.put("productId", t.getProductId());
                tObj.put("quantity", t.getQuantity());
                tObj.put("amount", t.getAmount());
                tObj.put("status", t.getStatus());
                tObj.put("type", t.getType());
                tObj.put("createdAt", t.getCreated_at() != null ? t.getCreated_at() : "");
                txArray.put(tObj);

                if (t.getBuyerId() == userId) {
                    totalSpent += t.getAmount();
                    purchaseCount++;
                }
                if (t.getSellerId() == userId) {
                    totalEarned += t.getAmount();
                    saleCount++;
                }
            }

            JSONObject report = new JSONObject();
            report.put("transactions", txArray);
            report.put("totalTransactions", transactions.size());
            report.put("totalSpent", totalSpent);
            report.put("totalEarned", totalEarned);
            report.put("purchaseCount", purchaseCount);
            report.put("saleCount", saleCount);

            // Persist report to database
            String params = new JSONObject().put("userId", userId).toString();
            String summary = String.format("Transaction history: %d transactions, spent $%.2f, earned $%.2f, %d purchases, %d sales.",
                transactions.size(), totalSpent, totalEarned, purchaseCount, saleCount);
            int reportId = reportDao.insertReport(userId, "TRANSACTION_HISTORY", params, summary);
            report.put("reportId", reportId);

            return "SUCCESS " + report.toString();
        } catch (Exception e) {
            return "ERROR: Failed to generate transaction history - " + e.getMessage();
        }
    }

    public String getSalesReport(int sellerId) {
        try {
            List<TransactionEntity> transactions = transactionDao.getTransactionsByUserId(sellerId);
            List<ProductEntity> products = productDao.getProductsBySellerId(sellerId);

            double totalRevenue = 0;
            int totalSales = 0;
            int totalItemsSold = 0;

            JSONArray salesArray = new JSONArray();
            for (TransactionEntity t : transactions) {
                if (t.getSellerId() == sellerId) {
                    JSONObject sale = new JSONObject();
                    sale.put("transactionId", t.getTransactionId());
                    sale.put("buyerId", t.getBuyerId());
                    sale.put("productId", t.getProductId());
                    sale.put("quantity", t.getQuantity());
                    sale.put("amount", t.getAmount());
                    sale.put("status", t.getStatus());
                    sale.put("createdAt", t.getCreated_at() != null ? t.getCreated_at() : "");
                    salesArray.put(sale);

                    totalRevenue += t.getAmount();
                    totalSales++;
                    totalItemsSold += t.getQuantity();
                }
            }

            int activeProducts = 0;
            int soldProducts = 0;
            for (ProductEntity p : products) {
                if ("IN_STOCK".equals(p.getStatus()))
                    activeProducts++;
                if ("OUT_OF_STOCK".equals(p.getStatus()))
                    soldProducts++;
            }

            JSONObject report = new JSONObject();
            report.put("sales", salesArray);
            report.put("totalRevenue", totalRevenue);
            report.put("totalSales", totalSales);
            report.put("totalItemsSold", totalItemsSold);
            report.put("totalProducts", products.size());
            report.put("activeProducts", activeProducts);
            report.put("soldProducts", soldProducts);
            report.put("averageOrderValue", totalSales > 0 ? totalRevenue / totalSales : 0);

            // Persist report to database
            String params = new JSONObject().put("sellerId", sellerId).toString();
            String summary = String.format("Sales report: $%.2f revenue, %d sales, %d items sold, %d active products, avg order $%.2f.",
                totalRevenue, totalSales, totalItemsSold, activeProducts, totalSales > 0 ? totalRevenue / totalSales : 0);
            int reportId = reportDao.insertReport(sellerId, "SALES_REPORT", params, summary);
            report.put("reportId", reportId);

            return "SUCCESS " + report.toString();
        } catch (Exception e) {
            return "ERROR: Failed to generate sales report - " + e.getMessage();
        }
    }

    public String getInventoryReport(int requestedBy) {
        try {
            JSONArray inventoryArray = new JSONArray();
            // Query all available products and their inventory
            List<ProductEntity> products = productDao.advancedSearch(
                    null, null, null, null, null, null, null, null, "name", "ASC", 1000, 0);

            int totalProducts = products.size();
            int inStockCount = 0;
            int outOfStockCount = 0;

            for (ProductEntity p : products) {
                InventoryEntity inv = inventoryDao.getInventoryByProductId(p.getProductId());
                JSONObject item = new JSONObject();
                item.put("productId", p.getProductId());
                item.put("productName", p.getName());
                item.put("sellerId", p.getSellerId());
                item.put("price", p.getPrice());
                item.put("status", p.getStatus());
                if (inv != null) {
                    item.put("quantity", inv.getQuantity());
                    item.put("warehouseNode", inv.getWarehouse_node());
                    if (inv.getQuantity() > 0)
                        inStockCount++;
                    else
                        outOfStockCount++;
                } else {
                    item.put("quantity", 0);
                    item.put("warehouseNode", "N/A");
                    outOfStockCount++;
                }
                inventoryArray.put(item);
            }

            JSONObject report = new JSONObject();
            report.put("inventory", inventoryArray);
            report.put("totalProducts", totalProducts);
            report.put("inStockCount", inStockCount);
            report.put("outOfStockCount", outOfStockCount);

            // Persist report to database (generated_by = 0 means system/anonymous)
            String params = new JSONObject().put("scope", "all").toString();
            String summary = String.format("Inventory report: %d total products, %d in stock, %d out of stock.",
                totalProducts, inStockCount, outOfStockCount);
            int reportId = reportDao.insertReport(requestedBy, "INVENTORY_REPORT", params, summary);
            report.put("reportId", reportId);

            return "SUCCESS " + report.toString();
        } catch (Exception e) {
            return "ERROR: Failed to generate inventory report - " + e.getMessage();
        }
    }

    public String getSystemStatistics(int requestedBy) {
        try {
            // Get all products
            List<ProductEntity> products = productDao.advancedSearch(
                    null, null, null, null, null, null, null, null, "created_at", "DESC", 10000, 0);

            int totalProducts = products.size();
            int availableProducts = 0;
            int soldProducts = 0;
            for (ProductEntity p : products) {
                if ("IN_STOCK".equals(p.getStatus()))
                    availableProducts++;
                if ("OUT_OF_STOCK".equals(p.getStatus()))
                    soldProducts++;
            }

            JSONObject report = new JSONObject();
            report.put("totalProducts", totalProducts);
            report.put("availableProducts", availableProducts);
            report.put("soldProducts", soldProducts);

            // Persist report to database (generated_by = 0 means system/anonymous)
            String params = new JSONObject().put("scope", "system").toString();
            String summary = String.format("System statistics: %d total products, %d available, %d sold out.",
                totalProducts, availableProducts, soldProducts);
            int reportId = reportDao.insertReport(requestedBy, "SYSTEM_STATISTICS", params, summary);
            report.put("reportId", reportId);

            return "SUCCESS " + report.toString();
        } catch (Exception e) {
            return "ERROR: Failed to generate system statistics - " + e.getMessage();
        }
    }
}
