package Rest;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * RestServer — the REST API entry point.
 * 
 * Creates an HTTP server on port 9090 using Java's built-in HttpServer.
 * All incoming requests under /api/ are delegated to RestClientHandler,
 * which routes them to the appropriate microservice.
 * 
 * Usage:
 *   compile.bat
 *   run.bat Rest.RestServer
 * 
 * This server is independent of the existing TCP MarketplaceServer (port 8080).
 * Both can run simultaneously if needed.
 */
public class RestServer {

    private static final int PORT = 9090;
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) throws Exception {
        // Create the HTTP server bound to the specified port
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Create the handler that routes all REST requests to microservices
        RestClientHandler handler = new RestClientHandler();

        // Register the handler for all /api/ endpoints
        server.createContext("/api/", handler);

        // Use a fixed thread pool for concurrent request handling
        server.setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE));

        // Start the server
        server.start();

        System.out.println("==============================================");
        System.out.println("  Marketplace REST Server");
        System.out.println("  Listening on http://localhost:" + PORT);
        System.out.println("==============================================");
        System.out.println("  Available Endpoints:");
        System.out.println("  [GET]    /api/products/{id}");
        System.out.println("  [GET]    /api/products/seller/{sellerId}");
        System.out.println("  [POST]   /api/products");
        System.out.println("  [POST]   /api/users/register");
        System.out.println("  [POST]   /api/users/login");
        System.out.println("  [GET]    /api/search?keyword=...");
        System.out.println("==============================================");
        System.out.println("  Press Ctrl+C to stop the server.");
        System.out.println("==============================================");
    }
}
