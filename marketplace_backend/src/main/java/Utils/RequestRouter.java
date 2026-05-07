package Utils;

import Handlers.ServiceHandler;
import Handlers.UserHandler;
import Handlers.ProductHandler;

import java.util.HashMap;
import java.util.Map;

public class RequestRouter {

    private final Map<String, ServiceHandler> routingTable;

    public RequestRouter() {
        routingTable = new HashMap<>();

        routingTable.put("USER", new UserHandler());
        routingTable.put("PRODUCT", new ProductHandler());
    }

    public String route(String serviceDomain, String action, String jsonPayload) {

        ServiceHandler handler = routingTable.get(serviceDomain.toUpperCase());

        if (handler != null) {
            return handler.handleRequest(action, jsonPayload);
        } else {
            return "404 {\"error\":\"Service Domain Not Found: " + serviceDomain + "\"}";
        }
    }
}