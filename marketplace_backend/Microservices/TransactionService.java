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
        if (quantity <= 0)
            return "ERROR: Invalid quantity.";

        try {
            ProductEntity product = productDao.getProductById(productId);
            if (product == null || !"IN_STOCK".equals(product.getStatus())) {
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

            // Credit seller
            AccountEntity sellerAcc = accountDao.getAccountByUserId(product.getSellerId());
            if (sellerAcc != null) {
                accountDao.updateBalance(product.getSellerId(), sellerAcc.getBalance() + totalCost);
            }

            DAOs.InventoryDAO inventoryDao = new DAOs.InventoryDAO();
            Entities.InventoryEntity inventory = inventoryDao.getInventoryByProductId(productId);
            if (inventory == null || inventory.getQuantity() < quantity) {
                accountDao.updateBalance(buyerId, buyerAcc.getBalance());
                if (sellerAcc != null)
                    accountDao.updateBalance(product.getSellerId(), sellerAcc.getBalance());
                return "ERROR: Not enough stock available.";
            }

            boolean stockReduced = inventoryDao.reduceStock(productId, quantity, inventory.getWarehouse_node());
            if (!stockReduced) {
                accountDao.updateBalance(buyerId, buyerAcc.getBalance());
                if (sellerAcc != null)
                    accountDao.updateBalance(product.getSellerId(), sellerAcc.getBalance());
                return "ERROR: Failed to reduce inventory stock.";
            }

            boolean logSuccess = transactionDao.insertTransaction(
                    buyerId, product.getSellerId(), productId, quantity, totalCost, "COMPLETED", "PURCHASE");

            if (!logSuccess) {
                accountDao.updateBalance(buyerId, buyerAcc.getBalance());
                if (sellerAcc != null)
                    accountDao.updateBalance(product.getSellerId(), sellerAcc.getBalance());
                inventoryDao.addStock(productId, quantity, inventory.getWarehouse_node());
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

    public String processDeposit(int userId, double amount) {
        if (amount <= 0)
            return "ERROR: Deposit amount must be positive.";

        try {
            AccountEntity account = accountDao.getAccountByUserId(userId);
            if (account == null) {
                return "ERROR: Account not found.";
            }

            double newBalance = account.getBalance() + amount;
            boolean success = accountDao.updateBalance(userId, newBalance);

            if (!success) {
                return "ERROR: Failed to process deposit.";
            }

            transactionDao.insertTransaction(userId, 0, 0, 0, amount, "COMPLETED", "DEPOSIT");

            JSONObject res = new JSONObject();
            res.put("message", "Deposit successful");
            res.put("depositedAmount", amount);
            res.put("newBalance", newBalance);

            return "SUCCESS " + res.toString();

        } catch (Exception e) {
            return "ERROR: Internal server failure.";
        }
    }

    public String processWithdraw(int userId, double amount) {
        if (amount <= 0)
            return "ERROR: Withdrawal amount must be positive.";

        try {
            AccountEntity account = accountDao.getAccountByUserId(userId);
            if (account == null) {
                return "ERROR: Account not found.";
            }

            if (account.getBalance() < amount) {
                return "ERROR: Insufficient funds.";
            }

            double newBalance = account.getBalance() - amount;
            boolean success = accountDao.updateBalance(userId, newBalance);

            if (!success) {
                return "ERROR: Failed to process withdrawal.";
            }

            transactionDao.insertTransaction(userId, 0, 0, 0, amount, "COMPLETED", "WITHDRAWAL");

            JSONObject res = new JSONObject();
            res.put("message", "Withdrawal successful");
            res.put("withdrawnAmount", amount);
            res.put("newBalance", newBalance);

            return "SUCCESS " + res.toString();

        } catch (Exception e) {
            return "ERROR: Internal server failure.";
        }
    }
}