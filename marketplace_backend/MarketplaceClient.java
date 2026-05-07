import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MarketplaceClient {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 8080;

        try (Socket socket = new Socket(hostname, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to Marketplace Server.");

            // Generate a unique ID so we always register a NEW user
            long uniqueId = System.currentTimeMillis();
            String username = "user_" + uniqueId;
            String email = "email_" + uniqueId + "@test.com";

            String registerCommand = "User REGISTER {\"username\":\"" + username + "\", \"email\":\"" + email + "\", \"password\":\"secure123\"}";
            System.out.println("Sending: " + registerCommand);
            out.println(registerCommand);

            String regResponse = in.readLine();
            System.out.println("Server replied (Register): " + regResponse);

            System.out.println("\n--- Attempting Login ---");
            // Now using email instead of username for login
            String loginCommand = "User LOGIN {\"email\":\"" + email + "\", \"password\":\"secure123\"}";
            System.out.println("Sending: " + loginCommand);
            out.println(loginCommand);

            String loginResponse = in.readLine();
            System.out.println("Server replied (Login): " + loginResponse);

            // Extract the token dynamically
            String token = "";
            if (loginResponse.contains("\"token\":\"")) {
                token = loginResponse.split("\"token\":\"")[1].split("\"")[0];
            }

            System.out.println("\n--- Attempting Get Info ---");
            String infoCommand = "User GET_INFO {\"token\":\"" + token + "\"}";
            System.out.println("Sending: " + infoCommand);
            out.println(infoCommand);

            String infoResponse = in.readLine();
            System.out.println("Server replied (Get Info): " + infoResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}