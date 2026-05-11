package network;

import org.json.JSONObject;
import java.io.*;
import java.net.Socket;

public class SocketClient {

    private static SocketClient instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String host;
    private int port;
    private boolean connected = false;

    private final Object lock = new Object();

    private SocketClient() {}

    public static synchronized SocketClient getInstance() {
        if (instance == null) {
            instance = new SocketClient();
        }
        return instance;
    }

    public boolean connect(String host, int port) {
        this.host = host;
        this.port = port;
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
            System.out.println("[SocketClient] Connected to " + host + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("[SocketClient] Connection failed: " + e.getMessage());
            connected = false;
            return false;
        }
    }

    public void disconnect() {
        try {
            connected = false;
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("[SocketClient] Disconnected.");
        } catch (IOException e) {
            System.err.println("[SocketClient] Error disconnecting: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    private boolean reconnect() {
        System.out.println("[SocketClient] Attempting reconnect...");
        disconnect();
        return connect(host, port);
    }

    /**
     * Sends a request in the format: SERVICE ACTION {json}\n
     * Returns the raw response string (e.g., "200 {...}")
     */
    public String sendRequest(String service, String action, JSONObject payload) {
        synchronized (lock) {
            if (!isConnected()) {
                if (!reconnect()) {
                    return "500 {\"error\":\"Not connected to server\"}";
                }
            }

            try {
                String request = service + " " + action + " " + (payload != null ? payload.toString() : "{}");
                out.println(request);
                String response = in.readLine();
                if (response == null) {
                    // Server closed connection, try reconnect once
                    if (reconnect()) {
                        out.println(request);
                        response = in.readLine();
                    }
                    if (response == null) {
                        return "500 {\"error\":\"Server connection lost\"}";
                    }
                }
                return response;
            } catch (IOException e) {
                System.err.println("[SocketClient] Request failed: " + e.getMessage());
                // Try reconnect
                if (reconnect()) {
                    try {
                        String request = service + " " + action + " " + (payload != null ? payload.toString() : "{}");
                        out.println(request);
                        String response = in.readLine();
                        return response != null ? response : "500 {\"error\":\"Server connection lost after retry\"}";
                    } catch (IOException ex) {
                        return "500 {\"error\":\"Reconnect failed: " + ex.getMessage() + "\"}";
                    }
                }
                return "500 {\"error\":\"Connection error: " + e.getMessage() + "\"}";
            }
        }
    }

    /**
     * Parses the status code from a response like "200 {...}"
     */
    public static int getStatusCode(String response) {
        try {
            String code = response.split(" ", 2)[0];
            return Integer.parseInt(code);
        } catch (Exception e) {
            return 500;
        }
    }

    /**
     * Parses the JSON body from a response like "200 {...}"
     */
    public static JSONObject getResponseBody(String response) {
        try {
            String body = response.substring(response.indexOf(" ") + 1);
            return new JSONObject(body);
        } catch (Exception e) {
            JSONObject err = new JSONObject();
            err.put("error", "Failed to parse response: " + response);
            return err;
        }
    }
}
