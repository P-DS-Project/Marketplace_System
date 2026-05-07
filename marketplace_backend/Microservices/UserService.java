package microservices;

import daos.UserDAO;
import daos.AccountDAO;
import daos.ProductDAO;
import daos.TransactionDAO;
import entities.UserEntity;
import entities.AccountEntity;
import entities.ProductEntity;
import entities.TransactionEntity;
import org.json.JSONArray;
import org.json.JSONObject;

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
        if (username == null || username.length() < 3) return "ERROR: Username must be at least 3 characters.";
        if (plainPassword == null || plainPassword.length() < 8) return "ERROR: Password must be at least 8 characters.";

        String salt = authService.generateCryptoSalt();
        String passwordHash = authService.hashPassword(plainPassword, salt);

        int userId = userDao.createUser(username, email, passwordHash, salt, role);

        if (userId != -1) {
            // Immediately create a linked account for the user on Node 1
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

        // 1. Fetch from Node 1 (Users & Accounts)
        UserEntity user = userDao.findById(userId);
        if (user == null) return "ERROR: User not found.";
        AccountEntity account = accountDao.getAccountByUserId(userId);

        // 2. Fetch from Node 2 (Products)
        List<ProductEntity> products = productDao.getProductsBySellerId(userId);

        // 3. Fetch from Node 3 (Transactions)
        List<TransactionEntity> transactions = transactionDao.getTransactionsByUserId(userId);

        // Aggregate Data
        JSONObject response = new JSONObject();
        
        JSONObject userData = new JSONObject();
        userData.put("userId", user.getUserId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());
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
            txArray.put(tObj);
        }
        response.put("transactions", txArray);

        return "SUCCESS: " + response.toString();
    }
}
