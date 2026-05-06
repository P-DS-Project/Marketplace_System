import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MarketplaceServer {

    private static final int PORT = 8080;
    // Creates a pool of 100 reusable threads to handle concurrent users
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(100);

    public static void main(String[] args) {
        System.out.println("Starting Multithreaded Marketplace Server on port " + PORT + "...");

        // Initialize the Database Connections (From our previous step)
        // DatabaseConnectionManager.init(); // Ensures all 3 nodes are connected

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening for incoming TCP connections...");
            RequestRouter router = new RequestRouter(); // Create a single router instance to share

            while (true) {
                // This blocks until a client connects
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Hand the socket to a background thread and immediately go back to listening
                ClientHandler handler = new ClientHandler(clientSocket, router);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}