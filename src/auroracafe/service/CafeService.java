package auroracafe.service;

import auroracafe.model.AppData;
import auroracafe.model.AppSettings;
import auroracafe.model.CafeTable;
import auroracafe.model.DailySalesStats;
import auroracafe.model.MenuItem;
import auroracafe.model.Order;
import auroracafe.model.OrderItem;
import auroracafe.model.OrderStatus;
import auroracafe.model.Role;
import auroracafe.model.ShiftRecord;
import auroracafe.model.TableStatus;
import auroracafe.model.User;
import auroracafe.store.DataStore;
import auroracafe.util.FormatUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CafeService {
    private final AppData data;
    private final DataStore store;
    private final Path invoiceDir;

    public CafeService(AppData data, DataStore store, Path invoiceDir) {
        this.data = data;
        this.store = store;
        this.invoiceDir = invoiceDir;
    }

    public AppData getData() {
        return data;
    }

    public List<MenuItem> findMenu(String keyword, String category) {
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return data.getMenuItems().stream()
                .filter(item -> category == null || category.equals("Tất cả") || item.getCategory().equalsIgnoreCase(category))
                .filter(item -> q.isBlank() || item.getName().toLowerCase(Locale.ROOT).contains(q)
                        || item.getDescription().toLowerCase(Locale.ROOT).contains(q))
                .sorted(Comparator.comparing(MenuItem::getCategory).thenComparing(MenuItem::getName))
                .collect(Collectors.toList());
    }

    public List<MenuItem> getFeaturedMenuItems() {
        return data.getMenuItems().stream().filter(MenuItem::isAvailable).limit(6).collect(Collectors.toList());
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("Tất cả");
        data.getMenuItems().stream().map(MenuItem::getCategory).distinct().sorted().forEach(categories::add);
        return categories;
    }

    public MenuItem addMenuItem(String name, String category, double price, boolean available, String description) {
        return addMenuItem(name, category, price, available, description, "");
    }

    public MenuItem addMenuItem(String name, String category, double price, boolean available, String description, String imageUrl) {
        MenuItem item = new MenuItem(data.nextMenuItemId(), name, category, price, available, description, imageUrl);
        data.getMenuItems().add(item);
        save();
        return item;
    }

    public void updateMenuItem(int id, String name, String category, double price, boolean available, String description) {
        updateMenuItem(id, name, category, price, available, description, "");
    }

    public void updateMenuItem(int id, String name, String category, double price, boolean available, String description, String imageUrl) {
        MenuItem item = findMenuItem(id);
        if (item == null) return;
        item.setName(name);
        item.setCategory(category);
        item.setPrice(price);
        item.setAvailable(available);
        item.setDescription(description);
        item.setImageUrl(imageUrl == null ? "" : imageUrl.trim());
        save();
    }

    public void deleteMenuItem(int id) {
        data.getMenuItems().removeIf(item -> item.getId() == id);
        save();
    }

    public List<CafeTable> getTables() {
        return data.getTables().stream().sorted(Comparator.comparingInt(CafeTable::getId)).collect(Collectors.toList());
    }

    public long getOccupiedTableCount() {
        return data.getTables().stream().filter(this::isTableCurrentlyOccupied).count();
    }

    public long getAvailableTableCount() {
        return data.getTables().stream()
                .filter(table -> table.getStatus() == TableStatus.AVAILABLE && !isTableCurrentlyOccupied(table))
                .count();
    }

    private boolean isTableCurrentlyOccupied(CafeTable table) {
        if (table == null) return false;
        if (table.getCurrentOrderId() != null) {
            Order current = findOrder(table.getCurrentOrderId());
            if (current != null && current.getStatus() == OrderStatus.OPEN) {
                return true;
            }
        }
        return table.getStatus() == TableStatus.OCCUPIED;
    }

    public void updateTableStatus(int tableId, TableStatus status) {
        CafeTable table = findTable(tableId);
        if (table == null) return;
        table.setStatus(status);
        if (status == TableStatus.AVAILABLE) {
            table.setCurrentOrderId(null);
        }
        save();
    }

    public Order openOrder(int tableId, User user, String customerName) {
        CafeTable table = findTable(tableId);
        if (table == null) throw new IllegalArgumentException("Không tìm thấy bàn.");
        if (table.getCurrentOrderId() != null) {
            Order existing = findOrder(table.getCurrentOrderId());
            if (existing != null && existing.getStatus() == OrderStatus.OPEN) {
                if (customerName != null && !customerName.isBlank() && (existing.getCustomerName() == null || existing.getCustomerName().equalsIgnoreCase("Khách lẻ"))) {
                    existing.setCustomerName(customerName.trim());
                    save();
                }
                return existing;
            }
        }
        Order order = new Order(data.nextOrderId(), tableId, table.getName(), user.getFullName(), LocalDateTime.now());
        order.setTaxRate(data.getSettings().getTaxRate());
        order.setServiceChargeRate(data.getSettings().getServiceChargeRate());
        if (customerName != null && !customerName.isBlank()) {
            order.setCustomerName(customerName.trim());
        }
        data.getOrders().add(order);
        table.setCurrentOrderId(order.getId());
        table.setStatus(TableStatus.OCCUPIED);
        save();
        return order;
    }

    public Order openOrResumeOrder(int tableId, User user, String customerName) {
        Order order = getOpenOrderForTable(tableId);
        return order != null ? order : openOrder(tableId, user, customerName);
    }

    public Order getOpenOrderForTable(int tableId) {
        CafeTable table = findTable(tableId);
        if (table == null || table.getCurrentOrderId() == null) return null;
        Order order = findOrder(table.getCurrentOrderId());
        if (order != null && order.getStatus() == OrderStatus.OPEN) {
            return order;
        }
        return null;
    }

    public void addItemToOrder(int orderId, MenuItem menuItem, int quantity, String note) {
        Order order = findOrder(orderId);
        if (order == null) return;
        Optional<OrderItem> existing = order.getItems().stream()
                .filter(it -> it.getMenuItemId() == menuItem.getId() && sameNote(it.getNote(), note))
                .findFirst();
        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
        } else {
            order.getItems().add(new OrderItem(menuItem.getId(), menuItem.getName(), menuItem.getPrice(), quantity, note == null ? "" : note.trim()));
        }
        save();
    }

    public void updateOrderItem(int orderId, int menuItemId, int quantity, String note) {
        Order order = findOrder(orderId);
        if (order == null) return;
        order.getItems().stream()
                .filter(item -> item.getMenuItemId() == menuItemId)
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(Math.max(1, quantity));
                    item.setNote(note == null ? "" : note.trim());
                });
        save();
    }

    public void removeItem(int orderId, int menuItemId) {
        Order order = findOrder(orderId);
        if (order == null) return;
        order.getItems().removeIf(item -> item.getMenuItemId() == menuItemId);
        if (order.getItems().isEmpty()) {
            CafeTable table = findTable(order.getTableId());
            if (table != null) {
                table.setStatus(TableStatus.AVAILABLE);
                table.setCurrentOrderId(null);
            }
            order.setStatus(OrderStatus.CANCELLED);
            order.setClosedAt(LocalDateTime.now());
        }
        save();
    }

    public void releaseTable(int tableId) {
        CafeTable table = findTable(tableId);
        if (table == null) {
            throw new IllegalArgumentException("Không tìm thấy bàn.");
        }
        if (table.getCurrentOrderId() != null) {
            Order order = findOrder(table.getCurrentOrderId());
            if (order != null && order.getStatus() == OrderStatus.OPEN) {
                order.setStatus(OrderStatus.CANCELLED);
                order.setClosedAt(LocalDateTime.now());
            }
        }
        table.setStatus(TableStatus.AVAILABLE);
        table.setCurrentOrderId(null);
        save();
    }

    public Path checkoutOrder(int orderId, String paymentMethod, String customerName) {
        Order order = findOrder(orderId);
        if (order == null) throw new IllegalArgumentException("Order không tồn tại.");
        if (order.getItems().isEmpty()) throw new IllegalArgumentException("Order chưa có món.");
        order.setPaymentMethod(paymentMethod == null || paymentMethod.isBlank() ? "Tiền mặt" : paymentMethod);
        if (customerName != null && !customerName.isBlank()) {
            order.setCustomerName(customerName.trim());
        }
        order.setStatus(OrderStatus.PAID);
        order.setClosedAt(LocalDateTime.now());
        CafeTable table = findTable(order.getTableId());
        if (table != null) {
            table.setStatus(TableStatus.AVAILABLE);
            table.setCurrentOrderId(null);
        }
        save();
        return exportInvoice(order);
    }

    public List<Order> getOrders() {
        return data.getOrders().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Order> findOrders(String keyword, LocalDate from, LocalDate to) {
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return getOrders().stream()
                .filter(order -> q.isBlank() || String.valueOf(order.getId()).contains(q)
                        || order.getTableName().toLowerCase(Locale.ROOT).contains(q)
                        || order.getCreatedBy().toLowerCase(Locale.ROOT).contains(q)
                        || order.getCustomerName().toLowerCase(Locale.ROOT).contains(q))
                .filter(order -> from == null || !order.getCreatedAt().toLocalDate().isBefore(from))
                .filter(order -> to == null || !order.getCreatedAt().toLocalDate().isAfter(to))
                .collect(Collectors.toList());
    }

    public String buildInvoiceText(Order order) {
        AppSettings settings = data.getSettings();
        StringBuilder sb = new StringBuilder();
        sb.append(settings.getBusinessName()).append('\n');
        sb.append(settings.getSlogan()).append('\n');
        sb.append("====================================\n");
        sb.append("Mã hóa đơn: #").append(order.getId()).append('\n');
        sb.append("Khách hàng: ").append(order.getCustomerName()).append('\n');
        sb.append("Bàn: ").append(order.getTableName()).append('\n');
        sb.append("Nhân viên: ").append(order.getCreatedBy()).append('\n');
        sb.append("Mở lúc: ").append(FormatUtils.dateTime(order.getCreatedAt())).append('\n');
        sb.append("Thanh toán: ").append(FormatUtils.dateTime(order.getClosedAt())).append('\n');
        sb.append("Hình thức: ").append(order.getPaymentMethod()).append('\n');
        sb.append("------------------------------------\n");
        for (OrderItem item : order.getItems()) {
            sb.append(item.getName()).append(" x").append(item.getQuantity())
                    .append(" - ").append(FormatUtils.money(item.getLineTotal())).append('\n');
            if (item.getNote() != null && !item.getNote().isBlank()) {
                sb.append("   Ghi chú: ").append(item.getNote()).append('\n');
            }
        }
        sb.append("------------------------------------\n");
        sb.append("Tạm tính: ").append(FormatUtils.money(order.getSubtotal())).append('\n');
        sb.append("Thuế: ").append(FormatUtils.money(order.getTaxAmount())).append('\n');
        sb.append("Phí dịch vụ: ").append(FormatUtils.money(order.getServiceChargeAmount())).append('\n');
        sb.append("Tổng cộng: ").append(FormatUtils.money(order.getTotal())).append('\n');
        sb.append("====================================\n");
        sb.append(settings.getInvoiceFooter()).append('\n');
        return sb.toString();
    }

    public Path exportInvoice(Order order) {
        try {
            Files.createDirectories(invoiceDir);
            String invoiceText = buildInvoiceText(order);
            Path path = invoiceDir.resolve("invoice_" + order.getId() + "_" + FormatUtils.fileStamp(order.getCreatedAt()) + ".txt");
            Files.writeString(path, invoiceText);

            Path ledger = invoiceDir.resolve("hoa_don_tong_hop.txt");
            String separator = "\n\n============================================================\n\n";
            String content = (Files.exists(ledger) ? Files.readString(ledger) + separator : "") + invoiceText;
            Files.writeString(ledger, content);
            return path;
        } catch (IOException e) {
            throw new RuntimeException("Không thể xuất hóa đơn: " + e.getMessage(), e);
        }
    }


    public Path exportMenuCsv(Path outputPath) {
        try {
            if (outputPath.getParent() != null) Files.createDirectories(outputPath.getParent());
            StringBuilder sb = new StringBuilder();
            sb.append("id,name,category,price,available,description,image_url\n");
            for (MenuItem item : data.getMenuItems()) {
                sb.append(item.getId()).append(',')
                        .append(csv(item.getName())).append(',')
                        .append(csv(item.getCategory())).append(',')
                        .append(item.getPrice()).append(',')
                        .append(item.isAvailable()).append(',')
                        .append(csv(item.getDescription())).append(',')
                        .append(csv(item.getImageUrl())).append('\n');
            }
            Files.writeString(outputPath, sb.toString());
            return outputPath;
        } catch (IOException e) {
            throw new RuntimeException("Không thể export menu CSV: " + e.getMessage(), e);
        }
    }

    public int importMenuCsv(Path inputPath, boolean replaceAll) {
        try {
            List<String> lines = Files.readAllLines(inputPath);
            if (lines.isEmpty()) throw new IllegalArgumentException("File CSV trống.");
            List<MenuItem> imported = new ArrayList<>();
            int start = lines.get(0).toLowerCase(Locale.ROOT).contains("name") ? 1 : 0;
            for (int i = start; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isBlank()) continue;
                List<String> cols = parseCsvLine(line);
                // Hỗ trợ 2 mẫu: id,name,category,price,available,description,image_url hoặc name,category,price,available,description,image_url
                int offset = cols.size() >= 7 ? 1 : 0;
                if (cols.size() < 6) throw new IllegalArgumentException("Dòng " + (i + 1) + " sai định dạng, cần tối thiểu 6 cột.");
                String name = cols.get(offset).trim();
                String category = cols.get(offset + 1).trim();
                double price = Double.parseDouble(cols.get(offset + 2).trim());
                boolean available = Boolean.parseBoolean(cols.get(offset + 3).trim());
                String description = cols.get(offset + 4).trim();
                String imageUrl = cols.get(offset + 5).trim();
                if (name.isBlank() || category.isBlank()) throw new IllegalArgumentException("Dòng " + (i + 1) + " thiếu tên hoặc danh mục.");
                if (price <= 0) throw new IllegalArgumentException("Dòng " + (i + 1) + " giá tiền phải lớn hơn 0.");
                imported.add(new MenuItem(data.nextMenuItemId(), name, category, price, available, description, imageUrl));
            }
            if (imported.isEmpty()) throw new IllegalArgumentException("Không có món hợp lệ để import.");
            if (replaceAll) data.getMenuItems().clear();
            data.getMenuItems().addAll(imported);
            save();
            return imported.size();
        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file CSV: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("File CSV có giá tiền sai định dạng.", e);
        }
    }

    private String csv(String value) {
        String v = value == null ? "" : value;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values;
    }

    public void saveSettings(String businessName, String slogan, double taxRate, double serviceRate, String footer) {
        AppSettings settings = data.getSettings();
        settings.setBusinessName(businessName);
        settings.setSlogan(slogan);
        settings.setTaxRate(taxRate);
        settings.setServiceChargeRate(serviceRate);
        settings.setInvoiceFooter(footer);
        save();
    }

    public boolean isClockedIn(User user) {
        return data.getShiftRecords().stream().anyMatch(s -> s.getUserId() == user.getId() && s.isOpen());
    }

    public ShiftRecord clockIn(User user) {
        if (isClockedIn(user)) {
            return getOpenShift(user);
        }
        ShiftRecord record = new ShiftRecord(data.nextShiftId(), user.getId(), user.getFullName(), LocalDateTime.now());
        data.getShiftRecords().add(record);
        save();
        return record;
    }

    public ShiftRecord clockOut(User user) {
        ShiftRecord record = getOpenShift(user);
        if (record != null) {
            record.setClockOut(LocalDateTime.now());
            save();
        }
        return record;
    }

    public ShiftRecord getOpenShift(User user) {
        return data.getShiftRecords().stream()
                .filter(s -> s.getUserId() == user.getId() && s.isOpen())
                .reduce((first, second) -> second)
                .orElse(null);
    }

    public double getTodayHoursForUser(User user) {
        LocalDate today = LocalDate.now();
        return data.getShiftRecords().stream()
                .filter(s -> s.getUserId() == user.getId())
                .filter(s -> s.getDate().equals(today))
                .mapToDouble(ShiftRecord::getWorkedHours)
                .sum();
    }

    public List<ShiftRecord> getTodayShiftRecords() {
        LocalDate today = LocalDate.now();
        return data.getShiftRecords().stream()
                .filter(s -> s.getDate().equals(today))
                .sorted(Comparator.comparing(ShiftRecord::getClockIn).reversed())
                .collect(Collectors.toList());
    }

    public DailySalesStats getTodaySalesStats() {
        return buildStatsForDate(LocalDate.now());
    }

    public List<DailySalesStats> getDailySalesStats(int days) {
        List<DailySalesStats> stats = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            stats.add(buildStatsForDate(LocalDate.now().minusDays(i)));
        }
        return stats;
    }

    public Map<String, Integer> getTopSellingItemsToday() {
        Map<String, Integer> map = new LinkedHashMap<>();
        getOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .filter(o -> o.getClosedAt() != null && o.getClosedAt().toLocalDate().equals(LocalDate.now()))
                .flatMap(o -> o.getItems().stream())
                .forEach(item -> map.merge(item.getName(), item.getQuantity(), Integer::sum));
        return map.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    public DailySalesStats getCurrentMonthSalesStats() {
        LocalDate now = LocalDate.now();
        List<Order> paid = data.getOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .filter(o -> o.getClosedAt() != null)
                .filter(o -> o.getClosedAt().getYear() == now.getYear() && o.getClosedAt().getMonth() == now.getMonth())
                .collect(Collectors.toList());
        int cups = paid.stream().flatMap(o -> o.getItems().stream()).mapToInt(OrderItem::getQuantity).sum();
        double revenue = paid.stream().mapToDouble(Order::getTotal).sum();
        return new DailySalesStats(now.withDayOfMonth(1), paid.size(), cups, revenue);
    }

    public boolean canEditFees(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    private DailySalesStats buildStatsForDate(LocalDate date) {
        List<Order> paid = data.getOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .filter(o -> o.getClosedAt() != null && o.getClosedAt().toLocalDate().equals(date))
                .collect(Collectors.toList());
        int cups = paid.stream().flatMap(o -> o.getItems().stream()).mapToInt(OrderItem::getQuantity).sum();
        double revenue = paid.stream().mapToDouble(Order::getTotal).sum();
        return new DailySalesStats(date, paid.size(), cups, revenue);
    }

    public void save() {
        store.save(data);
    }

    public MenuItem findMenuItem(int id) {
        return data.getMenuItems().stream().filter(item -> item.getId() == id).findFirst().orElse(null);
    }

    public CafeTable findTable(int id) {
        return data.getTables().stream().filter(table -> table.getId() == id).findFirst().orElse(null);
    }

    public Order findOrder(int id) {
        return data.getOrders().stream().filter(order -> order.getId() == id).findFirst().orElse(null);
    }

    public String summaryStats() {
        long openTables = getOccupiedTableCount();
        long availableTables = getAvailableTableCount();
        DailySalesStats today = getTodaySalesStats();
        return "Bàn đang phục vụ: " + openTables + " | Bàn trống: " + availableTables
                + " | Hóa đơn hôm nay: " + today.getPaidOrders() + " | Doanh thu hôm nay: " + FormatUtils.money(today.getRevenue());
    }

    public boolean canManageUsers(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    private boolean sameNote(String a, String b) {
        String x = a == null ? "" : a.trim();
        String y = b == null ? "" : b.trim();
        return x.equalsIgnoreCase(y);
    }
}
