package auroracafe.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AppData implements Serializable {
    private final List<User> users = new ArrayList<>();
    private final List<MenuItem> menuItems = new ArrayList<>();
    private final List<CafeTable> tables = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<ShiftRecord> shiftRecords = new ArrayList<>();
    private final List<AuthHistoryRecord> authHistoryRecords = new ArrayList<>();
    private AppSettings settings = new AppSettings();
    private int nextUserId = 1;
    private int nextMenuItemId = 1;
    private int nextTableId = 1;
    private int nextOrderId = 1;
    private int nextShiftId = 1;
    private int nextAuthHistoryId = 1;

    public List<User> getUsers() { return users; }
    public List<MenuItem> getMenuItems() { return menuItems; }
    public List<CafeTable> getTables() { return tables; }
    public List<Order> getOrders() { return orders; }
    public List<ShiftRecord> getShiftRecords() { return shiftRecords; }
    public List<AuthHistoryRecord> getAuthHistoryRecords() { return authHistoryRecords; }
    public AppSettings getSettings() { return settings; }
    public void setSettings(AppSettings settings) { this.settings = settings; }
    public int nextUserId() { return nextUserId++; }
    public int nextMenuItemId() { return nextMenuItemId++; }
    public int nextTableId() { return nextTableId++; }
    public int nextOrderId() { return nextOrderId++; }
    public int nextShiftId() { return nextShiftId++; }
    public int nextAuthHistoryId() { return nextAuthHistoryId++; }

    public void syncCounters() {
        nextUserId = users.stream().mapToInt(User::getId).max().orElse(0) + 1;
        nextMenuItemId = menuItems.stream().mapToInt(MenuItem::getId).max().orElse(0) + 1;
        nextTableId = tables.stream().mapToInt(CafeTable::getId).max().orElse(0) + 1;
        nextOrderId = orders.stream().mapToInt(Order::getId).max().orElse(0) + 1;
        nextShiftId = shiftRecords.stream().mapToInt(ShiftRecord::getId).max().orElse(0) + 1;
        nextAuthHistoryId = authHistoryRecords.stream().mapToInt(AuthHistoryRecord::getId).max().orElse(0) + 1;
    }
}
