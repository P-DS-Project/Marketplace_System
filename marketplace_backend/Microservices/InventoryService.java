package Microservices;
import DAOs.InventoryDAO;
import Entities.InventoryEntity;

public class InventoryService {
    private InventoryDAO inventoryDao;

    public InventoryService(InventoryDAO inventoryDao) {
        this.inventoryDao = inventoryDao;
    }

}
