package Microservices;
import DAOs.InventoryDAO;
import Entities.InventoryEntity;

public class InventoryService {
    private InventoryDAO inventoryDao;

    //public InventoryService(){
    //this.inventoryDao = new InventoryDAO();
    //}
    public InventoryService(InventoryDAO inventoryDao) {
        this.inventoryDao = inventoryDao;
    }
    public String addStock(int productId, int quantity, String warehouseNode) {
        if (quantity < 0) {
            return "ERROR: Quantity cannot be negative.";
        }
        boolean success = inventoryDao.addStock(productId, quantity, warehouseNode);
        return success ? "SUCCESS: Inventory added." : "ERROR: Failed to add inventory.";
    }
    public String reduceStock(int productId, int quantity, String warehouseNode) {
        if (quantity < 0) {
            return "ERROR: Quantity cannot be negative.";
        }
        boolean success = inventoryDao.reduceStock(productId, quantity, warehouseNode);
        return success ? "SUCCESS: Inventory reduced." : "ERROR: Failed to reduce inventory. Check stock levels.";
    }
    public String checkStock(int productId, int requiredQuantity) {
        boolean inStock = inventoryDao.checkStock(productId, requiredQuantity);
        return inStock ? "SUCCESS: Sufficient stock available." : "ERROR: Insufficient stock.";
    }
    public String getInventoryDetails(int productId) {
        InventoryEntity inventory = inventoryDao.getInventoryByProductId(productId);
        if (inventory == null) {
            return "ERROR: Inventory not found for product ID " + productId;
        }
        return "Product ID: " + inventory.getProductId() + ", Quantity: " + inventory.getQuantity() + ", Warehouse Node: " + inventory.getWarehouse_node();
    }
}
