package models;

public class Inventory {
    private int productId;
    private int quantity;
    private String warehouseNode;

    public Inventory() {}

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getWarehouseNode() { return warehouseNode; }
    public void setWarehouseNode(String warehouseNode) { this.warehouseNode = warehouseNode; }
}
