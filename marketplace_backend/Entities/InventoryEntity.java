package Entities;

public class InventoryEntity {
    private int inventoryId;
    private int productId;
    private int quantity;
    private String Updated_at;
    private String Warehouse_node;

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUpdated_at() {
        return Updated_at;
    }

    public void setUpdated_at(String updated_at) {
        Updated_at = updated_at;
    }

    public String getWarehouse_node() {
        return Warehouse_node;
    }

    public void setWarehouse_node(String warehouse_node) {
        Warehouse_node = warehouse_node;
    }

}
