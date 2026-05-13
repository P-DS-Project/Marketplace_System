package Microservices;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class AuthService {

    public String generateCryptoSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public String generateJWT(int userId, String role) {

        String header = Base64.getEncoder().encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes());
        String payload = Base64.getEncoder()
                .encodeToString(("{\"userId\":" + userId + ",\"role\":\"" + role + "\"}").getBytes());
        return header + "." + payload + ".signature_stub";
    }

    public boolean verifyJWT(String token) {
        return token != null && token.contains(".");
    }

    public int extractUserIdFromToken(String token) {
        if (!verifyJWT(token))
            return -1;
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2)
                return -1;
            String payloadStr = new String(Base64.getDecoder().decode(parts[1]));
            org.json.JSONObject payload = new org.json.JSONObject(payloadStr);
            return payload.optInt("userId", -1);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String extractRoleFromToken(String token) {
        if (!verifyJWT(token))
            return null;
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2)
                return null;
            String payloadStr = new String(Base64.getDecoder().decode(parts[1]));
            org.json.JSONObject payload = new org.json.JSONObject(payloadStr);
            return payload.optString("role", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isAdmin(String token) {
        return "ADMIN".equals(extractRoleFromToken(token));
    }
}
