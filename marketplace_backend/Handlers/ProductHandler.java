package Handlers;

public class ProductHandler implements ServiceHandler {

    // private final ProductService productService;

    @Override
    public String handleRequest(String action, String jsonPayload) {
        switch (action.toUpperCase()) {
            case "ADD":
                return "201 {\"message\":\"Product added\"}";
            case "SEARCH":
                return "200 {\"products\":[]}";
            default:
                return "400 {\"error\":\"Unknown Product Action: " + action + "\"}";
        }
    }
}
