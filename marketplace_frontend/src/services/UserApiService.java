package services;

import models.User;
import models.Account;
import models.Product;
import models.Transaction;
import network.SocketClient;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class UserApiService {

    private final SocketClient client = SocketClient.getInstance();

    public String register(String username, String email, String password, String role) {
        JSONObject payload = new JSONObject();
        payload.put("username", username);
        payload.put("email", email);
        payload.put("password", password);
        payload.put("role", role);

        String response = client.sendRequest("USER", "REGISTER", payload);
        int code = SocketClient.getStatusCode(response);
        JSONObject body = SocketClient.getResponseBody(response);

        if (code == 200 || code == 201) {
            return body.optString("message", "Registration successful");
        } else {
            return "ERROR: " + body.optString("error", "Registration failed");
        }
    }

    public String login(String email, String password) {
        JSONObject payload = new JSONObject();
        payload.put("email", email);
        payload.put("password", password);

        String response = client.sendRequest("USER", "LOGIN", payload);
        int code = SocketClient.getStatusCode(response);
        JSONObject body = SocketClient.getResponseBody(response);

        if (code == 200) {
            return body.optString("token", null);
        } else {
            return null;
        }
    }

    /**
     * Returns a JSONObject with user, account, products, transactions
     */
    public JSONObject getInfo(String token) {
        JSONObject payload = new JSONObject();
        payload.put("token", token);

        String response = client.sendRequest("USER", "GET_INFO", payload);
        int code = SocketClient.getStatusCode(response);

        if (code == 200) {
            return SocketClient.getResponseBody(response);
        }
        return null;
    }

    public User parseUser(JSONObject infoData) {
        if (infoData == null || !infoData.has("user")) return null;
        JSONObject u = infoData.getJSONObject("user");
        User user = new User();
        user.setUserId(u.optInt("userId", -1));
        user.setUsername(u.optString("username", ""));
        user.setEmail(u.optString("email", ""));
        user.setRole(u.optString("role", "buyer"));
        return user;
    }

    public Account parseAccount(JSONObject infoData) {
        if (infoData == null || !infoData.has("account")) return null;
        JSONObject a = infoData.getJSONObject("account");
        Account acc = new Account();
        acc.setAccountId(a.optInt("accountId", 0));
        acc.setBalance(a.optDouble("balance", 0.0));
        acc.setCurrency(a.optString("currency", "EGP"));
        return acc;
    }

    public List<Product> parseProducts(JSONObject infoData) {
        List<Product> list = new ArrayList<>();
        if (infoData == null || !infoData.has("products")) return list;
        JSONArray arr = infoData.getJSONArray("products");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject p = arr.getJSONObject(i);
            Product prod = new Product();
            prod.setProductId(p.optInt("productId"));
            prod.setName(p.optString("name", ""));
            prod.setPrice(p.optDouble("price", 0));
            prod.setStatus(p.optString("status", ""));
            list.add(prod);
        }
        return list;
    }

    public List<Transaction> parseTransactions(JSONObject infoData) {
        List<Transaction> list = new ArrayList<>();
        if (infoData == null || !infoData.has("transactions")) return list;
        JSONArray arr = infoData.getJSONArray("transactions");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject t = arr.getJSONObject(i);
            Transaction tx = new Transaction();
            tx.setTransactionId(t.optInt("transactionId"));
            tx.setAmount(t.optDouble("amount", 0));
            tx.setType(t.optString("type", ""));
            tx.setStatus(t.optString("status", ""));
            list.add(tx);
        }
        return list;
    }
}
