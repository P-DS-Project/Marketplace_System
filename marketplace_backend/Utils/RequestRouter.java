package Utils;

import java.util.concurrent.ConcurrentHashMap;

import Handlers.ChatHandler;
import Handlers.ProductHandler;
import Handlers.ServiceHandler;
import Handlers.UserHandler;
import Handlers.TransactionHandler;
import Handlers.InventoryHandler;
import Handlers.SearchHandler;
import Handlers.ReportHandler;

public class RequestRouter {

    private final ConcurrentHashMap<String, ServiceHandler> routingTable;

    public RequestRouter() {
        routingTable = new ConcurrentHashMap<>();

        routingTable.put("USER", new UserHandler());
        routingTable.put("PRODUCT", new ProductHandler());
        routingTable.put("CHAT", new ChatHandler());
        routingTable.put("TRANSACTION", new TransactionHandler());
        routingTable.put("INVENTORY", new InventoryHandler());
        routingTable.put("SEARCH", new SearchHandler());
        routingTable.put("REPORT", new ReportHandler());
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
