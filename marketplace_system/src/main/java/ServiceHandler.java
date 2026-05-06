
public interface ServiceHandler {
    // Every service must know how to handle its own actions
    String handleRequest(String action, String jsonPayload);
}