package Handlers;

import org.json.JSONObject;

import Microservices.UserService;

public class UserHandler implements ServiceHandler {

    private final UserService userService;

    public UserHandler() {
        this.userService = new UserService();
    }

    @Override
    public String handleRequest(String action, String jsonPayload) {
        JSONObject json;
        try {
            // Parse the incoming JSON string
            json = new JSONObject(jsonPayload != null ? jsonPayload : "{}");
        } catch (Exception e) {
            return "400 {\"error\":\"Invalid JSON payload format\"}";
        }

        switch (action.toUpperCase()) {
            case "REGISTER":
                // fallback to "name" if "username" is not provided
                String regUsername = json.optString("username", json.optString("name", null));
                String regEmail = json.optString("email", null);
                String regPassword = json.optString("password", null);
                String regRole = json.optString("role", "buyer"); // default to "buyer" if not provided

                String regResult = userService.registerUser(regUsername, regEmail, regPassword, regRole);
                if (regResult.startsWith("SUCCESS")) {
                    return "200 {\"message\":\"" + regResult + "\"}";
                } else {
                    return "400 {\"error\":\"" + regResult + "\"}";
                }

            case "LOGIN":
                String loginEmail = json.optString("email", null);
                String loginPassword = json.optString("password", null);

                String loginResult = userService.login(loginEmail, loginPassword);
                if (loginResult.startsWith("SUCCESS")) {
                    // Extract token from "SUCCESS: dummy.jwt.token"
                    String token = loginResult.substring(loginResult.indexOf(" ") + 1);
                    return "200 {\"token\":\"" + token + "\"}";
                } else {
                    return "401 {\"error\":\"" + loginResult + "\"}";
                }

            case "GET_INFO":
                String infoToken = json.optString("token", null);
                String infoResult = userService.getAccountInfo(infoToken);
                if (infoResult.startsWith("SUCCESS")) {
                    String userData = infoResult.substring(infoResult.indexOf(" ") + 1);
                    return "200 " + userData;
                } else {
                    return "404 {\"error\":\"" + infoResult + "\"}";
                }

            default:
                return "400 {\"error\":\"Unknown User Action: " + action + "\"}";
        }
    }
}
