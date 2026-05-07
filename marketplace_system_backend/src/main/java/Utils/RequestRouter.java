package Utils;

import Handlers.ServiceHandler;
import Handlers.UserHandler;
import Handlers.ProductHandler;

import java.util.HashMap;
import java.util.Map;

public class RequestRouter {

    // A map matching the domain string (e.g., "USER") to the correct Handler
    private final Map<String, ServiceHandler> routingTable;

    public RequestRouter() {
        routingTable = new HashMap<>();

        // Register all your microservices here
        routingTable.put("USER", new UserHandler());
        routingTable.put("PRODUCT", new ProductHandler());
        // routingTable.put("TRANSACTION", new TransactionHandler());
    }

    public String route(String serviceDomain, String action, String jsonPayload) {
        // 1. Look up the requested service in the map
        ServiceHandler handler = routingTable.get(serviceDomain.toUpperCase());

        // 2. If the service exists, hand the request to it
        if (handler != null) {
            return handler.handleRequest(action, jsonPayload);
        } else {
            // 3. If the client asked for a service that doesn't exist
            return "404 {\"error\":\"Service Domain Not Found: " + serviceDomain + "\"}";
        }
    }
}