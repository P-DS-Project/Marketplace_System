package Microservices;

import DAOs.UserDAO;
import DAOs.ProductDAO;
import DAOs.TransactionDAO;
import DAOs.InventoryDAO;
import DAOs.AccountDAO;
import Entities.UserEntity;
import Entities.ProductEntity;
import Entities.TransactionEntity;
import Entities.InventoryEntity;
import Entities.AccountEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class AdminService {

    private final UserDAO userDao;
    private final ProductDAO productDao;
    private final TransactionDAO transactionDao;
    private final InventoryDAO inventoryDao;
    private final AccountDAO accountDao;
    private final AuthService authService;

    public AdminService() {
        this.userDao = new UserDAO();
        this.productDao = new ProductDAO();
        this.transactionDao = new TransactionDAO();
        this.inventoryDao = new InventoryDAO();
        this.accountDao = new AccountDAO();
        this.authService = new AuthService();
    }

    private String verifyAdmin(String token) {
        if (token == null || token.trim().isEmpty()) return "ERROR: Token is required.";
        if (!authService.isAdmin(token)) return "ERROR: Access denied. Admin privileges required.";
        return null;
    }

    public String getDashboardStats(String token) {
        String err = verifyAdmin(token);
        if (err != null) return err;

        try {
            int totalUsers = userDao.countUsers();
            int totalAdmins = userDao.countAdmins();
            int totalProducts = productDao.countProducts();
            int totalTransactions = transactionDao.countTransactions();
            double totalRevenue = transactionDao.sumCompletedAmount();

            List<TransactionEntity> recentTx = transactionDao.getAllTransactions();
            JSONArray recentArray = new JSONArray();
            int limit = Math.min(recentTx.size(), 10);
            for (int i = 0; i < limit; i++) {
                TransactionEntity t = recentTx.get(i);
                JSONObject tObj = new JSONObject();
                tObj.put("transactionId", t.getTransactionId());
                tObj.put("buyerId", t.getBuyerId());
                tObj.put("sellerId", t.getSellerId());
                tObj.put("productId", t.getProductId());
                tObj.put("amount", t.getAmount());
                tObj.put("type", t.getType());
                tObj.put("status", t.getStatus());
                tObj.put("createdAt", t.getCreated_at() != null ? t.getCreated_at() : "");
                recentArray.put(tObj);
            }

            JSONObject stats = new JSONObject();
            stats.put("totalUsers", totalUsers);
            stats.put("totalAdmins", totalAdmins);
            stats.put("totalProducts", totalProducts);
            stats.put("totalTransactions", totalTransactions);
            stats.put("totalRevenue", totalRevenue);
            stats.put("recentTransactions", recentArray);

            return "SUCCESS " + stats.toString();
        } catch (Exception e) {
            return "ERROR: Failed to get dashboard stats - " + e.getMessage();
        }
    }

    public String getAllUsers(String token) {
        String err = verifyAdmin(token);
        if (err != null) return err;

        try {
            List<UserEntity> users = userDao.getAllUsers();
            JSONArray arr = buildUsersArray(users);
            JSONObject result = new JSONObject();
            result.put("users", arr);
            result.put("total", users.size());
            return "SUCCESS " + result.toString();
        } catch (Exception e) {
            return "ERROR: Failed to get users - " + e.getMessage();
        }
    }

    public String searchUsers(String token, String query) {
        String err = verifyAdmin(token);
        if (err != null) return err;

        try {
            List<UserEntity> users = userDao.searchUsers(query);
            JSONArray arr = buildUsersArray(users);
            JSONObject result = new JSONObject();
            result.put("users", arr);
            result.put("total", users.size());
            return "SUCCESS " + result.toString();
        } catch (Exception e) {
            return "ERROR: Failed to search users - " + e.getMessage();
        }
    }

    public String promoteToAdmin(String token, int targetUserId) {
        String err = verifyAdmin(token);
        if (err != null) return err;

        UserEntity target = userDao.findById(targetUserId);
        if (target == null) return "ERROR: User not found.";
        if ("ADMIN".equals(target.getRole())) return "ERROR: User is already an admin.";

        boolean success = userDao.updateRole(targetUserId, "ADMIN");
        return success ? "SUCCESS: User promoted to admin." : "ERROR: Failed to promote user.";
    }

    public String demoteFromAdmin(String token, int targetUserId) {
        String err = verifyAdmin(token);
        if (err != null) return err;

        int callerId = authService.extractUserIdFromToken(token);
        if (callerId == targetUserId) return "ERROR: You cannot demote yourself.";

        UserEntity target = userDao.findById(targetUserId);
        if (target == null) return "ERROR: User not found.";
        if (!"ADMIN".equals(target.getRole())) return "ERROR: User is not an admin.";

        boolean success = userDao.updateRole(targetUserId, "USER");
        return success ? "SUCCESS: Admin privileges removed." : "ERROR: Failed to demote user.";
    }

    public String deleteUser(String token, int targetUserId) {
        String err = verifyAdmin(token);
        if (err != null) return err;

        int callerId = authService.extractUserIdFromToken(token);
        if (callerId == targetUserId) return "ERROR: You cannot delete your own account from admin panel.";

        UserEntity target = userDao.findById(targetUserId);
        if (target == null) return "ERROR: User not found.";

        boolean success = userDao.deleteUser(targetUserId);
        return success ? "SUCCESS: User deleted." : "ERROR: Failed to delete user.";
    }

    public String toggleUserActive(String token, int targetUserId, boolean active) {
        String err = verifyAdmin(token);
        if (err != null) return err;

        int callerId = authService.extractUserIdFromToken(token);
        if (callerId == targetUserId) return "ERROR: You cannot disable your own account.";

        UserEntity target = userDao.findById(targetUserId);
        if (target == null) return "ERROR: User not found.";

        boolean success = userDao.setActive(targetUserId, active);
        String action = active ? "enabled" : "disabled";
        return success ? "SUCCESS: User account " + action + "." : "ERROR: Failed to update user status.";
    }

    public String getAllProducts(String token) {
        String err = verifyAdmin(token);
        if (err != null) return err;

        try {
            List<ProductEntity> products = productDao.advancedSearch(
                    null, null, null, null, null, null, null, null, "created_at", "DESC", 10000, 0);

            JSONArray arr = new JSONArray();
            for (ProductEntity p : products) {
                JSONObject obj = new JSONObject();
                obj.put("productId", p.getProductId());
                obj.put("sellerId", p.getSellerId());
                obj.put("categoryId", p.getCategoryId());
                obj.put("name", p.getName());
                obj.put("brand", p.getBrand() != null ? p.getBrand() : "");
                obj.put("price", p.getPrice());
                obj.put("status", p.getStatus());
                obj.put("description", p.getDescription() != null ? p.getDescription() : "");
                obj.put("imageUrl", p.getImageUrl() != null ? p.getImageUrl() : "");
                if (p.getCreatedAt() != null) obj.put("createdAt", p.getCreatedAt().toString());

                InventoryEntity inv = inventoryDao.getInventoryByProductId(p.getProductId());
                obj.put("stock", inv != null ? inv.getQuantity() : 0);
                obj.put("warehouseNode", inv != null ? inv.getWarehouse_node() : "N/A");

                arr.put(obj);
            }

            JSONObject result = new JSONObject();
            result.put("products", arr);
            result.put("total", arr.length());
            return "SUCCESS " + result.toString();
        } catch (Exception e) {
            return "ERROR: Failed to get products - " + e.getMessage();
        }
    }

    public String adminDeleteProduct(String token, int productId) {
        String err = verifyAdmin(token);
        if (err != null) return err;

        boolean success = productDao.deleteProduct(productId);
        return success ? "SUCCESS: Product deleted." : "ERROR: Failed to delete product.";
    }

    public String adminUpdateProduct(String token, int productId, String name, double price, String description, String brand, String imageUrl, String status) {
        String err = verifyAdmin(token);
        if (err != null) return err;

        if (status != null && !status.isEmpty()) {
            productDao.updateProductStatus(productId, status);
        }

        boolean success = productDao.updateProduct(productId, name, price, description, brand, imageUrl);
        return success ? "SUCCESS: Product updated." : "ERROR: Failed to update product.";
    }

    public String getAllTransactions(String token) {
        String err = verifyAdmin(token);
        if (err != null) return err;

        try {
            List<TransactionEntity> transactions = transactionDao.getAllTransactions();

            int completed = 0, pending = 0, failed = 0, refunded = 0;
            double totalRevenue = 0;

            JSONArray arr = new JSONArray();
            for (TransactionEntity t : transactions) {
                JSONObject tObj = new JSONObject();
                tObj.put("transactionId", t.getTransactionId());
                tObj.put("buyerId", t.getBuyerId());
                tObj.put("sellerId", t.getSellerId());
                tObj.put("productId", t.getProductId());
                tObj.put("quantity", t.getQuantity());
                tObj.put("amount", t.getAmount());
                tObj.put("type", t.getType());
                tObj.put("status", t.getStatus());
                tObj.put("createdAt", t.getCreated_at() != null ? t.getCreated_at() : "");
                tObj.put("completedAt", t.getCompleted_at() != null ? t.getCompleted_at() : "");
                arr.put(tObj);

                if ("COMPLETED".equals(t.getStatus())) { completed++; if ("PURCHASE".equals(t.getType())) totalRevenue += t.getAmount(); }
                else if ("PENDING".equals(t.getStatus())) pending++;
                else if ("FAILED".equals(t.getStatus())) failed++;
                else if ("REFUNDED".equals(t.getStatus())) refunded++;
            }

            JSONObject result = new JSONObject();
            result.put("transactions", arr);
            result.put("total", transactions.size());
            result.put("completed", completed);
            result.put("pending", pending);
            result.put("failed", failed);
            result.put("refunded", refunded);
            result.put("totalRevenue", totalRevenue);

            return "SUCCESS " + result.toString();
        } catch (Exception e) {
            return "ERROR: Failed to get transactions - " + e.getMessage();
        }
    }

    public String getUserDetails(String token, int userId) {
        String err = verifyAdmin(token);
        if (err != null) return err;

        UserEntity user = userDao.findById(userId);
        if (user == null) return "ERROR: User not found.";

        AccountEntity account = accountDao.getAccountByUserId(userId);

        JSONObject result = new JSONObject();
        JSONObject uObj = userToJson(user);
        if (account != null) {
            uObj.put("balance", account.getBalance());
            uObj.put("currency", account.getCurrency());
        }
        result.put("user", uObj);

        return "SUCCESS " + result.toString();
    }

    private JSONArray buildUsersArray(List<UserEntity> users) {
        JSONArray arr = new JSONArray();
        for (UserEntity u : users) {
            arr.put(userToJson(u));
        }
        return arr;
    }

    private JSONObject userToJson(UserEntity u) {
        JSONObject obj = new JSONObject();
        obj.put("userId", u.getUserId());
        obj.put("username", u.getUsername());
        obj.put("email", u.getEmail());
        obj.put("role", u.getRole());
        obj.put("isActive", u.isActive());
        obj.put("isVerified", u.isVerified());
        obj.put("avatarUrl", u.getAvatarUrl() != null ? u.getAvatarUrl() : "");
        obj.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt() : "");
        return obj;
    }
}
