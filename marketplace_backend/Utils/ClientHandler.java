package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final RequestRouter router;

    public ClientHandler(Socket socket, RequestRouter router) {
        this.clientSocket = socket;
        this.router = router;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String inputLine;
            while ((inputLine = in.readLine()) != null) {

                String[] parts = inputLine.split(" ", 3);

                if (parts.length >= 2) {
                    String serviceDomain = parts[0];
                    String action = parts[1];
                    String jsonPayload = (parts.length == 3) ? parts[2] : "{}";

                    String response = router.route(serviceDomain, action, jsonPayload);
                    out.println(response);
                } else {
                    out.println("400 {\"error\":\"Invalid Protocol Format. Use: SERVICE ACTION JSON\"}");
                }
            }
        } catch (IOException e) {
            System.err.println("Client disconnected unexpectedly.");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }

    }
}
