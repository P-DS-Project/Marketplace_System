package entities;

public class InventoryEntity {
    private int inventoryId;
    private int productId;
    private int quantity;
    private String Updated_at;
    private String Wearhouse_node;

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

    public String getWearhouse_node() {
        return Wearhouse_node;
    }

    public void setWearhouse_node(String wearhouse_node) {
        Wearhouse_node = wearhouse_node;
    }

}
