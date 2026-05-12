package Microservices;

import Entities.UserEntity;
import Entities.AccountEntity;
import Entities.ProductEntity;
import Entities.TransactionEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import DAOs.AccountDAO;
import DAOs.ProductDAO;
import DAOs.TransactionDAO;
import DAOs.UserDAO;

import java.util.List;

public class UserService {

    private final UserDAO userDao;
    private final AccountDAO accountDao;
    private final ProductDAO productDao;
    private final TransactionDAO transactionDao;
    private final AuthService authService;

    public UserService() {
        this.userDao = new UserDAO();
        this.accountDao = new AccountDAO();
        this.productDao = new ProductDAO();
        this.transactionDao = new TransactionDAO();
        this.authService = new AuthService();
    }

    public String registerUser(String username, String email, String plainPassword, String role) {
        if (username == null || username.length() < 3)
            return "ERROR: Username must be at least 3 characters.";
        if (plainPassword == null || plainPassword.length() < 8)
            return "ERROR: Password must be at least 8 characters.";

        if (role == null || role.trim().isEmpty()) {
            role = "USER";
        }
        role = role.toUpperCase().replace(" ", "_");
        if (!"USER".equals(role) && !"EXTERNAL_STORE".equals(role) && !"ADMIN".equals(role)) {
            role = "USER";
        }

        String salt = authService.generateCryptoSalt();
        String passwordHash = authService.hashPassword(plainPassword, salt);

        int userId = userDao.createUser(username, email, passwordHash, salt, role);

        if (userId != -1) {
            accountDao.createAccount(userId);
            return "SUCCESS: User registered.";
        } else {
            return "ERROR: Username or Email already exists.";
        }
    }

    public String login(String email, String plainPassword) {
        UserEntity user = userDao.findByEmail(email);

        if (user == null) {
            return "ERROR: Invalid credentials.";
        }

        String expectedHash = authService.hashPassword(plainPassword, user.getSalt());

        if (expectedHash.equals(user.getPasswordHash())) {
            String jwt = authService.generateJWT(user.getUserId(), user.getRole());
            return "SUCCESS: " + jwt;
        } else {
            return "ERROR: Invalid credentials.";
        }
    }

    public String getAccountInfo(String token) {
        if (token == null || token.trim().isEmpty()) {
            return "ERROR: Token is required.";
        }

        int userId = authService.extractUserIdFromToken(token);
        if (userId == -1) {
            return "ERROR: Invalid or expired token.";
        }

        UserEntity user = userDao.findById(userId);
        if (user == null)
            return "ERROR: User not found.";
        AccountEntity account = accountDao.getAccountByUserId(userId);

        List<ProductEntity> products = productDao.getProductsBySellerId(userId);

        List<TransactionEntity> transactions = transactionDao.getTransactionsByUserId(userId);

        JSONObject response = new JSONObject();

        JSONObject userData = new JSONObject();
        userData.put("userId", user.getUserId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());
        userData.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        response.put("user", userData);

        if (account != null) {
            JSONObject accData = new JSONObject();
            accData.put("accountId", account.getAccountId());
            accData.put("balance", account.getBalance());
            accData.put("currency", account.getCurrency());
            response.put("account", accData);
        }

        JSONArray prodArray = new JSONArray();
        for (ProductEntity p : products) {
            JSONObject pObj = new JSONObject();
            pObj.put("productId", p.getProductId());
            pObj.put("name", p.getName());
            pObj.put("price", p.getPrice());
            pObj.put("status", p.getStatus());
            pObj.put("imageUrl", p.getImageUrl() != null ? p.getImageUrl() : "");
            prodArray.put(pObj);
        }
        response.put("products", prodArray);

        JSONArray txArray = new JSONArray();
        for (TransactionEntity t : transactions) {
            JSONObject tObj = new JSONObject();
            tObj.put("transactionId", t.getTransactionId());
            tObj.put("amount", t.getAmount());
            tObj.put("type", t.getType());
            tObj.put("status", t.getStatus());
            tObj.put("createdAt", t.getCreated_at() != null ? t.getCreated_at() : "");
            txArray.put(tObj);
        }
        response.put("transactions", txArray);

        return "SUCCESS: " + response.toString();
    }

    public String updateProfile(String token, String username, String email, String avatarUrl) {
        int userId = authService.extractUserIdFromToken(token);
        if (userId == -1) return "ERROR: Invalid token.";

        if (username == null || username.length() < 3) return "ERROR: Username must be at least 3 characters.";

        boolean success = userDao.updateProfile(userId, username, email, avatarUrl);
        return success ? "SUCCESS: Profile updated." : "ERROR: Failed to update profile.";
    }

    public String changePassword(String token, String oldPassword, String newPassword) {
        int userId = authService.extractUserIdFromToken(token);
        if (userId == -1) return "ERROR: Invalid token.";

        UserEntity user = userDao.findById(userId);
        if (user == null) return "ERROR: User not found.";

        String expectedHash = authService.hashPassword(oldPassword, user.getSalt());
        if (!expectedHash.equals(user.getPasswordHash())) {
            return "ERROR: Current password is incorrect.";
        }

        if (newPassword == null || newPassword.length() < 8) {
            return "ERROR: New password must be at least 8 characters.";
        }

        String newSalt = authService.generateCryptoSalt();
        String newHash = authService.hashPassword(newPassword, newSalt);
        boolean success = userDao.updatePassword(userId, newHash, newSalt);
        return success ? "SUCCESS: Password changed." : "ERROR: Failed to change password.";
    }

    public String deleteAccount(String token) {
        int userId = authService.extractUserIdFromToken(token);
        if (userId == -1) return "ERROR: Invalid token.";

        boolean success = userDao.deleteUser(userId);
        return success ? "SUCCESS: Account deleted." : "ERROR: Failed to delete account.";
    }

    public String getUsernameById(int userId) {
        return userDao.getUsernameById(userId);
    }
}
