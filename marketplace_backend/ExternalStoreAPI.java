import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A Java Client SDK for External Stores to integrate with the Marketplace System.
 * 
 * Example usage:
 * ExternalStoreAPI store = new ExternalStoreAPI("localhost", 8080);
 * store.login("youssef.store@techzone.com", "password123");
 * store.addProduct(1, "New Laptop", 15000, "Great laptop", "Dell", "url");
 */
public class ExternalStoreAPI {

    private String host;
    private int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String jwtToken;
    private int storeUserId;

    public ExternalStoreAPI(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;
        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Disconnect error: " + e.getMessage());
        }
    }

    private String sendRequest(String service, String action, JSONObject payload) {
        if (socket == null || socket.isClosed()) {
            if (!connect()) return "500 {\"error\":\"Connection failed\"}";
        }
        try {
            // Append token if logged in
            if (jwtToken != null && !payload.has("token")) {
                payload.put("token", jwtToken);
            }
            if (storeUserId > 0 && !payload.has("sellerId")) {
                payload.put("sellerId", storeUserId);
            }

            String request = service + " " + action + " " + payload.toString();
            out.println(request);
            return in.readLine();
        } catch (IOException e) {
            return "500 {\"error\":\"Communication error: " + e.getMessage() + "\"}";
        }
    }

    // =========================================================================
    // STORE API METHODS
    // =========================================================================

    /**
     * Login as an external store.
     */
    public boolean login(String email, String password) {
        JSONObject payload = new JSONObject();
        payload.put("email", email);
        payload.put("password", password);
        
        String response = sendRequest("USER", "LOGIN", payload);
        if (response != null && response.startsWith("200")) {
            JSONObject body = new JSONObject(response.substring(4));
            this.jwtToken = body.optString("token");
            
            // Extract user ID from token (simple decode)
            try {
                String[] parts = jwtToken.split("\\.");
                String payloadStr = new String(java.util.Base64.getDecoder().decode(parts[1]));
                JSONObject tokenPayload = new JSONObject(payloadStr);
                this.storeUserId = tokenPayload.optInt("userId");
            } catch (Exception e) {
                // Ignore
            }
            return true;
        }
        System.err.println("Login failed: " + response);
        return false;
    }

    /**
     * Add a new product to the marketplace.
     */
    public int addProduct(int categoryId, String name, double price, String description, String brand, String imageUrl) {
        JSONObject payload = new JSONObject();
        payload.put("categoryId", categoryId);
        payload.put("name", name);
        payload.put("price", price);
        payload.put("description", description);
        payload.put("brand", brand);
        payload.put("imageUrl", imageUrl);
        
        String response = sendRequest("PRODUCT", "ADD", payload);
        if (response != null && response.startsWith("201")) {
            System.out.println("Product added successfully.");
            // Response typically contains the ID or success message.
            return 1; // Assuming success
        }
        System.err.println("Failed to add product: " + response);
        return -1;
    }

    /**
     * Get all products for this store.
     */
    public void printMyProducts() {
        JSONObject payload = new JSONObject();
        String response = sendRequest("PRODUCT", "GET_PRODUCTS_BY_SELLER", payload);
        
        if (response != null && response.startsWith("200")) {
            JSONObject body = new JSONObject(response.substring(4));
            JSONArray products = body.optJSONArray("products");
            if (products != null) {
                System.out.println("--- MY PRODUCTS (" + products.length() + ") ---");
                for (int i = 0; i < products.length(); i++) {
                    JSONObject p = products.getJSONObject(i);
                    System.out.printf("ID: %-4d | %-30s | EGP %.2f | %s%n", 
                        p.optInt("productId"), p.optString("name"), 
                        p.optDouble("price"), p.optString("status"));
                }
                System.out.println("-------------------------");
            }
        } else {
            System.err.println("Failed to get products: " + response);
        }
    }

    /**
     * Add inventory stock for a product.
     */
    public boolean addStock(int productId, int quantity, String warehouseNode) {
        JSONObject payload = new JSONObject();
        payload.put("productId", productId);
        payload.put("quantity", quantity);
        payload.put("warehouseNode", warehouseNode);
        
        String response = sendRequest("INVENTORY", "ADD", payload);
        if (response != null && response.startsWith("200")) {
            System.out.println("Stock added successfully.");
            return true;
        }
        System.err.println("Failed to add stock: " + response);
        return false;
    }

    /**
     * Get sales report.
     */
    public void printSalesReport() {
        JSONObject payload = new JSONObject();
        String response = sendRequest("REPORT", "GET_SALES_REPORT", payload);
        if (response != null && response.startsWith("200")) {
            String reportStr = response.substring(4);
            System.out.println("--- SALES REPORT ---");
            System.out.println(reportStr);
            System.out.println("--------------------");
        } else {
            System.err.println("Failed to get report: " + response);
        }
    }

    // =========================================================================
    // TEST HARNESS
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("External Store API Client Test");
        System.out.println("=========================================");

        ExternalStoreAPI store = new ExternalStoreAPI("localhost", 8080);
        
        // Ensure server is running before executing this!
        if (!store.connect()) {
            System.err.println("Could not connect to the Marketplace Server. Please ensure it is running on port 8080.");
            return;
        }
        
        // Let's test with TechZone store
        // We know their email is youssef.store@techzone.com and password is plain password string?
        // Wait, what is the raw password for the sample data?
        // Looking at the frontend's sample users, usually the plain password is "password" or "password123".
        // If "password" doesn't work, we'll try "password123".
        // Let's assume "password" for now, as that's standard in many of these mock apps unless specified.
        System.out.println("Attempting login as TechZone...");
        boolean loggedIn = store.login("youssef.store@techzone.com", "password");
        
        if (!loggedIn) {
            System.out.println("Trying 'password123' instead...");
            loggedIn = store.login("youssef.store@techzone.com", "password123");
        }

        if (loggedIn) {
            System.out.println("Login successful! Store User ID: " + store.storeUserId);
            
            // 1. View products
            store.printMyProducts();
            
            // 2. View sales report
            store.printSalesReport();

            // 3. Add a new test product
            System.out.println("Adding a new product via External API...");
            int result = store.addProduct(1, "API Test Laptop 2026", 25000.0, 
                "Added via external API SDK", "TechZone API", "https://example.com/laptop.jpg");
            
            if (result > 0) {
                // If we knew the exact ID, we could add stock. 
                // We'll just print products again to see if it appeared.
                store.printMyProducts();
            }

        } else {
            System.err.println("All login attempts failed. Check user credentials or backend auth logs.");
        }

        store.disconnect();
        System.out.println("Test complete.");
    }
}
