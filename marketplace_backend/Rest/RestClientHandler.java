package Rest;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import Microservices.ProductService;
import Microservices.UserService;
import Microservices.SearchService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * RestClientHandler — the core REST request router.
 * 
 * Implements HttpHandler to process incoming HTTP requests and route them
 * to the appropriate microservice. This is the REST equivalent of the
 * existing Utils/ClientHandler + Utils/RequestRouter combination.
 * 
 * Supported endpoints:
 *   GET    /api/products/{id}            → ProductService.getProductDetails()
 *   GET    /api/products/seller/{id}     → ProductService.getProductsBySeller()
 *   POST   /api/products                 → ProductService.addProduct()
 *   POST   /api/users/register           → UserService.registerUser()
 *   POST   /api/users/login              → UserService.login()
 *   GET    /api/search?keyword=...       → SearchService.searchByKeyword()
 */
public class RestClientHandler implements HttpHandler {

    private final ProductService productService;
    private final UserService userService;
    private final SearchService searchService;

    public RestClientHandler() {
        this.productService = new ProductService();
        this.userService = new UserService();
        this.searchService = new SearchService();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set common response headers
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        System.out.println("[REST] " + method + " " + path + (query != null ? "?" + query : ""));

        try {
            // Route based on path prefix
            if (path.startsWith("/api/users")) {
                handleUsers(exchange, method, path);
            } else if (path.startsWith("/api/products")) {
                handleProducts(exchange, method, path);
            } else if (path.startsWith("/api/search")) {
                handleSearch(exchange, method, query);
            } else {
                sendResponse(exchange, 404, errorJson("Endpoint not found: " + path));
            }
        } catch (Exception e) {
            System.err.println("[REST ERROR] " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, errorJson("Internal server error: " + e.getMessage()));
        }
    }

    // ==================== PRODUCT ENDPOINTS ====================

    /**
     * Handles all /api/products/* requests.
     * 
     * GET  /api/products/{id}           → get single product details
     * GET  /api/products/seller/{id}    → get all products by a seller
     * POST /api/products                → add a new product
     */
    private void handleProducts(HttpExchange exchange, String method, String path) throws IOException {

        if ("GET".equals(method)) {

            if (path.matches("/api/products/seller/\\d+")) {
                // GET /api/products/seller/{sellerId}
                int sellerId = extractPathId(path, "/api/products/seller/");
                String result = productService.getProductsBySeller(sellerId);

                JSONObject response = new JSONObject();
                response.put("products", new JSONArray(result));
                sendResponse(exchange, 200, response.toString());

            } else if (path.matches("/api/products/\\d+")) {
                // GET /api/products/{productId}
                int productId = extractPathId(path, "/api/products/");
                String result = productService.getProductDetails(productId);

                if (result.startsWith("ERROR")) {
                    sendResponse(exchange, 404, errorJson(result.replace("ERROR: ", "")));
                } else {
                    sendResponse(exchange, 200, result);
                }

            } else {
                sendResponse(exchange, 400, errorJson("Invalid product endpoint. Use /api/products/{id} or /api/products/seller/{sellerId}"));
            }

        } else if ("POST".equals(method) && path.equals("/api/products")) {
            // POST /api/products — add a new product
            String body = readRequestBody(exchange);
            JSONObject json = new JSONObject(body);

            int sellerId = json.getInt("sellerId");
            int categoryId = json.getInt("categoryId");
            String name = json.getString("name");
            double price = json.getDouble("price");
            String description = json.optString("description", "");
            String brand = json.optString("brand", null);
            String imageUrl = json.optString("imageUrl", null);

            String result = productService.addProduct(sellerId, categoryId, name, price, description, brand, imageUrl);

            if (result.startsWith("SUCCESS")) {
                String jsonPart = result.substring("SUCCESS ".length());
                sendResponse(exchange, 201, jsonPart);
            } else {
                sendResponse(exchange, 400, errorJson(result.replace("ERROR: ", "")));
            }

        } else {
            sendResponse(exchange, 405, errorJson("Method not allowed: " + method));
        }
    }

    // ==================== USER ENDPOINTS ====================

    /**
     * Handles all /api/users/* requests.
     * 
     * POST /api/users/register   → register a new user
     * POST /api/users/login      → login and receive a JWT token
     */
    private void handleUsers(HttpExchange exchange, String method, String path) throws IOException {

        if (!"POST".equals(method)) {
            sendResponse(exchange, 405, errorJson("Method not allowed: " + method + ". User endpoints only accept POST."));
            return;
        }

        String body = readRequestBody(exchange);
        JSONObject json = new JSONObject(body);

        if (path.equals("/api/users/register")) {
            // POST /api/users/register
            String username = json.getString("username");
            String email = json.getString("email");
            String password = json.getString("password");
            String role = json.optString("role", "USER");

            String result = userService.registerUser(username, email, password, role);

            if (result.startsWith("SUCCESS")) {
                JSONObject response = new JSONObject();
                response.put("message", result.replace("SUCCESS: ", ""));
                sendResponse(exchange, 201, response.toString());
            } else {
                sendResponse(exchange, 400, errorJson(result.replace("ERROR: ", "")));
            }

        } else if (path.equals("/api/users/login")) {
            // POST /api/users/login
            String email = json.getString("email");
            String password = json.getString("password");

            String result = userService.login(email, password);

            if (result.startsWith("SUCCESS:")) {
                // Service returns format: "SUCCESS:<ROLE> <jwt_token>"
                String afterSuccess = result.substring("SUCCESS:".length());
                String[] parts = afterSuccess.split(" ", 2);
                String role = parts[0];
                String token = parts.length > 1 ? parts[1] : "";

                JSONObject response = new JSONObject();
                response.put("token", token);
                response.put("role", role);
                sendResponse(exchange, 200, response.toString());
            } else {
                sendResponse(exchange, 401, errorJson(result.replace("ERROR: ", "")));
            }

        } else {
            sendResponse(exchange, 404, errorJson("User endpoint not found: " + path));
        }
    }

    // ==================== SEARCH ENDPOINTS ====================

    /**
     * Handles /api/search requests.
     * 
     * GET /api/search?keyword=...&sortBy=...&sortOrder=...&limit=...&offset=...
     *     → search products by keyword using SearchService
     */
    private void handleSearch(HttpExchange exchange, String method, String query) throws IOException {

        if (!"GET".equals(method)) {
            sendResponse(exchange, 405, errorJson("Method not allowed: " + method + ". Search only accepts GET."));
            return;
        }

        Map<String, String> params = parseQueryParams(query);
        String keyword = params.getOrDefault("keyword", "");
        String sortBy = params.getOrDefault("sortBy", "created_at");
        String sortOrder = params.getOrDefault("sortOrder", "DESC");
        int limit = Integer.parseInt(params.getOrDefault("limit", "20"));
        int offset = Integer.parseInt(params.getOrDefault("offset", "0"));

        String result = searchService.searchByKeyword(keyword, null, sortBy, sortOrder, limit, offset);

        if (result.startsWith("SUCCESS")) {
            String jsonPart = result.substring("SUCCESS ".length());
            sendResponse(exchange, 200, jsonPart);
        } else {
            sendResponse(exchange, 400, errorJson(result.replace("ERROR: ", "")));
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Extracts a numeric ID from the end of a path segment.
     * Example: extractPathId("/api/products/42", "/api/products/") → 42
     */
    private int extractPathId(String path, String prefix) {
        String idStr = path.substring(prefix.length());
        return Integer.parseInt(idStr);
    }

    /**
     * Reads the full request body from the HTTP exchange as a string.
     */
    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    /**
     * Parses URL query parameters into a key-value map.
     * Example: "keyword=Mouse&limit=10" → {keyword=Mouse, limit=10}
     */
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }

    /**
     * Sends an HTTP response with the given status code and JSON body.
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    /**
     * Creates a JSON error response string: {"error": "message"}
     */
    private String errorJson(String message) {
        return new JSONObject().put("error", message).toString();
    }
}
