package Microservices;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class AuthService {

    /**
     * Generates a random salt for password hashing.
     */
    public String generateCryptoSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password using SHA-256 and the provided salt.
     */
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

    /**
     * Generates a simple mock JWT token based on the user's ID and role.
     */
    public String generateJWT(int userId, String role) {
        // In a real app, use a JWT library like jjwt to sign the token properly.
        // For pure Java, we simulate it by Base64 encoding the payload.
        String header = Base64.getEncoder().encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes());
        String payload = Base64.getEncoder().encodeToString(("{\"userId\":" + userId + ",\"role\":\"" + role + "\"}").getBytes());
        return header + "." + payload + ".signature_stub";
    }
    
    /**
     * Very basic stub to verify if a token is valid
     */
    public boolean verifyJWT(String token) {
        return token != null && token.contains(".");
    }

    /**
     * Extracts the userId from our simple Base64 encoded token.
     */
    public int extractUserIdFromToken(String token) {
        if (!verifyJWT(token)) return -1;
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return -1;
            String payloadStr = new String(Base64.getDecoder().decode(parts[1]));
            // Use org.json.JSONObject to parse the payload safely
            org.json.JSONObject payload = new org.json.JSONObject(payloadStr);
            return payload.optInt("userId", -1);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
