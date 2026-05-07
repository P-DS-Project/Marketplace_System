package Microservices;

import DAOs.UserDAO;
import Entities.UserEntity;

public class UserService {

    private final UserDAO userDao;

    // Inject the DAO so the service can use it
    public UserService() {
        this.userDao = new UserDAO();
    }

    /**
     * Handles the business logic for registering a new user.
     * Matches the "User Registration" sequence diagram from your Phase 1 report.
     */
    public String registerUser(String username, String email, String plainPassword, String role) {

        // 1. Business Validation
        if (username == null || username.length() < 3) {
            return "ERROR: Username must be at least 3 characters.";
        }
        if (plainPassword == null || plainPassword.length() < 8) {
            return "ERROR: Password must be at least 8 characters.";
        }

        // 2. Security: Generate Salt and Hash Password
        // (You will implement PBKDF2 hashing here using pure Java libraries)
        String salt = generateCryptoSalt();
        String passwordHash = hashPassword(plainPassword, salt);

        // 3. Delegate to DAO
        boolean isCreated = userDao.createUser(username, email, passwordHash, salt, role);

        // 4. Return Business Result
        if (isCreated) {
            return "SUCCESS: User registered.";
            // In a full implementation, you might trigger email verification here as per
            // your docs
        } else {
            return "ERROR: Username or Email already exists.";
        }
    }

    /**
     * Handles the business logic for logging in.
     */
    public String login(String username, String plainPassword) {

        // 1. Fetch user from DAO
        UserEntity user = userDao.findByUsername(username);

        if (user == null) {
            return "ERROR: Invalid credentials.";
        }

        // 2. Verify Password
        String expectedHash = hashPassword(plainPassword, user.getSalt());

        if (expectedHash.equals(user.getPasswordHash())) {
            // 3. Generate JWT Token (Business logic for sessions)
            String jwt = generateJWT(user.getUserId(), user.getRole());
            return "SUCCESS: " + jwt;
        } else {
            return "ERROR: Invalid credentials.";
        }
    }

    // --- Cryptography Stubs (To be implemented using java.security) ---
    private String generateCryptoSalt() {
        return "random_salt_string";
    }

    private String hashPassword(String password, String salt) {
        return "hashed_" + password + "_" + salt;
    }

    private String generateJWT(int userId, String role) {
        return "dummy.jwt.token";
    }
}