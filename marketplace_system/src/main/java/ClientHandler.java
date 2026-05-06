import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final RequestRouter router; // Inject the router

    public ClientHandler(Socket socket, RequestRouter router) {
        this.clientSocket = socket;
        this.router = router;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            // Inside your ClientHandler.java run() method...

            String inputLine;
            while ((inputLine = in.readLine()) != null) {

                // Split into exactly 3 parts: [SERVICE] [ACTION] [JSON]
                // Example: "USER" "REGISTER" "{"username":"abdel"}"
                String[] parts = inputLine.split(" ", 3);

                if (parts.length >= 2) {
                    String serviceDomain = parts[0];
                    String action = parts[1];
                    String jsonPayload = (parts.length == 3) ? parts[2] : "{}";

                    // Let the Router instantly map it to the right Service Handler
                    String response = router.route(serviceDomain, action, jsonPayload);
                    out.println(response);
                } else {
                    out.println("400 {\"error\":\"Invalid Protocol Format. Use: SERVICE ACTION JSON\"}");
                }
            }
        } catch (IOException e) {
            System.err.println("Client disconnected unexpectedly.");
        }
        // ... socket closing logic ...
    }
}