package auroracafe.store;

import auroracafe.model.*;
import java.sql.*;

public class MySqlStore implements DataStore {
    private final MySqlConfig config;

    public MySqlStore(MySqlConfig config) {
        this.config = config;
    }

    @Override
    public AppData load() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            if (config.isAutoCreateSchema()) createDatabaseIfNeeded();
            ensureSchema();
            AppData data = readAll();
            if (data.getUsers().isEmpty()) {
                AppData seeded = new FileStore(java.nio.file.Path.of("data", "seed-backup.dat")).seed();
                save(seeded);
                return seeded;
            }
            data = new FileStore(java.nio.file.Path.of("data", "seed-backup.dat")).loadNormalized(data);
            data.syncCounters();
            return data;
        } catch (Exception e) {
            throw new RuntimeException("Không thể kết nối MySQL: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(AppData data) {
        data.syncCounters();
        try (Connection conn = getConnection(true)) {
            conn.setAutoCommit(false);
            try {
                wipeTables(conn);
                writeSettings(conn, data.getSettings());
                writeUsers(conn, data);
                writeMenu(conn, data);
                writeTables(conn, data);
                writeOrders(conn, data);
                writeShifts(conn, data);
                writeAuthHistory(conn, data);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Không thể lưu dữ liệu MySQL: " + e.getMessage(), e);
        }
    }

    private Connection getConnection(boolean withDb) throws SQLException {
        return DriverManager.getConnection(config.jdbcUrl(withDb), config.getUsername(), config.getPassword());
    }

    private void createDatabaseIfNeeded() throws SQLException {
        try (Connection conn = getConnection(false); Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + config.getDatabase() + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
    }

    private void ensureSchema() throws SQLException {
        try (Connection conn = getConnection(true); Statement st = conn.createStatement()) {
            // Cấu trúc bảng phải đồng bộ với file sql/schema_mysql.sql
            st.executeUpdate("CREATE TABLE IF NOT EXISTS app_settings (id TINYINT PRIMARY KEY, business_name VARCHAR(255), slogan VARCHAR(255), tax_rate DOUBLE, service_charge_rate DOUBLE, invoice_footer TEXT, accent_color_hex VARCHAR(32)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INT PRIMARY KEY, full_name VARCHAR(255), username VARCHAR(120) UNIQUE, password VARCHAR(255), role VARCHAR(20), active BOOLEAN) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS menu_items (id INT PRIMARY KEY, name VARCHAR(255), category VARCHAR(100), price DOUBLE, available BOOLEAN, description TEXT, image_url TEXT) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS cafe_tables (id INT PRIMARY KEY, name VARCHAR(100), seats INT, status VARCHAR(30), current_order_id INT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS orders (id INT PRIMARY KEY, table_id INT, table_name VARCHAR(100), created_by VARCHAR(255), created_at DATETIME, closed_at DATETIME NULL, status VARCHAR(20), payment_method VARCHAR(100), tax_rate DOUBLE, service_charge_rate DOUBLE, customer_name VARCHAR(255), CONSTRAINT fk_orders_table FOREIGN KEY (table_id) REFERENCES cafe_tables(id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS order_items (order_id INT, row_no INT, menu_item_id INT, name VARCHAR(255), unit_price DOUBLE, quantity INT, note TEXT, PRIMARY KEY(order_id, row_no), CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS shift_records (id INT PRIMARY KEY, user_id INT, user_name VARCHAR(255), clock_in DATETIME, clock_out DATETIME NULL, CONSTRAINT fk_shift_user FOREIGN KEY (user_id) REFERENCES users(id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS auth_history (id INT PRIMARY KEY, user_id INT, full_name VARCHAR(255), username VARCHAR(120), role_name VARCHAR(20), login_at DATETIME, logout_at DATETIME NULL, CONSTRAINT fk_auth_user FOREIGN KEY (user_id) REFERENCES users(id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        }

        // Sửa tự động database cũ nếu trước đó bảng order_items bị thiếu cột row_no.
        // Lỗi cũ thường gặp: Unknown column 'row_no' in 'order clause'.
        migrateOrderItemsTableIfNeeded();
    }

    private void migrateOrderItemsTableIfNeeded() throws SQLException {
        try (Connection conn = getConnection(true)) {
            if (!columnExists(conn, "order_items", "row_no")) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE order_items ADD COLUMN row_no INT NOT NULL DEFAULT 1 AFTER order_id");
                }
            }

            // Đảm bảo khóa chính đúng với code: PRIMARY KEY(order_id, row_no).
            // Nếu database cũ có khóa chính khác, xóa khóa cũ và tạo lại khóa mới.
            if (!primaryKeyIsOrderIdAndRowNo(conn)) {
                try (Statement st = conn.createStatement()) {
                    try {
                        st.executeUpdate("ALTER TABLE order_items DROP PRIMARY KEY");
                    } catch (SQLException ignored) {
                        // Bảng cũ có thể chưa có primary key.
                    }

                    // Nếu trong bảng cũ có nhiều dòng cùng order_id, row_no mặc định đều là 1.
                    // Cập nhật lại row_no theo từng order_id để tạo được khóa chính mới.
                    st.executeUpdate(
                            "UPDATE order_items oi " +
                            "JOIN ( " +
                            "  SELECT order_id, menu_item_id, name, " +
                            "         ROW_NUMBER() OVER (PARTITION BY order_id ORDER BY menu_item_id, name) AS rn " +
                            "  FROM order_items " +
                            ") x ON oi.order_id = x.order_id " +
                            "   AND oi.menu_item_id = x.menu_item_id " +
                            "   AND oi.name = x.name " +
                            "SET oi.row_no = x.rn"
                    );

                    st.executeUpdate("ALTER TABLE order_items ADD PRIMARY KEY(order_id, row_no)");
                }
            }
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, tableName, columnName)) {
            return rs.next();
        }
    }

    private boolean primaryKeyIsOrderIdAndRowNo(Connection conn) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        java.util.Map<Short, String> columns = new java.util.TreeMap<>();
        try (ResultSet rs = meta.getPrimaryKeys(conn.getCatalog(), null, "order_items")) {
            while (rs.next()) {
                columns.put(rs.getShort("KEY_SEQ"), rs.getString("COLUMN_NAME"));
            }
        }

        return columns.size() == 2
                && "order_id".equalsIgnoreCase(columns.get((short) 1))
                && "row_no".equalsIgnoreCase(columns.get((short) 2));
    }

    private AppData readAll() throws SQLException {
        AppData data = new AppData();
        try (Connection conn = getConnection(true)) {
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM app_settings WHERE id=1")) {
                if (rs.next()) {
                    AppSettings s = new AppSettings();
                    s.setBusinessName(rs.getString("business_name"));
                    s.setSlogan(rs.getString("slogan"));
                    s.setTaxRate(rs.getDouble("tax_rate"));
                    s.setServiceChargeRate(rs.getDouble("service_charge_rate"));
                    s.setInvoiceFooter(rs.getString("invoice_footer"));
                    s.setAccentColorHex(rs.getString("accent_color_hex"));
                    data.setSettings(s);
                }
            }
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM users ORDER BY id")) {
                while (rs.next()) data.getUsers().add(new User(rs.getInt("id"), rs.getString("full_name"), rs.getString("username"), rs.getString("password"), Role.valueOf(rs.getString("role")), rs.getBoolean("active")));
            }
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM menu_items ORDER BY id")) {
                while (rs.next()) data.getMenuItems().add(new MenuItem(rs.getInt("id"), rs.getString("name"), rs.getString("category"), rs.getDouble("price"), rs.getBoolean("available"), rs.getString("description"), rs.getString("image_url")));
            }
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM cafe_tables ORDER BY id")) {
                while (rs.next()) {
                    CafeTable t = new CafeTable(rs.getInt("id"), rs.getString("name"), rs.getInt("seats"), TableStatus.valueOf(rs.getString("status")));
                    int currentOrder = rs.getInt("current_order_id");
                    if (!rs.wasNull()) t.setCurrentOrderId(currentOrder);
                    data.getTables().add(t);
                }
            }
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM orders ORDER BY id")) {
                while (rs.next()) {
                    Order o = new Order(rs.getInt("id"), rs.getInt("table_id"), rs.getString("table_name"), rs.getString("created_by"), rs.getTimestamp("created_at").toLocalDateTime());
                    Timestamp closed = rs.getTimestamp("closed_at");
                    if (closed != null) o.setClosedAt(closed.toLocalDateTime());
                    o.setStatus(OrderStatus.valueOf(rs.getString("status")));
                    o.setPaymentMethod(rs.getString("payment_method"));
                    o.setTaxRate(rs.getDouble("tax_rate"));
                    o.setServiceChargeRate(rs.getDouble("service_charge_rate"));
                    o.setCustomerName(rs.getString("customer_name"));
                    data.getOrders().add(o);
                }
            }
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM order_items ORDER BY order_id, row_no")) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    Order o = data.getOrders().stream().filter(x -> x.getId() == orderId).findFirst().orElse(null);
                    if (o != null) o.getItems().add(new OrderItem(rs.getInt("menu_item_id"), rs.getString("name"), rs.getDouble("unit_price"), rs.getInt("quantity"), rs.getString("note")));
                }
            }
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM shift_records ORDER BY id")) {
                while (rs.next()) {
                    ShiftRecord shift = new ShiftRecord(rs.getInt("id"), rs.getInt("user_id"), rs.getString("user_name"), rs.getTimestamp("clock_in").toLocalDateTime());
                    Timestamp out = rs.getTimestamp("clock_out");
                    if (out != null) shift.setClockOut(out.toLocalDateTime());
                    data.getShiftRecords().add(shift);
                }
            }
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM auth_history ORDER BY id")) {
                while (rs.next()) {
                    AuthHistoryRecord rec = new AuthHistoryRecord(rs.getInt("id"), rs.getInt("user_id"), rs.getString("full_name"), rs.getString("username"), rs.getString("role_name"), rs.getTimestamp("login_at").toLocalDateTime());
                    Timestamp out = rs.getTimestamp("logout_at");
                    if (out != null) rec.setLogoutAt(out.toLocalDateTime());
                    data.getAuthHistoryRecords().add(rec);
                }
            }
        }
        data.syncCounters();
        return data;
    }

    private void wipeTables(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("SET FOREIGN_KEY_CHECKS=0");
            for (String table : new String[]{"order_items","orders","cafe_tables","menu_items","shift_records","auth_history","users","app_settings"}) st.executeUpdate("DELETE FROM " + table);
            st.executeUpdate("SET FOREIGN_KEY_CHECKS=1");
        }
    }

    private void writeSettings(Connection conn, AppSettings s) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO app_settings(id,business_name,slogan,tax_rate,service_charge_rate,invoice_footer,accent_color_hex) VALUES (1,?,?,?,?,?,?)")) {
            ps.setString(1, s.getBusinessName()); ps.setString(2, s.getSlogan()); ps.setDouble(3, s.getTaxRate()); ps.setDouble(4, s.getServiceChargeRate()); ps.setString(5, s.getInvoiceFooter()); ps.setString(6, s.getAccentColorHex()); ps.executeUpdate();
        }
    }
    private void writeUsers(Connection conn, AppData data) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO users(id,full_name,username,password,role,active) VALUES (?,?,?,?,?,?)")) {
            for (User u : data.getUsers()) { ps.setInt(1,u.getId()); ps.setString(2,u.getFullName()); ps.setString(3,u.getUsername()); ps.setString(4,u.getPassword()); ps.setString(5,u.getRole().name()); ps.setBoolean(6,u.isActive()); ps.addBatch(); }
            ps.executeBatch();
        }
    }
    private void writeMenu(Connection conn, AppData data) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO menu_items(id,name,category,price,available,description,image_url) VALUES (?,?,?,?,?,?,?)")) {
            for (MenuItem m : data.getMenuItems()) { ps.setInt(1,m.getId()); ps.setString(2,m.getName()); ps.setString(3,m.getCategory()); ps.setDouble(4,m.getPrice()); ps.setBoolean(5,m.isAvailable()); ps.setString(6,m.getDescription()); ps.setString(7,m.getImageUrl()); ps.addBatch(); }
            ps.executeBatch();
        }
    }
    private void writeTables(Connection conn, AppData data) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO cafe_tables(id,name,seats,status,current_order_id) VALUES (?,?,?,?,?)")) {
            for (CafeTable t : data.getTables()) { ps.setInt(1,t.getId()); ps.setString(2,t.getName()); ps.setInt(3,t.getSeats()); ps.setString(4,t.getStatus().name()); if (t.getCurrentOrderId()==null) ps.setNull(5,Types.INTEGER); else ps.setInt(5,t.getCurrentOrderId()); ps.addBatch(); }
            ps.executeBatch();
        }
    }
    private void writeOrders(Connection conn, AppData data) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO orders(id,table_id,table_name,created_by,created_at,closed_at,status,payment_method,tax_rate,service_charge_rate,customer_name) VALUES (?,?,?,?,?,?,?,?,?,?,?)"); PreparedStatement itemPs = conn.prepareStatement("INSERT INTO order_items(order_id,row_no,menu_item_id,name,unit_price,quantity,note) VALUES (?,?,?,?,?,?,?)")) {
            for (Order o : data.getOrders()) {
                ps.setInt(1,o.getId()); ps.setInt(2,o.getTableId()); ps.setString(3,o.getTableName()); ps.setString(4,o.getCreatedBy()); ps.setTimestamp(5,Timestamp.valueOf(o.getCreatedAt())); if (o.getClosedAt()==null) ps.setNull(6,Types.TIMESTAMP); else ps.setTimestamp(6,Timestamp.valueOf(o.getClosedAt())); ps.setString(7,o.getStatus().name()); ps.setString(8,o.getPaymentMethod()); ps.setDouble(9,o.getTaxRate()); ps.setDouble(10,o.getServiceChargeRate()); ps.setString(11,o.getCustomerName()); ps.addBatch();
                int rowNo=1; for (OrderItem item: o.getItems()) { itemPs.setInt(1,o.getId()); itemPs.setInt(2,rowNo++); itemPs.setInt(3,item.getMenuItemId()); itemPs.setString(4,item.getName()); itemPs.setDouble(5,item.getUnitPrice()); itemPs.setInt(6,item.getQuantity()); itemPs.setString(7,item.getNote()); itemPs.addBatch(); }
            }
            ps.executeBatch(); itemPs.executeBatch();
        }
    }
    private void writeShifts(Connection conn, AppData data) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO shift_records(id,user_id,user_name,clock_in,clock_out) VALUES (?,?,?,?,?)")) { for (ShiftRecord s: data.getShiftRecords()) { ps.setInt(1,s.getId()); ps.setInt(2,s.getUserId()); ps.setString(3,s.getUserName()); ps.setTimestamp(4,Timestamp.valueOf(s.getClockIn())); if (s.getClockOut()==null) ps.setNull(5,Types.TIMESTAMP); else ps.setTimestamp(5,Timestamp.valueOf(s.getClockOut())); ps.addBatch(); } ps.executeBatch(); }
    }
    private void writeAuthHistory(Connection conn, AppData data) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO auth_history(id,user_id,full_name,username,role_name,login_at,logout_at) VALUES (?,?,?,?,?,?,?)")) { for (AuthHistoryRecord r: data.getAuthHistoryRecords()) { ps.setInt(1,r.getId()); ps.setInt(2,r.getUserId()); ps.setString(3,r.getFullName()); ps.setString(4,r.getUsername()); ps.setString(5,r.getRoleName()); ps.setTimestamp(6,Timestamp.valueOf(r.getLoginAt())); if (r.getLogoutAt()==null) ps.setNull(7,Types.TIMESTAMP); else ps.setTimestamp(7,Timestamp.valueOf(r.getLogoutAt())); ps.addBatch(); } ps.executeBatch(); }
    }
}
