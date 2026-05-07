package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionManager {

    // Connection details for our three distributed nodes
    private static final String NODE1_URL = "jdbc:postgresql://localhost:5432/marketplace_node1";
    private static final String NODE2_URL = "jdbc:postgresql://localhost:5432/marketplace_node2";
    private static final String NODE3_URL = "jdbc:postgresql://localhost:5432/marketplace_node3";

    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres"; // <-- CHANGE THIS

    static {
        try {
            // Load the PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");
            System.out.println("✅ PostgreSQL JDBC Driver loaded successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ PostgreSQL JDBC Driver not found! Make sure postgresql.jar is on the classpath.");
            throw new RuntimeException(e);
        }
    }

    private DatabaseConnectionManager() {
    } // Prevent instantiation

    // --- Specific Getters for the DAO Layer to Route Queries ---

    public static Connection getNode1UsersConnection() throws SQLException {
        return DriverManager.getConnection(NODE1_URL, USERNAME, PASSWORD);
    }

    public static Connection getNode2ProductsConnection() throws SQLException {
        return DriverManager.getConnection(NODE2_URL, USERNAME, PASSWORD);
    }

    public static Connection getNode3TransactionsConnection() throws SQLException {
        return DriverManager.getConnection(NODE3_URL, USERNAME, PASSWORD);
    }

    // --- Shutdown Hook (no pools to close with pure JDBC, but kept for interface consistency) ---
    public static void closeAllPools() {
        System.out.println("All database connections closed.");
    }
}
