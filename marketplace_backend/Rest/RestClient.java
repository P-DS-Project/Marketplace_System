package Rest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * RestClient — REST API test client.
 * 
 * Connects to the REST server on http://localhost:9090 and runs a suite of
 * test cases against all 6 endpoints. Each test validates the HTTP status
 * code and response content.
 * 
 * Usage:
 *   1. Start the REST server first:  run.bat Rest.RestServer
 *   2. Run this client:              run.bat Rest.RestClient
 * 
 * Test cases cover:
 *   - User registration (success + duplicate)
 *   - User login (success + wrong password)
 *   - Product creation (success + invalid data)
 *   - Product retrieval by ID
 *   - Product retrieval by seller
 *   - Search by keyword (success + missing keyword)
 *   - Method not allowed (GET on POST-only endpoint)
 */
public class RestClient {

    private static final String BASE_URL = "http://localhost:9090";
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    private static int lastStatusCode = 0;

    // ==================== HTTP HELPER METHODS ====================

    /**
     * Sends a GET request to the specified endpoint.
     */
    private static String sendGet(String endpoint) {
        return sendRequest("GET", endpoint, null);
    }

    /**
     * Sends a POST request with a JSON body to the specified endpoint.
     */
    private static String sendPost(String endpoint, String jsonBody) {
        return sendRequest("POST", endpoint, jsonBody);
    }

    /**
     * Generic HTTP request sender. Handles GET and POST methods.
     * Stores the response status code in lastStatusCode for assertion.
     */
    private static String sendRequest(String method, String endpoint, String jsonBody) {
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            // Write request body for POST
            if (jsonBody != null && "POST".equals(method)) {
                conn.setDoOutput(true);
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                OutputStream os = conn.getOutputStream();
                os.write(input);
                os.close();
            }

            lastStatusCode = conn.getResponseCode();

            // Read response from the appropriate stream
            InputStream is = (lastStatusCode >= 200 && lastStatusCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            if (is == null) {
                conn.disconnect();
                return "";
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();

            return response.toString();

        } catch (Exception e) {
            lastStatusCode = -1;
            return "CONNECTION ERROR: " + e.getMessage();
        }
    }

    // ==================== TEST ASSERTION HELPERS ====================

    /**
     * Asserts that the HTTP status code matches the expected value.
     */
    private static void assertStatus(String testName, int expected, int actual) {
        if (expected == actual) {
            System.out.println("    PASS - " + testName + " (HTTP " + actual + ")");
            testsPassed++;
        } else {
            System.out.println("    FAIL - " + testName + " (Expected HTTP " + expected + ", got " + actual + ")");
            testsFailed++;
        }
    }

    /**
     * Asserts that the response body contains the expected substring.
     */
    private static void assertContains(String testName, String response, String expected) {
        if (response != null && response.contains(expected)) {
            System.out.println("    PASS - " + testName);
            testsPassed++;
        } else {
            System.out.println("    FAIL - " + testName + " (Response does not contain: '" + expected + "')");
            testsFailed++;
        }
    }

    // ==================== TEST CASES ====================

    public static void main(String[] args) {
        System.out.println("================================================");
        System.out.println("  Marketplace REST Client - Test Suite");
        System.out.println("  Target: " + BASE_URL);
        System.out.println("================================================");
        System.out.println();

        // Generate a unique ID for this test run to avoid conflicts
        long uniqueId = System.currentTimeMillis();
        String testEmail = "resttest_" + uniqueId + "@test.com";
        String testUsername = "RestUser_" + uniqueId;

        // ======================================================
        // TEST 1: User Registration (Success)
        // ======================================================
        System.out.println("[TEST 1] POST /api/users/register - Register a new user");
        String registerBody = "{"
                + "\"username\": \"" + testUsername + "\","
                + "\"email\": \"" + testEmail + "\","
                + "\"password\": \"securePass123\","
                + "\"role\": \"USER\""
                + "}";
        System.out.println("  Body: " + registerBody);
        String registerResponse = sendPost("/api/users/register", registerBody);
        System.out.println("  Response [" + lastStatusCode + "]: " + registerResponse);
        assertStatus("Registration returns 201 Created", 201, lastStatusCode);
        assertContains("Response contains success message", registerResponse, "User registered");
        System.out.println();

        // ======================================================
        // TEST 2: Duplicate Registration (Should Fail)
        // ======================================================
        System.out.println("[TEST 2] POST /api/users/register - Duplicate user registration");
        System.out.println("  Body: (same as Test 1)");
        String dupResponse = sendPost("/api/users/register", registerBody);
        System.out.println("  Response [" + lastStatusCode + "]: " + dupResponse);
        assertStatus("Duplicate registration returns 400 Bad Request", 400, lastStatusCode);
        assertContains("Error mentions 'already exists'", dupResponse, "already exists");
        System.out.println();

        // ======================================================
        // TEST 3: User Login (Success)
        // ======================================================
        System.out.println("[TEST 3] POST /api/users/login - Login with valid credentials");
        String loginBody = "{"
                + "\"email\": \"" + testEmail + "\","
                + "\"password\": \"securePass123\""
                + "}";
        System.out.println("  Body: " + loginBody);
        String loginResponse = sendPost("/api/users/login", loginBody);
        System.out.println("  Response [" + lastStatusCode + "]: " + loginResponse);
        assertStatus("Login returns 200 OK", 200, lastStatusCode);
        assertContains("Response contains token", loginResponse, "token");
        assertContains("Response contains role", loginResponse, "role");
        System.out.println();

        // ======================================================
        // TEST 4: Login with Wrong Password (Should Fail)
        // ======================================================
        System.out.println("[TEST 4] POST /api/users/login - Login with wrong password");
        String badLoginBody = "{"
                + "\"email\": \"" + testEmail + "\","
                + "\"password\": \"wrongPassword99\""
                + "}";
        System.out.println("  Body: " + badLoginBody);
        String badLoginResponse = sendPost("/api/users/login", badLoginBody);
        System.out.println("  Response [" + lastStatusCode + "]: " + badLoginResponse);
        assertStatus("Wrong password returns 401 Unauthorized", 401, lastStatusCode);
        assertContains("Error mentions invalid credentials", badLoginResponse, "Invalid credentials");
        System.out.println();

        // ======================================================
        // TEST 5: Add Product (Success)
        // ======================================================
        System.out.println("[TEST 5] POST /api/products - Add a new product");
        String productBody = "{"
                + "\"sellerId\": 2,"
                + "\"categoryId\": 1,"
                + "\"name\": \"REST Test Product\","
                + "\"price\": 49.99,"
                + "\"description\": \"A product added via REST API\""
                + "}";
        System.out.println("  Body: " + productBody);
        String addProductResponse = sendPost("/api/products", productBody);
        System.out.println("  Response [" + lastStatusCode + "]: " + addProductResponse);
        assertStatus("Add product returns 201 Created", 201, lastStatusCode);
        assertContains("Response contains productId", addProductResponse, "productId");
        System.out.println();

        // ======================================================
        // TEST 6: Add Product with Invalid Data (Should Fail)
        // ======================================================
        System.out.println("[TEST 6] POST /api/products - Product with name too short");
        String badProductBody = "{"
                + "\"sellerId\": 2,"
                + "\"categoryId\": 1,"
                + "\"name\": \"AB\","
                + "\"price\": 10.0,"
                + "\"description\": \"Invalid product\""
                + "}";
        System.out.println("  Body: " + badProductBody);
        String badProductResponse = sendPost("/api/products", badProductBody);
        System.out.println("  Response [" + lastStatusCode + "]: " + badProductResponse);
        assertStatus("Invalid product returns 400 Bad Request", 400, lastStatusCode);
        assertContains("Error mentions name length", badProductResponse, "at least 3 characters");
        System.out.println();

        // ======================================================
        // TEST 7: Get Product Details (GET by ID)
        // ======================================================
        System.out.println("[TEST 7] GET /api/products/1 - Get product details by ID");
        String productDetailsResponse = sendGet("/api/products/1");
        System.out.println("  Response [" + lastStatusCode + "]: " + productDetailsResponse);
        if (lastStatusCode == 200) {
            assertStatus("Get product returns 200 OK", 200, lastStatusCode);
            assertContains("Response contains product name", productDetailsResponse, "name");
        } else {
            // Product may not exist in database — still a valid test
            assertStatus("Product not found returns 404", 404, lastStatusCode);
        }
        System.out.println();

        // ======================================================
        // TEST 8: Get Products by Seller
        // ======================================================
        System.out.println("[TEST 8] GET /api/products/seller/2 - Get all products by seller ID 2");
        String sellerProductsResponse = sendGet("/api/products/seller/2");
        System.out.println("  Response [" + lastStatusCode + "]: " + sellerProductsResponse);
        assertStatus("Get seller products returns 200 OK", 200, lastStatusCode);
        assertContains("Response contains products array", sellerProductsResponse, "products");
        System.out.println();

        // ======================================================
        // TEST 9: Search Products by Keyword
        // ======================================================
        System.out.println("[TEST 9] GET /api/search?keyword=Product - Search by keyword");
        String searchResponse = sendGet("/api/search?keyword=Product");
        System.out.println("  Response [" + lastStatusCode + "]: " + searchResponse);
        assertStatus("Search returns 200 OK", 200, lastStatusCode);
        assertContains("Response contains results array", searchResponse, "results");
        assertContains("Response contains count", searchResponse, "count");
        System.out.println();

        // ======================================================
        // TEST 10: Search with No Keyword (Should Fail)
        // ======================================================
        System.out.println("[TEST 10] GET /api/search - Search without keyword");
        String emptySearchResponse = sendGet("/api/search");
        System.out.println("  Response [" + lastStatusCode + "]: " + emptySearchResponse);
        assertStatus("Empty search returns 400 Bad Request", 400, lastStatusCode);
        assertContains("Error mentions keyword required", emptySearchResponse, "keyword");
        System.out.println();

        // ======================================================
        // TEST 11: Method Not Allowed
        // ======================================================
        System.out.println("[TEST 11] GET /api/users/register - Wrong HTTP method");
        String wrongMethodResponse = sendGet("/api/users/register");
        System.out.println("  Response [" + lastStatusCode + "]: " + wrongMethodResponse);
        assertStatus("Wrong method returns 405 Method Not Allowed", 405, lastStatusCode);
        System.out.println();

        // ==================== SUMMARY ====================
        int totalTests = testsPassed + testsFailed;
        System.out.println("================================================");
        System.out.println("  TEST RESULTS SUMMARY");
        System.out.println("================================================");
        System.out.println("  Passed : " + testsPassed + " / " + totalTests);
        System.out.println("  Failed : " + testsFailed + " / " + totalTests);
        System.out.println("================================================");
        if (testsFailed == 0) {
            System.out.println("  ALL TESTS PASSED!");
        } else {
            System.out.println("  SOME TESTS FAILED - review output above.");
        }
        System.out.println("================================================");
    }
}
