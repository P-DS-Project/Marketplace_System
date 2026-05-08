import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Utils.ClientHandler;
import Utils.RequestRouter;

public class MarketplaceServer {

    private static final int PORT = 8080;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(100);

    public static void main(String[] args) {
        System.out.println("Starting Marketplace Server on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening for incoming TCP connections...");
            RequestRouter router = new RequestRouter();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, router);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
