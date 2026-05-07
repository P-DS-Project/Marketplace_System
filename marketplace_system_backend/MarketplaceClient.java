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

            String registerCommand = "UserSrvice REGISTER {\"username\":\"abdel\", \"email\":\"test@test.com\", \"password\":\"secure123\"}";
            System.out.println("Sending: " + registerCommand);
            out.println(registerCommand);

            String response = in.readLine();
            System.out.println("Server replied: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}