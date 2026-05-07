package Handlers;

import Microservices.UserService;

public class UserHandler implements ServiceHandler {

    private final UserService userService;

    public UserHandler() {
        this.userService = new UserService();
    }

    @Override
    public String handleRequest(String action, String jsonPayload) {
        switch (action.toUpperCase()) {
            case "REGISTER":
                // Parse JSON and call userService.registerUser()
                return "200 {\"message\":\"User registered\"}";
            case "LOGIN":
                return "200 {\"token\":\"jwt_token\"}";
            default:
                return "400 {\"error\":\"Unknown User Action: " + action + "\"}";
                // localhost:8080 UserService LOGIN {"username":"abdel", "password":"secure123"}
                // localhost:appi/usre/
        }
    }
}