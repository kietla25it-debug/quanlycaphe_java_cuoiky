package auroracafe.model;

import java.io.Serializable;

public class OrderItem implements Serializable {
    private final int menuItemId;
    private String name;
    private double unitPrice;
    private int quantity;
    private String note;

    public OrderItem(int menuItemId, String name, double unitPrice, int quantity, String note) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.note = note;
    }

    public int getMenuItemId() { return menuItemId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public double getLineTotal() { return unitPrice * quantity; }
}
