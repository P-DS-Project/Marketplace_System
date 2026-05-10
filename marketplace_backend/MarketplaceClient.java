import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MarketplaceClient {

    private static void sendRequest(PrintWriter out, BufferedReader in, String command) {
        System.out.println("Sending: " + command);
        out.println(command);
        try {
            String response = in.readLine();
            System.out.println("Server replied: " + response);
            System.out.println("------------------------------------------------");
        } catch (Exception e) {
            System.err.println("Error reading response: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 8080;

        try (Socket socket = new Socket(hostname, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to Marketplace Server.");
            System.out.println("================================================");

            // Generate unique IDs
            long uniqueId1 = System.currentTimeMillis();
            long uniqueId2 = uniqueId1 + 1;

            // 1. USER SERVICE TESTS
            System.out.println("\n[1] TESTING USER SERVICE");
            String email1 = "user1_" + uniqueId1 + "@test.com";
            String email2 = "user2_" + uniqueId2 + "@test.com";

            sendRequest(out, in, "User REGISTER {\"username\":\"TestUser1\", \"email\":\"" + email1 + "\", \"password\":\"secure123\", \"role\":\"BUYER\"}");
            sendRequest(out, in, "User REGISTER {\"username\":\"TestUser2\", \"email\":\"" + email2 + "\", \"password\":\"secure123\", \"role\":\"SELLER\"}");

            System.out.println("\nLogging in...");
            out.println("User LOGIN {\"email\":\"" + email1 + "\", \"password\":\"secure123\"}");
            String loginResponse = in.readLine();
            System.out.println("Server replied (Login 1): " + loginResponse);
            System.out.println("------------------------------------------------");
            
            // Extract token if returned
            String token = "";
            if (loginResponse != null && loginResponse.contains("\"token\":\"")) {
                token = loginResponse.split("\"token\":\"")[1].split("\"")[0];
            }

            if (!token.isEmpty()) {
                sendRequest(out, in, "User GET_INFO {\"token\":\"" + token + "\"}");
            }

            // 2. PRODUCT SERVICE TESTS
            System.out.println("\n[2] TESTING PRODUCT SERVICE");
            sendRequest(out, in, "Product ADD_PRODUCT {\"sellerId\": 2, \"categoryId\": 1, \"name\": \"Wireless Mouse\", \"price\": 25.50, \"description\": \"Bluetooth mouse\"}");
            sendRequest(out, in, "Product GET_PRODUCTS_BY_SELLER {\"sellerId\": 2}");
            sendRequest(out, in, "Product GET_PRODUCT_DETAILS {\"productId\": 1}");
            sendRequest(out, in, "Product UPDATE_PRODUCT {\"productId\": 1, \"name\": \"Gaming Mouse\", \"price\": 35.0, \"description\": \"RGB Gaming mouse\"}");
            sendRequest(out, in, "Product UPDATE_PRODUCT_STATUS {\"productId\": 1, \"status\": \"SOLD\"}");
            sendRequest(out, in, "Product REMOVE_PRODUCT {\"productId\": 1}");

            // 3. SEARCH SERVICE TESTS
            System.out.println("\n[3] TESTING SEARCH SERVICE");
            sendRequest(out, in, "Search GET_PRODUCTS_BY_CATEGORY {\"categoryId\": 1, \"limit\": 10, \"offset\": 0, \"sortBy\": \"price\", \"sortOrder\": \"ASC\"}");
            sendRequest(out, in, "Search SEARCH_BY_KEYWORD {\"keyword\": \"Mouse\"}");
            sendRequest(out, in, "Search FILTER_PRODUCTS {\"minPrice\": 10.0, \"maxPrice\": 50.0, \"brand\": \"Logitech\"}");

            // 4. CHAT SERVICE TESTS
            System.out.println("\n[4] TESTING CHAT SERVICE");
            sendRequest(out, in, "Chat START_CHAT {\"user1Id\": 1, \"user2Id\": 2}");
            sendRequest(out, in, "Chat SEND_MESSAGE {\"senderId\": 1, \"receiverId\": 2, \"content\": \"Is this mouse still available?\"}");
            sendRequest(out, in, "Chat GET_CONVERSATION {\"user1Id\": 1, \"user2Id\": 2}");
            sendRequest(out, in, "Chat LIST_USER_CHATS {\"userId\": 1}");

            // 5. TRANSACTION SERVICE TESTS
            System.out.println("\n[5] TESTING TRANSACTION SERVICE");
            sendRequest(out, in, "Transaction BUY {\"buyerId\": 1, \"productId\": 1, \"quantity\": 1}");

            // 6. INVENTORY SERVICE TESTS
            System.out.println("\n[6] TESTING INVENTORY SERVICE");
            sendRequest(out, in, "Inventory ADD {\"productId\": 2, \"quantity\": 50, \"warehouseNode\": \"Cairo_Warehouse_1\"}");
            sendRequest(out, in, "Inventory CHECK {\"productId\": 1, \"requiredQuantity\": 5}");
            sendRequest(out, in, "Inventory RETRIEVE {\"productId\": 1}");
            sendRequest(out, in, "Inventory REDUCE {\"productId\": 1, \"quantity\": 1, \"warehouseNode\": \"Cairo_Warehouse_1\"}");

            System.out.println("\nAll tests executed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
