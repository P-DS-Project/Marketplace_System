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
            json = new JSONObject(jsonPayload != null ? jsonPayload : "{}");
        } catch (Exception e) {
            return "400 {\"error\":\"Invalid JSON payload format\"}";
        }

        switch (action.toUpperCase()) {
            case "REGISTER": {
                String regUsername = json.optString("username", json.optString("name", null));
                String regEmail = json.optString("email", null);
                String regPassword = json.optString("password", null);
                String regRole = json.optString("role", "USER").toUpperCase().replace(" ", "_");

                String regResult = userService.registerUser(regUsername, regEmail, regPassword, regRole);
                if (regResult.startsWith("SUCCESS")) {
                    return "200 {\"message\":\"" + regResult + "\"}";
                } else {
                    return "400 {\"error\":\"" + regResult + "\"}";
                }
            }

            case "LOGIN": {
                String loginEmail = json.optString("email", null);
                String loginPassword = json.optString("password", null);

                String loginResult = userService.login(loginEmail, loginPassword);
                if (loginResult.startsWith("SUCCESS:")) {
                    // Format: "SUCCESS:<ROLE> <token>"
                    String afterSuccess = loginResult.substring(8); // "<ROLE> <token>"
                    int spaceIdx = afterSuccess.indexOf(" ");
                    String role = afterSuccess.substring(0, spaceIdx);
                    String token = afterSuccess.substring(spaceIdx + 1);
                    return "200 {\"token\":\"" + token + "\",\"role\":\"" + role + "\"}";
                } else {
                    return "401 {\"error\":\"" + loginResult + "\"}";
                }
            }

            case "GET_INFO": {
                String infoToken = json.optString("token", null);
                String infoResult = userService.getAccountInfo(infoToken);
                if (infoResult.startsWith("SUCCESS")) {
                    String userData = infoResult.substring(infoResult.indexOf(" ") + 1);
                    return "200 " + userData;
                } else {
                    return "404 {\"error\":\"" + infoResult + "\"}";
                }
            }

            case "UPDATE_PROFILE": {
                String token = json.optString("token", null);
                String username = json.optString("username", null);
                String email = json.optString("email", null);
                String avatarUrl = json.optString("avatarUrl", null);

                String result = userService.updateProfile(token, username, email, avatarUrl);
                if (result.startsWith("SUCCESS")) {
                    return "200 {\"message\":\"" + result + "\"}";
                }
                return "400 {\"error\":\"" + result + "\"}";
            }

            case "CHANGE_PASSWORD": {
                String token = json.optString("token", null);
                String oldPassword = json.optString("oldPassword", null);
                String newPassword = json.optString("newPassword", null);

                String result = userService.changePassword(token, oldPassword, newPassword);
                if (result.startsWith("SUCCESS")) {
                    return "200 {\"message\":\"" + result + "\"}";
                }
                return "400 {\"error\":\"" + result + "\"}";
            }

            case "DELETE_ACCOUNT": {
                String token = json.optString("token", null);
                String result = userService.deleteAccount(token);
                if (result.startsWith("SUCCESS")) {
                    return "200 {\"message\":\"" + result + "\"}";
                }
                return "400 {\"error\":\"" + result + "\"}";
            }

            case "GET_USERNAME": {
                int userId = json.optInt("userId", -1);
                if (userId == -1) return "400 {\"error\":\"Missing userId\"}";
                String username = userService.getUsernameById(userId);
                return "200 {\"username\":\"" + username + "\"}";
            }

            default:
                return "400 {\"error\":\"Unknown User Action: " + action + "\"}";
        }
    }
}
