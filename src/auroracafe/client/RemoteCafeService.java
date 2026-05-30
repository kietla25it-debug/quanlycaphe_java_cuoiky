package auroracafe.client;

import auroracafe.model.*;
import auroracafe.network.Request;
import auroracafe.network.Response;
import auroracafe.network.ServiceCallRequest;
import auroracafe.service.CafeService;
import auroracafe.store.DataStore;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * CafeService phía CLIENT.
 * Lớp này KHÔNG xử lý nghiệp vụ và KHÔNG truy cập database.
 * Mọi hàm nghiệp vụ đều gửi request qua Socket đến Server; Server gọi CafeService thật và MySQL.
 */
public class RemoteCafeService extends CafeService {
    private final SocketClient client;

    public RemoteCafeService(SocketClient client) {
        super(new AppData(), new NoOpDataStore(), Path.of("exports", "invoices"));
        this.client = client;
    }

    @SuppressWarnings("unchecked")
    private <T> T call(String method, Object... args) {
        Response res = client.send(new Request("CAFE_SERVICE", new ServiceCallRequest(method, args)));
        if (!res.isSuccess()) {
            throw new IllegalStateException(res.getMessage());
        }
        return (T) res.getData();
    }

    @Override public AppData getData() { return call("getData"); }
    @Override public List<MenuItem> findMenu(String keyword, String category) { return call("findMenu", keyword, category); }
    @Override public List<MenuItem> getFeaturedMenuItems() { return call("getFeaturedMenuItems"); }
    @Override public List<String> getCategories() { return call("getCategories"); }
    @Override public MenuItem addMenuItem(String name, String category, double price, boolean available, String description) { return call("addMenuItem5", name, category, price, available, description); }
    @Override public MenuItem addMenuItem(String name, String category, double price, boolean available, String description, String imageUrl) { return call("addMenuItem6", name, category, price, available, description, imageUrl); }
    @Override public void updateMenuItem(int id, String name, String category, double price, boolean available, String description) { call("updateMenuItem6", id, name, category, price, available, description); }
    @Override public void updateMenuItem(int id, String name, String category, double price, boolean available, String description, String imageUrl) { call("updateMenuItem7", id, name, category, price, available, description, imageUrl); }
    @Override public void deleteMenuItem(int id) { call("deleteMenuItem", id); }
    @Override public List<CafeTable> getTables() { return call("getTables"); }
    @Override public long getOccupiedTableCount() { return call("getOccupiedTableCount"); }
    @Override public long getAvailableTableCount() { return call("getAvailableTableCount"); }
    @Override public void updateTableStatus(int tableId, TableStatus status) { call("updateTableStatus", tableId, status); }
    @Override public Order openOrder(int tableId, User user, String customerName) { return call("openOrder", tableId, user, customerName); }
    @Override public Order openOrResumeOrder(int tableId, User user, String customerName) { return call("openOrResumeOrder", tableId, user, customerName); }
    @Override public Order getOpenOrderForTable(int tableId) { return call("getOpenOrderForTable", tableId); }
    @Override public void addItemToOrder(int orderId, MenuItem menuItem, int quantity, String note) { call("addItemToOrder", orderId, menuItem, quantity, note); }
    @Override public void updateOrderItem(int orderId, int menuItemId, int quantity, String note) { call("updateOrderItem", orderId, menuItemId, quantity, note); }
    @Override public void removeItem(int orderId, int menuItemId) { call("removeItem", orderId, menuItemId); }
    @Override public void releaseTable(int tableId) { call("releaseTable", tableId); }
    @Override public Path checkoutOrder(int orderId, String paymentMethod, String customerName) { String path = call("checkoutOrder", orderId, paymentMethod, customerName); return Path.of(path); }
    @Override public List<Order> getOrders() { return call("getOrders"); }
    @Override public List<Order> findOrders(String keyword, LocalDate from, LocalDate to) { return call("findOrders", keyword, from, to); }
    @Override public String buildInvoiceText(Order order) { return call("buildInvoiceText", order); }
    @Override public Path exportInvoice(Order order) { String path = call("exportInvoice", order); return Path.of(path); }
    @Override public Path exportMenuCsv(Path outputPath) { String path = call("exportMenuCsv", outputPath.toString()); return Path.of(path); }
    @Override public int importMenuCsv(Path inputPath, boolean replaceAll) { return call("importMenuCsv", inputPath.toString(), replaceAll); }
    @Override public void saveSettings(String businessName, String slogan, double taxRate, double serviceRate, String footer) { call("saveSettings", businessName, slogan, taxRate, serviceRate, footer); }
    @Override public boolean isClockedIn(User user) { return call("isClockedIn", user); }
    @Override public ShiftRecord clockIn(User user) { return call("clockIn", user); }
    @Override public ShiftRecord clockOut(User user) { return call("clockOut", user); }
    @Override public ShiftRecord getOpenShift(User user) { return call("getOpenShift", user); }
    @Override public double getTodayHoursForUser(User user) { return call("getTodayHoursForUser", user); }
    @Override public List<ShiftRecord> getTodayShiftRecords() { return call("getTodayShiftRecords"); }
    @Override public DailySalesStats getTodaySalesStats() { return call("getTodaySalesStats"); }
    @Override public List<DailySalesStats> getDailySalesStats(int days) { return call("getDailySalesStats", days); }
    @Override public Map<String, Integer> getTopSellingItemsToday() { return call("getTopSellingItemsToday"); }
    @Override public DailySalesStats getCurrentMonthSalesStats() { return call("getCurrentMonthSalesStats"); }
    @Override public boolean canEditFees(User user) { return call("canEditFees", user); }
    @Override public void save() { call("save"); }
    @Override public MenuItem findMenuItem(int id) { return call("findMenuItem", id); }
    @Override public CafeTable findTable(int id) { return call("findTable", id); }
    @Override public Order findOrder(int id) { return call("findOrder", id); }
    @Override public String summaryStats() { return call("summaryStats"); }
    @Override public boolean canManageUsers(User user) { return call("canManageUsers", user); }

    private static class NoOpDataStore implements DataStore {
        @Override public AppData load() { return new AppData(); }
        @Override public void save(AppData data) { }
    }
}
