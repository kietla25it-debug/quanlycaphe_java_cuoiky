package auroracafe.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order implements Serializable {
    private final int id;
    private final int tableId;
    private final String tableName;
    private final String createdBy;
    private final LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private OrderStatus status;
    private final List<OrderItem> items;
    private String paymentMethod;
    private double taxRate;
    private double serviceChargeRate;
    private String customerName;

    public Order(int id, int tableId, String tableName, String createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.tableId = tableId;
        this.tableName = tableName;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.status = OrderStatus.OPEN;
        this.items = new ArrayList<>();
        this.paymentMethod = "Tiền mặt";
        this.customerName = "Khách lẻ";
    }

    public int getId() { return id; }
    public int getTableId() { return tableId; }
    public String getTableName() { return tableName; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public List<OrderItem> getItems() { return items; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }
    public double getServiceChargeRate() { return serviceChargeRate; }
    public void setServiceChargeRate(double serviceChargeRate) { this.serviceChargeRate = serviceChargeRate; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public double getSubtotal() {
        return items.stream().mapToDouble(OrderItem::getLineTotal).sum();
    }

    public double getTaxAmount() {
        return getSubtotal() * taxRate;
    }

    public double getServiceChargeAmount() {
        return getSubtotal() * serviceChargeRate;
    }

    public double getTotal() {
        return getSubtotal() + getTaxAmount() + getServiceChargeAmount();
    }
}
