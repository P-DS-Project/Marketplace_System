import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionManager {

    // Three separate connection pools for our three distributed nodes
    private static HikariDataSource node1UsersPool;
    private static HikariDataSource node2ProductsPool;
    private static HikariDataSource node3TransactionsPool;

    static {
        try {
            System.out.println("Initializing Distributed Database Connection Pools...");

            // --- NODE 1: Users & Accounts ---
            HikariConfig config1 = new HikariConfig();
            config1.setJdbcUrl("jdbc:postgresql://localhost:5432/marketplace_node1");
            config1.setUsername("postgres");
            config1.setPassword("postgres"); // <-- CHANGE THIS
            config1.setMaximumPoolSize(10);
            node1UsersPool = new HikariDataSource(config1);

            // --- NODE 2: Products & Inventory ---
            HikariConfig config2 = new HikariConfig();
            config2.setJdbcUrl("jdbc:postgresql://localhost:5432/marketplace_node2");
            config2.setUsername("postgres");
            config2.setPassword("postgres"); // <-- CHANGE THIS
            config2.setMaximumPoolSize(10);
            node2ProductsPool = new HikariDataSource(config2);

            // --- NODE 3: Transactions & Reports ---
            HikariConfig config3 = new HikariConfig();
            config3.setJdbcUrl("jdbc:postgresql://localhost:5432/marketplace_node3");
            config3.setUsername("postgres");
            config3.setPassword("postgres"); // <-- CHANGE THIS
            config3.setMaximumPoolSize(10);
            node3TransactionsPool = new HikariDataSource(config3);

            System.out.println("✅ All 3 Database Nodes Connected Successfully!");

        } catch (Exception e) {
            System.err.println("❌ Failed to initialize database connection pools.");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private DatabaseConnectionManager() {
    } // Prevent instantiation

    // --- Specific Getters for the DAO Layer to Route Queries ---

    public static Connection getNode1UsersConnection() throws SQLException {
        return node1UsersPool.getConnection();
    }

    public static Connection getNode2ProductsConnection() throws SQLException {
        return node2ProductsPool.getConnection();
    }

    public static Connection getNode3TransactionsConnection() throws SQLException {
        return node3TransactionsPool.getConnection();
    }

    // --- Shutdown Hook to Close All Pools Cleanly ---
    public static void closeAllPools() {
        if (node1UsersPool != null)
            node1UsersPool.close();
        if (node2ProductsPool != null)
            node2ProductsPool.close();
        if (node3TransactionsPool != null)
            node3TransactionsPool.close();
        System.out.println("All database connections closed.");
    }
}