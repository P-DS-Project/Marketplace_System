package Microservices;

import DAOs.AccountDAO;
import DAOs.ProductDAO;
import DAOs.TransactionDAO;
import Entities.AccountEntity;
import Entities.ProductEntity;
import org.json.JSONObject;

public class TransactionService {
    private final AccountDAO accountDao;
    private final ProductDAO productDao;
    private final TransactionDAO transactionDao;

    public TransactionService() {
        this.accountDao = new AccountDAO();
        this.productDao = new ProductDAO();
        this.transactionDao = new TransactionDAO();
    }

    public String processPurchase(int buyerId, int productId, int quantity) {
        if (quantity <= 0) return "ERROR: Invalid quantity.";

        try {
            ProductEntity product = productDao.getProductById(productId);
            if (product == null || !"AVAILABLE".equals(product.getStatus())) {
                return "ERROR: Product not found or unavailable.";
            }

            if (product.getSellerId() == buyerId) {
                return "ERROR: Sellers cannot purchase their own items.";
            }

            double totalCost = product.getPrice() * quantity;
            AccountEntity buyerAcc = accountDao.getAccountByUserId(buyerId);
            if (buyerAcc == null || buyerAcc.getBalance() < totalCost) {
                return "ERROR: Insufficient funds in account.";
            }

            double newBalance = buyerAcc.getBalance() - totalCost;
            boolean paymentSuccess = accountDao.updateBalance(buyerId, newBalance);
            
            if (!paymentSuccess) {
                return "ERROR: Payment processing failed.";
            }

            boolean logSuccess = transactionDao.insertTransaction(
                buyerId, product.getSellerId(), productId, quantity, totalCost, "COMPLETED", "PURCHASE"
            );

            if (!logSuccess) {
                accountDao.updateBalance(buyerId, buyerAcc.getBalance());
                return "ERROR: Failed to record transaction. Money refunded.";
            }

            JSONObject res = new JSONObject();
            res.put("message", "Purchase successful");
            res.put("totalPaid", totalCost);
            res.put("remainingBalance", newBalance);

            return "SUCCESS " + res.toString();

        } catch (Exception e) {
            return "ERROR: Internal server failure.";
        }
    }
}