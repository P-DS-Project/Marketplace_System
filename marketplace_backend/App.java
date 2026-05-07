import Utils.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class App {

    public static void main(String[] args) {
        System.out.println("Starting Distributed Marketplace Backend...");

        // By calling any method on the manager, the static block runs and connects to
        // all 3 DBs
        try {
            // Test Node 1
            testNode("Node 1 (Users)", DatabaseConnectionManager.getNode1UsersConnection(),
                    "SELECT COUNT(*) FROM users");

            // Test Node 2
            testNode("Node 2 (Products)", DatabaseConnectionManager.getNode2ProductsConnection(),
                    "SELECT COUNT(*) FROM products");

            // Test Node 3
            testNode("Node 3 (Transactions)", DatabaseConnectionManager.getNode3TransactionsConnection(),
                    "SELECT COUNT(*) FROM transactions");

        } catch (SQLException e) {
            System.err.println("❌ A database error occurred during testing.");
            e.printStackTrace();
        } finally {
            // Always close pools when the server stops
            DatabaseConnectionManager.closeAllPools();
        }
    }

    // Helper method to run a test query on a specific connection
    private static void testNode(String nodeName, Connection conn, String sql) {
        try (conn; // Auto-closes the connection back to the pool
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("✅ " + nodeName + " is working! Row count: " + count);
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to query " + nodeName);
            e.printStackTrace();
        }
    }
}