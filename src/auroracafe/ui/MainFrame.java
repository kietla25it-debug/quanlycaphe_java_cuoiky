package auroracafe.ui;

import auroracafe.model.AppSettings;
import auroracafe.model.AuthHistoryRecord;
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
import auroracafe.service.AuthService;
import auroracafe.service.CafeService;
import auroracafe.util.CardPanel;
import auroracafe.util.LuxuryDashboardHeroPanel;
import auroracafe.util.LuxurySidebarBrandPanel;
import auroracafe.util.CoffeeArtPanel;
import auroracafe.util.FormatUtils;
import auroracafe.util.RemoteImageCache;
import auroracafe.util.RoundBorder;
import auroracafe.util.RoundButton;
import auroracafe.util.UiTheme;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class MainFrame extends JFrame {
    private final AuthService authService;
    private final CafeService cafeService;
    private final User currentUser;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final JLabel lblHeaderTitle = new JLabel();
    private final JLabel lblHeaderSummary = new JLabel();
    private final JLabel lblCurrentUser = new JLabel();

    private JLabel lblAttendanceStatus;
    private JLabel lblAttendanceHours;
    private JLabel lblSideShiftTime;
    private JLabel lblAttendancePanelStatus;
    private JLabel lblAttendancePanelOpenShift;
    private JLabel lblAttendancePanelHours;
    private JButton btnClockAction;
    private JButton btnClockActionBig;

    private JLabel lblStatsTablesBusy;
    private JLabel lblStatsOrdersToday;
    private JLabel lblStatsTablesAvailable;
    private JLabel lblStatsRevenueToday;
    private JLabel lblStatsMenuCount;
    private JLabel lblStatsCupsToday;
    private DefaultTableModel modelRecentOrders;
    private DefaultTableModel modelTopItems;

    private JPanel tablesGridPanel;

    private DefaultTableModel modelMenu;
    private JTable tblMenu;
    private JTextField txtMenuSearch;
    private JComboBox<String> cboMenuCategory;
    private JTextField txtMenuName;
    private JTextField txtMenuCategoryInput;
    private JTextField txtMenuPrice;
    private JTextArea txtMenuDescription;
    private JCheckBox chkMenuAvailable;
    private JTextField txtMenuImagePath;
    private JLabel lblMenuImagePreview;
    private JButton btnMenuAdd;
    private JButton btnMenuDelete;

    private JComboBox<CafeTable> cboOrderTable;
    private JTextField txtOrderCustomer;
    private JComboBox<String> cboPaymentMethod;
    private JLabel lblOrderInfo;
    private DefaultTableModel modelOrderMenu;
    private JTable tblOrderMenu;
    private JTextField txtOrderSearch;
    private JSpinner spnQuantity;
    private JTextField txtItemNote;
    private DefaultTableModel modelCart;
    private JTable tblCart;
    private JLabel lblOrderSubtotal;
    private JLabel lblOrderTax;
    private JLabel lblOrderService;
    private JLabel lblOrderTotal;
    private JPanel featuredMenuPanel;
    private JPanel menuGalleryPanel;
    private final Map<String, RoundButton> navButtons = new LinkedHashMap<>();
    private String activeNavKey = "dashboard";
    private int currentOpenOrderId = -1;
    private boolean syncingOrderCombo;

    private DefaultTableModel modelReports;
    private JTable tblReports;
    private JTextField txtReportSearch;
    private JTextField txtReportFrom;
    private JTextField txtReportTo;
    private JTextArea txtInvoicePreview;
    private DefaultTableModel modelDailyStats;
    private JLabel lblMonthRevenueSummary;
    private DefaultTableModel modelShiftRecords;
    private DefaultTableModel modelShiftRecordsDetail;
    private DefaultTableModel modelAuthHistory;
    private DefaultTableModel modelMonthlyStaffSummary;

    private JTextField txtBizName;
    private JTextField txtBizSlogan;
    private JTextField txtTaxRate;
    private JTextField txtServiceRate;
    private JTextArea txtInvoiceFooter;
    private DefaultTableModel modelUsers;
    private JButton btnSaveSettings;
    private JTextField txtStaffName;
    private JTextField txtStaffUser;
    private JTextField txtStaffPass;
    private JComboBox<Role> cboStaffRole;

    public MainFrame(AuthService authService, CafeService cafeService, User currentUser) {
        this.authService = authService;
        this.cafeService = cafeService;
        this.currentUser = currentUser;
        initUi();
        refreshAll();
    }

    private void initUi() {
        setTitle(brandName() + " - Desktop Management");
        setSize(1520, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UiTheme.BG);

        add(createSidebar(), BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(createHeader(), BorderLayout.NORTH);
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        contentPanel.add(createDashboardPanel(), "dashboard");
        contentPanel.add(createTablesPanel(), "tables");
        contentPanel.add(createMenuPanel(), "menu");
        contentPanel.add(createOrdersPanel(), "orders");
        contentPanel.add(createReportsPanel(), "reports");
        contentPanel.add(createAttendancePanel(), "attendance");
        contentPanel.add(createSettingsPanel(), "settings");
        center.add(contentPanel, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        showCard("dashboard", "Tổng quan vận hành quán");
    }

    private String brandName() {
        return cafeService.getData().getSettings().getBusinessName();
    }

    private JPanel createSidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(304, 0));
        side.setBackground(new Color(23, 13, 8));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        LuxurySidebarBrandPanel brandPanel = new LuxurySidebarBrandPanel(brandName(), currentUser.getRole() == Role.ADMIN ? "Quản lý hệ thống" : "Nhân viên phục vụ");
        brandPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(brandPanel);
        side.add(Box.createVerticalStrut(14));

        CardPanel userCard = new CardPanel();
        userCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        userCard.setBackground(new Color(255, 250, 244));
        userCard.setLayout(new BoxLayout(userCard, BoxLayout.Y_AXIS));
        JLabel userTitle = new JLabel("Nhân viên đang đăng nhập");
        userTitle.setFont(UiTheme.SUBTITLE);
        userTitle.setForeground(UiTheme.PRIMARY_DARK);
        JLabel userName = new JLabel(currentUser.getFullName());
        userName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        userName.setForeground(UiTheme.PRIMARY_DARK);
        lblAttendanceStatus = new JLabel();
        lblAttendanceStatus.setFont(UiTheme.BODY);
        lblAttendanceStatus.setForeground(UiTheme.PRIMARY_DARK);
        lblAttendanceHours = new JLabel();
        lblAttendanceHours.setFont(UiTheme.BODY);
        lblAttendanceHours.setForeground(UiTheme.MUTED);
        lblSideShiftTime = new JLabel();
        lblSideShiftTime.setFont(UiTheme.SMALL);
        lblSideShiftTime.setForeground(UiTheme.MUTED);
        userCard.add(userTitle);
        userCard.add(Box.createVerticalStrut(8));
        userCard.add(userName);
        userCard.add(Box.createVerticalStrut(10));
        userCard.add(lblAttendanceStatus);
        userCard.add(Box.createVerticalStrut(4));
        userCard.add(lblAttendanceHours);
        userCard.add(Box.createVerticalStrut(4));
        userCard.add(lblSideShiftTime);
        side.add(userCard);
        side.add(Box.createVerticalStrut(16));

        side.add(createNavButton("dashboard", "🏠  Tổng quan", () -> showCard("dashboard", "Tổng quan vận hành quán")));
        side.add(Box.createVerticalStrut(8));
        side.add(createNavButton("tables", "🪑  Quản lý bàn", () -> showCard("tables", "Chọn bàn & order nhanh")));
        side.add(Box.createVerticalStrut(8));
        side.add(createNavButton("menu", "📋  Menu", () -> showCard("menu", "Thực đơn sáng tạo")));
        side.add(Box.createVerticalStrut(8));
        side.add(createNavButton("orders", "🛒  Order", () -> showCard("orders", "Order & thanh toán nhanh")));
        side.add(Box.createVerticalStrut(8));
        side.add(createNavButton("reports", "📈  Báo cáo", () -> showCard("reports", "Doanh thu ngày, tháng & hóa đơn")));
        side.add(Box.createVerticalStrut(8));
        side.add(createNavButton("attendance", "🖐️  Chấm công", () -> showCard("attendance", "Chấm công, ca làm hôm nay & lịch sử đăng nhập")));
        side.add(Box.createVerticalStrut(8));
        side.add(createNavButton("settings", "⚙️  Cài đặt", () -> showCard("settings", "Cài đặt hệ thống & tài khoản")));
        side.add(Box.createVerticalGlue());
        side.add(createLogoutButton());
        return side;
    }

    private JButton createNavButton(String key, String text, Runnable action) {
        RoundButton btn = new RoundButton(text, new Color(75, 48, 27), new Color(119, 76, 35), new Color(192, 138, 61));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setForeground(Color.WHITE);
        btn.putClientProperty("navKey", key);
        navButtons.put(key, btn);
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private JButton createLogoutButton() {
        RoundButton btn = new RoundButton("⏻  Đăng xuất", new Color(170, 49, 49), new Color(194, 74, 74));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setForeground(Color.WHITE);
        btn.addActionListener(e -> logout());
        return btn;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 251, 246));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        lblHeaderTitle.setFont(new Font("Serif", Font.BOLD, 27));
        lblHeaderTitle.setForeground(UiTheme.TEXT);
        lblHeaderSummary.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblHeaderSummary.setForeground(UiTheme.MUTED);
        left.add(lblHeaderTitle);
        left.add(Box.createVerticalStrut(6));
        left.add(lblHeaderSummary);

        lblCurrentUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCurrentUser.setForeground(UiTheme.TEXT);
        lblCurrentUser.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(left, BorderLayout.WEST);
        header.add(lblCurrentUser, BorderLayout.EAST);
        return header;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        JPanel topRow = new JPanel(new GridLayout(1, 2, 16, 16));
        topRow.setOpaque(false);
        topRow.add(createDashboardHero());
        topRow.add(createQuickInsightsPanel());

        JPanel cards = new JPanel(new GridLayout(1, 6, 16, 16));
        cards.setOpaque(false);
        lblStatsTablesBusy = createStatsValue();
        lblStatsTablesAvailable = createStatsValue();
        lblStatsOrdersToday = createStatsValue();
        lblStatsRevenueToday = createStatsValue();
        lblStatsMenuCount = createStatsValue();
        lblStatsCupsToday = createStatsValue();
        cards.add(createStatCard("Bàn đang phục vụ", lblStatsTablesBusy, UiTheme.WARNING));
        cards.add(createStatCard("Bàn trống", lblStatsTablesAvailable, UiTheme.INFO));
        cards.add(createStatCard("Hóa đơn hôm nay", lblStatsOrdersToday, UiTheme.ACCENT));
        cards.add(createStatCard("Doanh thu hôm nay", lblStatsRevenueToday, UiTheme.ACCENT));
        cards.add(createStatCard("Món trong menu", lblStatsMenuCount, UiTheme.GOLD));
        cards.add(createStatCard("Số ly hôm nay", lblStatsCupsToday, UiTheme.PRIMARY));

        CardPanel recent = new CardPanel();
        recent.setLayout(new BorderLayout(0, 12));
        JLabel title = new JLabel("Đơn hàng gần nhất");
        title.setFont(UiTheme.SUBTITLE);
        recent.add(title, BorderLayout.NORTH);
        modelRecentOrders = new DefaultTableModel(new Object[]{"Mã", "Bàn", "Khách", "Trạng thái", "Tổng", "Thời gian"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        recent.add(new JScrollPane(createTable(modelRecentOrders)), BorderLayout.CENTER);

        CardPanel topItems = new CardPanel();
        topItems.setLayout(new BorderLayout(0, 12));
        JLabel lblTop = new JLabel("Top món bán chạy hôm nay");
        lblTop.setFont(UiTheme.SUBTITLE);
        topItems.add(lblTop, BorderLayout.NORTH);
        modelTopItems = new DefaultTableModel(new Object[]{"Món", "Số ly"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        topItems.add(new JScrollPane(createTable(modelTopItems)), BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 16));
        bottomRow.setOpaque(false);
        bottomRow.add(recent);
        bottomRow.add(topItems);

        JPanel south = new JPanel(new BorderLayout(0, 16));
        south.setOpaque(false);
        south.add(cards, BorderLayout.NORTH);
        south.add(bottomRow, BorderLayout.CENTER);

        panel.add(topRow, BorderLayout.NORTH);
        panel.add(south, BorderLayout.CENTER);
        return panel;
    }

    private CardPanel createDashboardHero() {
        CardPanel heroCard = new CardPanel();
        heroCard.setLayout(new BorderLayout());
        heroCard.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(28, new Color(193, 153, 82)),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        LuxuryDashboardHeroPanel art = new LuxuryDashboardHeroPanel();
        art.setPreferredSize(new Dimension(0, 230));
        heroCard.add(art, BorderLayout.CENTER);
        return heroCard;
    }

    private CardPanel createQuickInsightsPanel() {
        CardPanel panel = new CardPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Tác vụ nhanh");
        title.setFont(UiTheme.SUBTITLE);
        panel.add(title);
        panel.add(Box.createVerticalStrut(12));
        panel.add(createActionButton("Xem doanh thu hôm nay", UiTheme.ACCENT, e -> showRevenueReportWindow()));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createActionButton("Mở màn hình order", new Color(96, 62, 31), e -> showCard("orders", "Order & thanh toán nhanh")));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createActionButton("Chọn bàn nhanh", new Color(208, 146, 28), e -> showCard("tables", "Chọn bàn & order nhanh")));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createActionButton("Xem ca làm hôm nay", new Color(22, 88, 170), e -> showCard("attendance", "Chấm công, ca làm hôm nay & lịch sử đăng nhập")));
        panel.add(Box.createVerticalStrut(14));
        JLabel tip = new JLabel("Mẹo: chọn bàn là order tự mở, món thêm nhanh và có thể ghi chú riêng cho từng món.");
        tip.setFont(UiTheme.BODY);
        tip.setForeground(UiTheme.MUTED);
        tip.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(tip);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accent) {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel icon = new JLabel(statIcon(title));
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        icon.setForeground(accent);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UiTheme.BODY);
        lblTitle.setForeground(UiTheme.MUTED);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setForeground(accent);
        card.add(icon);
        card.add(Box.createVerticalStrut(6));
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        return card;
    }

    private String statIcon(String title) {
        String t = title.toLowerCase();
        if (t.contains("phục vụ")) return "🪑";
        if (t.contains("trống")) return "💺";
        if (t.contains("hóa đơn")) return "🧾";
        if (t.contains("doanh thu")) return "💰";
        if (t.contains("menu")) return "🍽";
        if (t.contains("ly")) return "☕";
        return "✨";
    }

    private JLabel createStatsValue() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("Segoe UI", Font.BOLD, 26));
        return label;
    }

    private JPanel createTablesPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setOpaque(false);

        CardPanel info = new CardPanel();
        info.setLayout(new BorderLayout());
        JLabel msg = new JLabel("Chọn một bàn bên dưới là hệ thống tự mở order ngay. Khi thanh toán xong, bàn sẽ tự chuyển về trạng thái bàn trống.");
        msg.setFont(UiTheme.BODY);
        info.add(msg, BorderLayout.WEST);
        root.add(info, BorderLayout.NORTH);

        tablesGridPanel = new JPanel(new GridLayout(0, 4, 16, 16));
        tablesGridPanel.setOpaque(false);
        JScrollPane scroll = new JScrollPane(tablesGridPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        root.add(scroll, BorderLayout.CENTER);
        return root;
    }

    private JPanel createMenuPanel() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setOpaque(false);

        CardPanel left = new CardPanel();
        left.setLayout(new BorderLayout(0, 12));
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.setOpaque(false);
        txtMenuSearch = new JTextField(18);
        styleField(txtMenuSearch);
        cboMenuCategory = new JComboBox<>();
        styleCombo(cboMenuCategory);
        JButton btnSearch = createActionButton("Lọc menu", UiTheme.PRIMARY);
        btnSearch.addActionListener(e -> refreshMenuTable());
        filters.add(new JLabel("Tìm kiếm:"));
        filters.add(txtMenuSearch);
        filters.add(new JLabel("Danh mục:"));
        filters.add(cboMenuCategory);
        filters.add(btnSearch);
        left.add(filters, BorderLayout.NORTH);

        modelMenu = new DefaultTableModel(new Object[]{"ID", "Tên món", "Danh mục", "Giá", "Sẵn sàng", "Mô tả"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblMenu = createTable(modelMenu);
        tblMenu.getSelectionModel().addListSelectionListener(e -> fillMenuFormFromSelection());
        left.add(new JScrollPane(tblMenu), BorderLayout.CENTER);

        CardPanel form = new CardPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(440, 0));
        JLabel heading = sectionLabel("Thông tin món");
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(heading);
        form.add(Box.createVerticalStrut(8));

        JPanel managerActions = new JPanel(new GridLayout(3, 2, 10, 10));
        managerActions.setOpaque(false);
        btnMenuAdd = createActionButton("Thêm món mới", UiTheme.ACCENT);
        JButton btnUpdate = createActionButton("Cập nhật món", UiTheme.PRIMARY);
        btnMenuDelete = createActionButton("Xóa món", UiTheme.DANGER);
        JButton btnClear = createActionButton("Làm mới", UiTheme.WARNING);
        JButton btnImportCsv = createActionButton("Import CSV", UiTheme.INFO);
        JButton btnExportCsv = createActionButton("Export CSV", UiTheme.ACCENT);
        btnMenuAdd.addActionListener(e -> openAddMenuDialog());
        btnUpdate.addActionListener(e -> updateMenuItem());
        btnMenuDelete.addActionListener(e -> deleteMenuItem());
        btnClear.addActionListener(e -> clearMenuForm());
        btnImportCsv.addActionListener(e -> importMenuCsv());
        btnExportCsv.addActionListener(e -> exportMenuCsv());
        managerActions.add(btnMenuAdd);
        managerActions.add(btnUpdate);
        managerActions.add(btnMenuDelete);
        managerActions.add(btnClear);
        managerActions.add(btnImportCsv);
        managerActions.add(btnExportCsv);
        form.add(managerActions);
        form.add(Box.createVerticalStrut(10));

        txtMenuName = new JTextField(); styleField(txtMenuName);
        txtMenuCategoryInput = new JTextField(); styleField(txtMenuCategoryInput);
        txtMenuPrice = new JTextField(); styleField(txtMenuPrice);
        txtMenuDescription = new JTextArea(3, 20); styleArea(txtMenuDescription);
        chkMenuAvailable = new JCheckBox("Đang phục vụ"); chkMenuAvailable.setOpaque(false); chkMenuAvailable.setSelected(true);
        txtMenuImagePath = new JTextField(); styleField(txtMenuImagePath); txtMenuImagePath.setEditable(false);
        lblMenuImagePreview = new JLabel();
        lblMenuImagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblMenuImagePreview.setPreferredSize(new Dimension(0, 108));
        lblMenuImagePreview.setOpaque(true);
        lblMenuImagePreview.setBackground(new Color(250,246,242));
        lblMenuImagePreview.setBorder(BorderFactory.createCompoundBorder(new RoundBorder(18, UiTheme.BORDER), BorderFactory.createEmptyBorder(8,8,8,8)));

        form.add(formRow("Tên món", txtMenuName));
        form.add(formRow("Danh mục", txtMenuCategoryInput));
        form.add(formRow("Giá (VND)", txtMenuPrice));
        form.add(formRow("Mô tả", new JScrollPane(txtMenuDescription)));
        form.add(formRow("Ảnh món (tùy chọn)", txtMenuImagePath));
        JButton btnChooseImage = createActionButton("Thêm / đổi ảnh món", UiTheme.INFO);
        btnChooseImage.addActionListener(e -> chooseMenuImage());
        JButton btnClearImage = createActionButton("Không thêm ảnh", UiTheme.WARNING);
        btnClearImage.addActionListener(e -> { txtMenuImagePath.setText(""); updateMenuPreview(""); });
        JPanel imgBtns = new JPanel(new GridLayout(1,2,10,0)); imgBtns.setOpaque(false); imgBtns.add(btnChooseImage); imgBtns.add(btnClearImage);
        form.add(imgBtns);
        form.add(Box.createVerticalStrut(8));
        form.add(lblMenuImagePreview);
        form.add(Box.createVerticalStrut(8));
        form.add(chkMenuAvailable);
        form.add(Box.createVerticalGlue());

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.getViewport().setOpaque(false);
        formScroll.setOpaque(false);
        formScroll.setPreferredSize(new Dimension(450, 0));

        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, formScroll);
        topSplit.setBorder(BorderFactory.createEmptyBorder());
        topSplit.setResizeWeight(0.66);

        CardPanel galleryCard = new CardPanel();
        galleryCard.setLayout(new BorderLayout(0, 12));
        JLabel galleryTitle = new JLabel("Hình ảnh menu");
        galleryTitle.setFont(UiTheme.SUBTITLE);
        JLabel galleryHint = new JLabel("Hình ảnh sắc nét, tên món rõ ràng và đồng bộ với phần order.");
        galleryHint.setFont(UiTheme.BODY);
        galleryHint.setForeground(UiTheme.MUTED);
        JPanel galleryHeader = new JPanel(); galleryHeader.setOpaque(false); galleryHeader.setLayout(new BoxLayout(galleryHeader, BoxLayout.Y_AXIS));
        galleryHeader.add(galleryTitle); galleryHeader.add(Box.createVerticalStrut(4)); galleryHeader.add(galleryHint);
        galleryCard.add(galleryHeader, BorderLayout.NORTH);

        menuGalleryPanel = new JPanel(new GridLayout(0, 3, 12, 12));
        menuGalleryPanel.setOpaque(false);
        JScrollPane galleryScroll = new JScrollPane(menuGalleryPanel);
        galleryScroll.setBorder(BorderFactory.createEmptyBorder());
        galleryScroll.getVerticalScrollBar().setUnitIncrement(16);
        galleryScroll.getViewport().setOpaque(false);
        galleryScroll.setOpaque(false);
        galleryCard.add(galleryScroll, BorderLayout.CENTER);

        JSplitPane vertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, galleryCard);
        vertical.setBorder(BorderFactory.createEmptyBorder());
        vertical.setResizeWeight(0.68);

        root.add(vertical, BorderLayout.CENTER);
        return root;
    }

    private JPanel createOrdersPanel() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setOpaque(false);

        JPanel left = new JPanel(new BorderLayout(0, 16));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(660, 0));

        CardPanel orderInfo = new CardPanel();
        orderInfo.setLayout(new BoxLayout(orderInfo, BoxLayout.Y_AXIS));
        lblOrderInfo = new JLabel("Chưa chọn bàn");
        lblOrderInfo.setFont(UiTheme.SUBTITLE);
        lblOrderInfo.setForeground(UiTheme.TEXT);
        cboOrderTable = new JComboBox<>(); styleCombo(cboOrderTable);
        cboOrderTable.addActionListener(e -> onOrderTableChanged());
        txtOrderCustomer = new JTextField(); styleField(txtOrderCustomer);
        cboPaymentMethod = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản", "Thẻ"}); styleCombo(cboPaymentMethod);
        JButton btnOpenOrder = createActionButton("Mở / đồng bộ order", UiTheme.PRIMARY);
        btnOpenOrder.addActionListener(e -> openOrResumeOrder());
        JButton btnSelectMore = createActionButton("Gọi món bằng danh sách", UiTheme.INFO);
        btnSelectMore.addActionListener(e -> openMenuSelectionDialog());
        orderInfo.add(lblOrderInfo);
        orderInfo.add(Box.createVerticalStrut(10));
        orderInfo.add(formRow("Chọn bàn", cboOrderTable));
        orderInfo.add(formRow("Tên khách", txtOrderCustomer));
        orderInfo.add(formRow("Thanh toán", cboPaymentMethod));
        JPanel orderBtns = new JPanel(new GridLayout(1, 2, 10, 0));
        orderBtns.setOpaque(false);
        orderBtns.add(btnOpenOrder);
        orderBtns.add(btnSelectMore);
        orderInfo.add(orderBtns);

        CardPanel quickPickCard = new CardPanel();
        quickPickCard.setLayout(new BorderLayout(0, 12));
        JLabel quickTitle = new JLabel("Chọn món nhanh");
        quickTitle.setFont(UiTheme.SUBTITLE);
        JLabel quickHint = new JLabel("Ảnh món được co giãn vừa khung để không bị cắt mất hình, bên dưới vẫn còn đủ nút thao tác.");
        quickHint.setFont(UiTheme.BODY);
        quickHint.setForeground(UiTheme.MUTED);
        JPanel quickHeader = new JPanel();
        quickHeader.setOpaque(false);
        quickHeader.setLayout(new BoxLayout(quickHeader, BoxLayout.Y_AXIS));
        quickHeader.add(quickTitle);
        quickHeader.add(Box.createVerticalStrut(4));
        quickHeader.add(quickHint);
        quickPickCard.add(quickHeader, BorderLayout.NORTH);
        featuredMenuPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        featuredMenuPanel.setOpaque(false);
        quickPickCard.add(featuredMenuPanel, BorderLayout.CENTER);

        CardPanel menuCard = new CardPanel();
        menuCard.setLayout(new BorderLayout(0, 12));
        txtOrderSearch = new JTextField();
        JLabel orderMenuHint = new JLabel("Danh sách món của quán • chọn món rồi thêm ghi chú nếu cần");
        orderMenuHint.setFont(UiTheme.BODY);
        orderMenuHint.setForeground(UiTheme.MUTED);
        menuCard.add(orderMenuHint, BorderLayout.NORTH);
        modelOrderMenu = new DefaultTableModel(new Object[]{"ID", "Tên", "Danh mục", "Giá", "Sẵn", "Mô tả"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblOrderMenu = createTable(modelOrderMenu);
        tblOrderMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblOrderMenu.getSelectedRow() >= 0) {
                    openSelectedMenuDialog();
                }
            }
        });
        JScrollPane orderMenuScroll = new JScrollPane(tblOrderMenu);
        orderMenuScroll.setPreferredSize(new Dimension(0, 250));
        menuCard.add(orderMenuScroll, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new GridLayout(0, 1, 8, 8));
        bottom.setOpaque(false);
        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        txtItemNote = new JTextField(); styleField(txtItemNote);
        JButton btnAddItem = createActionButton("Gọi món đã chọn", UiTheme.ACCENT);
        btnAddItem.addActionListener(e -> addSelectedMenuToOrder());
        bottom.add(formRow("Số lượng", spnQuantity));
        bottom.add(formRow("Ghi chú cho món", txtItemNote));
        bottom.add(btnAddItem);
        menuCard.add(bottom, BorderLayout.SOUTH);

        JPanel leftStack = new JPanel();
        leftStack.setOpaque(false);
        leftStack.setLayout(new BoxLayout(leftStack, BoxLayout.Y_AXIS));
        orderInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        quickPickCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftStack.add(orderInfo);
        leftStack.add(Box.createVerticalStrut(16));
        leftStack.add(quickPickCard);
        leftStack.add(Box.createVerticalStrut(16));
        leftStack.add(menuCard);

        JScrollPane leftScroll = new JScrollPane(leftStack);
        leftScroll.setBorder(BorderFactory.createEmptyBorder());
        leftScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        leftScroll.getVerticalScrollBar().setUnitIncrement(16);
        leftScroll.getViewport().setOpaque(false);
        leftScroll.setOpaque(false);
        left.add(leftScroll, BorderLayout.CENTER);

        CardPanel right = new CardPanel();
        right.setLayout(new BorderLayout(0, 12));
        JLabel cartTitle = new JLabel("Giỏ order hiện tại");
        cartTitle.setFont(UiTheme.SUBTITLE);
        right.add(cartTitle, BorderLayout.NORTH);
        modelCart = new DefaultTableModel(new Object[]{"ID", "Tên", "SL", "Đơn giá", "Ghi chú", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblCart = createTable(modelCart);
        right.add(new JScrollPane(tblCart), BorderLayout.CENTER);

        JPanel totals = new JPanel();
        totals.setOpaque(false);
        totals.setLayout(new BoxLayout(totals, BoxLayout.Y_AXIS));
        lblOrderSubtotal = new JLabel();
        lblOrderTax = new JLabel();
        lblOrderService = new JLabel();
        lblOrderTotal = new JLabel();
        lblOrderTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totals.add(lblOrderSubtotal); totals.add(Box.createVerticalStrut(4));
        totals.add(lblOrderTax); totals.add(Box.createVerticalStrut(4));
        totals.add(lblOrderService); totals.add(Box.createVerticalStrut(6));
        totals.add(lblOrderTotal); totals.add(Box.createVerticalStrut(12));

        JPanel cartActions = new JPanel(new GridLayout(1, 4, 10, 10));
        cartActions.setOpaque(false);
        JButton btnUpdateItem = createActionButton("Sửa món", UiTheme.WARNING);
        JButton btnRemoveItem = createActionButton("Xóa món", UiTheme.DANGER);
        JButton btnReleaseTable = createActionButton("Giải phóng bàn", UiTheme.INFO);
        JButton btnCheckout = createActionButton("Thanh toán", UiTheme.ACCENT);
        btnUpdateItem.addActionListener(e -> updateCartItem());
        btnRemoveItem.addActionListener(e -> removeCartItem());
        btnReleaseTable.addActionListener(e -> releaseCurrentTable());
        btnCheckout.addActionListener(e -> checkoutOrder());
        cartActions.add(btnUpdateItem); cartActions.add(btnRemoveItem); cartActions.add(btnReleaseTable); cartActions.add(btnCheckout);
        totals.add(cartActions);
        right.add(totals, BorderLayout.SOUTH);

        root.add(left, BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        return root;
    }

    private JPanel createReportsPanel() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setOpaque(false);

        CardPanel summary = new CardPanel();
        summary.setLayout(new BorderLayout(16, 0));
        summary.setPreferredSize(new Dimension(0, 168));

        JPanel summaryText = new JPanel();
        summaryText.setOpaque(false);
        summaryText.setLayout(new BoxLayout(summaryText, BoxLayout.Y_AXIS));
        JLabel summaryTitle = new JLabel("Trung tâm báo cáo doanh thu");
        summaryTitle.setFont(UiTheme.SUBTITLE);
        JLabel summaryLabel = new JLabel("Xem doanh thu ngày, tháng, chi phí ước tính, lợi nhuận ước tính, hóa đơn và báo cáo giờ làm nhân viên trong một màn hình chi tiết.");
        summaryLabel.setFont(UiTheme.BODY);
        summaryLabel.setForeground(UiTheme.MUTED);
        lblMonthRevenueSummary = new JLabel();
        lblMonthRevenueSummary.setFont(UiTheme.BODY);
        summaryText.add(summaryTitle);
        summaryText.add(Box.createVerticalStrut(6));
        summaryText.add(summaryLabel);
        summaryText.add(Box.createVerticalStrut(10));
        summaryText.add(lblMonthRevenueSummary);

        JPanel summaryActions = new JPanel();
        summaryActions.setOpaque(false);
        summaryActions.setLayout(new BoxLayout(summaryActions, BoxLayout.Y_AXIS));
        JButton btnToday = createActionButton("Xem doanh thu chi tiết", UiTheme.ACCENT);
        btnToday.setPreferredSize(new Dimension(250, 52));
        btnToday.setMaximumSize(new Dimension(250, 52));
        btnToday.addActionListener(e -> showRevenueReportWindow());
        JButton btnRefresh = createActionButton("Làm mới báo cáo", UiTheme.PRIMARY);
        btnRefresh.setPreferredSize(new Dimension(250, 52));
        btnRefresh.setMaximumSize(new Dimension(250, 52));
        btnRefresh.addActionListener(e -> refreshReports());
        summaryActions.add(Box.createVerticalGlue());
        summaryActions.add(btnToday);
        summaryActions.add(Box.createVerticalStrut(12));
        summaryActions.add(btnRefresh);
        summaryActions.add(Box.createVerticalGlue());

        summary.add(summaryActions, BorderLayout.WEST);
        summary.add(summaryText, BorderLayout.CENTER);
        root.add(summary, BorderLayout.NORTH);

        CardPanel left = new CardPanel();
        left.setLayout(new BorderLayout(0, 12));
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        filters.setOpaque(false);
        txtReportSearch = new JTextField(14); styleField(txtReportSearch);
        txtReportFrom = new JTextField(8); styleField(txtReportFrom);
        txtReportTo = new JTextField(8); styleField(txtReportTo);
        JButton btnFilter = createActionButton("Lọc báo cáo", UiTheme.PRIMARY);
        btnFilter.setPreferredSize(new Dimension(180, 42));
        btnFilter.addActionListener(e -> refreshReports());
        filters.add(new JLabel("Từ khóa:")); filters.add(txtReportSearch);
        filters.add(new JLabel("Từ ngày yyyy-MM-dd:")); filters.add(txtReportFrom);
        filters.add(new JLabel("Đến ngày yyyy-MM-dd:")); filters.add(txtReportTo);
        filters.add(btnFilter);
        left.add(filters, BorderLayout.NORTH);

        modelReports = new DefaultTableModel(new Object[]{"Mã", "Trạng thái", "Bàn", "Khách", "Nhân viên", "Tổng", "Ngày"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblReports = createTable(modelReports);
        tblReports.getSelectionModel().addListSelectionListener(e -> previewSelectedInvoice());
        left.add(new JScrollPane(tblReports), BorderLayout.CENTER);

        CardPanel bottomStats = new CardPanel();
        bottomStats.setLayout(new BorderLayout(0, 12));
        JLabel statsTitle = new JLabel("Thống kê bán hàng từng ngày");
        statsTitle.setFont(UiTheme.SUBTITLE);
        bottomStats.add(statsTitle, BorderLayout.NORTH);
        modelDailyStats = new DefaultTableModel(new Object[]{"Ngày", "Số hóa đơn", "Số ly", "Doanh thu"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        bottomStats.add(new JScrollPane(createTable(modelDailyStats)), BorderLayout.CENTER);

        JPanel leftSplit = new JPanel(new BorderLayout(0, 16));
        leftSplit.setOpaque(false);
        leftSplit.add(left, BorderLayout.CENTER);
        leftSplit.add(bottomStats, BorderLayout.SOUTH);
        bottomStats.setPreferredSize(new Dimension(0, 250));

        CardPanel right = new CardPanel();
        right.setLayout(new BorderLayout(0, 12));
        JLabel title = new JLabel("Xem nhanh hóa đơn");
        title.setFont(UiTheme.SUBTITLE);
        right.add(title, BorderLayout.NORTH);
        txtInvoicePreview = new JTextArea();
        txtInvoicePreview.setEditable(false);
        txtInvoicePreview.setFont(new Font("Consolas", Font.PLAIN, 13));
        right.add(new JScrollPane(txtInvoicePreview), BorderLayout.CENTER);
        JButton btnExportAgain = createActionButton("Xuất lại hóa đơn TXT", UiTheme.ACCENT);
        btnExportAgain.addActionListener(e -> exportSelectedInvoice());
        right.add(btnExportAgain, BorderLayout.SOUTH);
        right.setPreferredSize(new Dimension(470, 0));

        root.add(leftSplit, BorderLayout.CENTER);
        root.add(right, BorderLayout.EAST);
        return root;
    }

    private JPanel createAttendancePanel() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setOpaque(false);

        JPanel top = new JPanel(new GridLayout(1, 2, 16, 16));
        top.setOpaque(false);

        CardPanel infoCard = new CardPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Chấm công nhân viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(UiTheme.TEXT);
        lblAttendancePanelStatus = new JLabel();
        lblAttendancePanelStatus.setFont(UiTheme.SUBTITLE);
        lblAttendancePanelHours = new JLabel();
        lblAttendancePanelHours.setFont(UiTheme.BODY);
        lblAttendancePanelOpenShift = new JLabel();
        lblAttendancePanelOpenShift.setFont(UiTheme.BODY);
        lblAttendancePanelOpenShift.setForeground(UiTheme.MUTED);
        btnClockActionBig = createActionButton("Chấm công vào", UiTheme.ACCENT);
        btnClockActionBig.setPreferredSize(new Dimension(280, 54));
        btnClockActionBig.setMaximumSize(new Dimension(280, 54));
        btnClockActionBig.addActionListener(e -> toggleAttendance());
        infoCard.add(title);
        infoCard.add(Box.createVerticalStrut(12));
        infoCard.add(new JLabel("Tập trung chấm công, xem ca làm hôm nay, lịch sử đăng nhập và tổng hợp số giờ làm trong tháng."));
        infoCard.add(Box.createVerticalStrut(12));
        infoCard.add(lblAttendancePanelStatus);
        infoCard.add(Box.createVerticalStrut(8));
        infoCard.add(lblAttendancePanelHours);
        infoCard.add(Box.createVerticalStrut(6));
        infoCard.add(lblAttendancePanelOpenShift);
        infoCard.add(Box.createVerticalStrut(16));
        infoCard.add(btnClockActionBig);

        CardPanel noteCard = new CardPanel();
        noteCard.setLayout(new BorderLayout());
        CoffeeArtPanel art = new CoffeeArtPanel("Bảng điều khiển chấm công", "Giữ cố định 3 nút điều hướng để quản lý dễ chuyển giữa ca làm hôm nay, lịch sử đăng nhập và giờ làm trong tháng mà không bị mất nút.", true);
        art.setPreferredSize(new Dimension(0, 230));
        noteCard.add(art, BorderLayout.CENTER);

        top.add(infoCard);
        top.add(noteCard);

        JTabbedPane tabs = new JTabbedPane();

        CardPanel todayCard = new CardPanel();
        todayCard.setLayout(new BorderLayout());
        modelShiftRecords = new DefaultTableModel(new Object[]{"Ngày", "Nhân viên", "Vào làm", "Kết thúc ca", "Thời gian", "Trạng thái"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        todayCard.add(new JScrollPane(createTable(modelShiftRecords)), BorderLayout.CENTER);
        tabs.addTab("Ca làm hôm nay", todayCard);

        CardPanel authCard = new CardPanel();
        authCard.setLayout(new BorderLayout(0, 12));
        JLabel authTitle = new JLabel("Lịch sử đăng nhập / đăng xuất");
        authTitle.setFont(UiTheme.SUBTITLE);
        authCard.add(authTitle, BorderLayout.NORTH);
        modelAuthHistory = new DefaultTableModel(new Object[]{"Họ tên", "Tài khoản", "Quyền", "Đăng nhập", "Đăng xuất", "Trạng thái"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        authCard.add(new JScrollPane(createTable(modelAuthHistory)), BorderLayout.CENTER);
        tabs.addTab("Lịch sử tài khoản", authCard);

        CardPanel monthCard = new CardPanel();
        monthCard.setLayout(new BorderLayout(0, 12));
        JLabel monthTitle = new JLabel("Quản lý nhân viên làm trong tháng");
        monthTitle.setFont(UiTheme.SUBTITLE);
        JLabel monthHint = new JLabel("Theo dõi tháng/năm, số ngày làm và tổng số giờ làm cộng dồn của từng nhân viên.");
        monthHint.setForeground(UiTheme.MUTED);
        JPanel monthTop = new JPanel();
        monthTop.setOpaque(false);
        monthTop.setLayout(new BoxLayout(monthTop, BoxLayout.Y_AXIS));
        monthTop.add(monthTitle);
        monthTop.add(Box.createVerticalStrut(4));
        monthTop.add(monthHint);
        monthCard.add(monthTop, BorderLayout.NORTH);
        modelMonthlyStaffSummary = new DefaultTableModel(new Object[]{"Nhân viên", "Tài khoản", "Tháng/Năm", "Số ngày làm", "Tổng giờ", "Ca mở"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        monthCard.add(new JScrollPane(createTable(modelMonthlyStaffSummary)), BorderLayout.CENTER);
        tabs.addTab("Giờ làm trong tháng", monthCard);

        JPanel attendanceActions = new JPanel(new GridLayout(1, 3, 12, 0));
        attendanceActions.setOpaque(false);
        JButton btnTodayTab = createActionButton("Ca làm hôm nay", UiTheme.INFO);
        btnTodayTab.setPreferredSize(new Dimension(240, 56));
        btnTodayTab.addActionListener(e -> tabs.setSelectedIndex(0));
        JButton btnAuthTab = createActionButton("Lịch sử đăng nhập", UiTheme.PRIMARY);
        btnAuthTab.setPreferredSize(new Dimension(240, 56));
        btnAuthTab.addActionListener(e -> tabs.setSelectedIndex(1));
        JButton btnMonthTab = createActionButton("Giờ làm trong tháng", UiTheme.ACCENT);
        btnMonthTab.setPreferredSize(new Dimension(240, 56));
        btnMonthTab.addActionListener(e -> tabs.setSelectedIndex(2));
        attendanceActions.add(btnTodayTab);
        attendanceActions.add(btnAuthTab);
        attendanceActions.add(btnMonthTab);

        JPanel bottom = new JPanel(new BorderLayout(0, 12));
        bottom.setOpaque(false);
        bottom.add(attendanceActions, BorderLayout.NORTH);
        bottom.add(tabs, BorderLayout.CENTER);

        root.add(top, BorderLayout.NORTH);
        root.add(bottom, BorderLayout.CENTER);
        return root;
    }

    private JPanel createShiftHistoryPanel() { JPanel root = new JPanel(); root.setOpaque(false); return root; }

    private JPanel createAuthHistoryPanel() { JPanel root = new JPanel(); root.setOpaque(false); return root; }

    private JPanel createSettingsPanel() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setOpaque(false);

        CardPanel left = new CardPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(sectionLabel("Thông tin thương hiệu"));
        txtBizName = new JTextField(); styleField(txtBizName);
        txtBizSlogan = new JTextField(); styleField(txtBizSlogan);
        txtTaxRate = new JTextField(); styleField(txtTaxRate);
        txtServiceRate = new JTextField(); styleField(txtServiceRate);
        txtInvoiceFooter = new JTextArea(5, 20); styleArea(txtInvoiceFooter);
        left.add(formRow("Tên quán", txtBizName));
        left.add(formRow("Slogan", txtBizSlogan));
        left.add(formRow("Thuế (vd 0.08)", txtTaxRate));
        left.add(formRow("Phí dịch vụ (vd 0.05)", txtServiceRate));
        left.add(formRow("Footer hóa đơn", new JScrollPane(txtInvoiceFooter)));
        btnSaveSettings = createActionButton("Lưu cài đặt", UiTheme.PRIMARY);
        btnSaveSettings.addActionListener(e -> saveSettings());
        left.add(btnSaveSettings);

        CardPanel right = new CardPanel();
        right.setLayout(new BorderLayout(0, 12));
        right.add(sectionLabel("Quản lý nhân viên"), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Danh sách", createUsersListTab());
        tabs.addTab("Tạo tài khoản", createUsersCreateTab());
        if (!cafeService.canManageUsers(currentUser)) {
            tabs.setEnabledAt(1, false);
        }
        right.add(tabs, BorderLayout.CENTER);
        right.setPreferredSize(new Dimension(430, 0));

        root.add(left, BorderLayout.CENTER);
        root.add(right, BorderLayout.EAST);
        return root;
    }

    private JPanel createUsersListTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        modelUsers = new DefaultTableModel(new Object[]{"ID", "Họ tên", "Username", "Role", "Trạng thái", "Giờ hôm nay"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        panel.add(new JScrollPane(createTable(modelUsers)), BorderLayout.CENTER);
        return panel;
    }

    private String roleDisplayName(Role role) {
        return role == Role.ADMIN ? "QUẢN LÝ" : "NHÂN VIÊN";
    }

    private JPanel createUsersCreateTab() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        txtStaffName = new JTextField(); styleField(txtStaffName);
        txtStaffUser = new JTextField(); styleField(txtStaffUser);
        txtStaffPass = new JTextField(); styleField(txtStaffPass);
        cboStaffRole = new JComboBox<>(Role.values()); styleCombo(cboStaffRole);
        panel.add(formRow("Họ tên", txtStaffName));
        panel.add(formRow("Username", txtStaffUser));
        panel.add(formRow("Mật khẩu", txtStaffPass));
        panel.add(formRow("Vai trò", cboStaffRole));
        JButton btnCreate = createActionButton("Tạo tài khoản", UiTheme.ACCENT);
        btnCreate.addActionListener(e -> createStaffAccount());
        panel.add(btnCreate);
        return panel;
    }

    private void showCard(String key, String title) {
        cardLayout.show(contentPanel, key);
        activeNavKey = key;
        updateNavSelection();
        lblHeaderTitle.setText(title);
        lblHeaderSummary.setText(cafeService.summaryStats());
        lblCurrentUser.setText(currentUser.getFullName() + " • " + roleDisplayName(currentUser.getRole()));
        if ("dashboard".equals(key) && lblStatsTablesBusy != null) {
            refreshDashboard();
        }
    }

    private void updateNavSelection() {
        for (Map.Entry<String, RoundButton> entry : navButtons.entrySet()) {
            entry.getValue().setSelectedStyle(entry.getKey().equals(activeNavKey));
        }
    }

    private void refreshAll() {
        refreshHeader();
        refreshAttendance();
        refreshDashboard();
        refreshTablesPanel();
        refreshMenuCategories();
        refreshMenuTable();
        refreshOrderTableCombo();
        refreshFeaturedMenuButtons();
        refreshOrderMenuTable();
        refreshCurrentOrderView();
        refreshReports();
        refreshSettings();
        refreshUsersTable();
        refreshShiftRecords();
        refreshAuthHistory();
        refreshMonthlyStaffSummary();
        updateMenuPermissions();
        setTitle(brandName() + " - Desktop Management");
    }

    private void updateMenuPermissions() {
        boolean manager = currentUser != null && currentUser.getRole() == Role.ADMIN;
        if (btnMenuAdd != null) btnMenuAdd.setEnabled(manager);
        if (btnMenuDelete != null) btnMenuDelete.setEnabled(manager);
    }

    private void refreshHeader() {
        lblHeaderSummary.setText(cafeService.summaryStats());
        lblCurrentUser.setText(currentUser.getFullName() + " • " + roleDisplayName(currentUser.getRole()));
    }

    private void refreshAttendance() {
        boolean working = cafeService.isClockedIn(currentUser);
        ShiftRecord open = cafeService.getOpenShift(currentUser);
        String statusText = working ? "Trạng thái: đang làm việc" : "Trạng thái: chưa chấm công";
        String hoursText = "Số giờ hôm nay: " + FormatUtils.hours(cafeService.getTodayHoursForUser(currentUser));
        String shiftText = working && open != null
                ? "Bắt đầu ca lúc: " + FormatUtils.time(open.getClockIn())
                : "Hiện chưa có ca làm đang mở";

        if (lblAttendanceStatus != null) lblAttendanceStatus.setText(statusText);
        if (lblAttendanceHours != null) lblAttendanceHours.setText(hoursText);
        if (lblSideShiftTime != null) lblSideShiftTime.setText(shiftText);
        if (lblAttendancePanelStatus != null) lblAttendancePanelStatus.setText(statusText);
        if (lblAttendancePanelHours != null) lblAttendancePanelHours.setText(hoursText);
        if (lblAttendancePanelOpenShift != null) lblAttendancePanelOpenShift.setText(shiftText);

        String actionText = working ? "Chấm công ra / kết thúc ca" : "Chấm công vào / bắt đầu ca";
        Color actionColor = working ? UiTheme.WARNING : UiTheme.ACCENT;
        if (btnClockAction != null) applyButtonStyle(btnClockAction, actionText, actionColor);
        if (btnClockActionBig != null) applyButtonStyle(btnClockActionBig, actionText, actionColor);
    }

    private void refreshDashboard() {
        long busy = cafeService.getOccupiedTableCount();
        long available = cafeService.getAvailableTableCount();
        DailySalesStats today = cafeService.getTodaySalesStats();
        lblStatsTablesBusy.setText(String.valueOf(busy));
        if (lblStatsTablesAvailable != null) lblStatsTablesAvailable.setText(String.valueOf(available));
        lblStatsOrdersToday.setText(String.valueOf(today.getPaidOrders()));
        lblStatsRevenueToday.setText(FormatUtils.money(today.getRevenue()));
        lblStatsMenuCount.setText(String.valueOf(cafeService.getData().getMenuItems().size()));
        lblStatsCupsToday.setText(String.valueOf(today.getCupsSold()));

        modelRecentOrders.setRowCount(0);
        for (Order order : cafeService.getOrders().stream().limit(10).toList()) {
            modelRecentOrders.addRow(new Object[]{order.getId(), order.getTableName(), order.getCustomerName(), order.getStatus(), FormatUtils.money(order.getTotal()), FormatUtils.dateTime(order.getCreatedAt())});
        }

        modelTopItems.setRowCount(0);
        Map<String, Integer> topItems = cafeService.getTopSellingItemsToday();
        if (topItems.isEmpty()) {
            modelTopItems.addRow(new Object[]{"Chưa có dữ liệu hôm nay", 0});
        } else {
            topItems.forEach((name, qty) -> modelTopItems.addRow(new Object[]{name, qty}));
        }
    }

    private void refreshTablesPanel() {
        tablesGridPanel.removeAll();
        for (CafeTable table : cafeService.getTables()) {
            CardPanel card = new CardPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            JLabel name = new JLabel(table.getName());
            name.setFont(new Font("Segoe UI", Font.BOLD, 20));
            name.setForeground(UiTheme.TEXT);
            JLabel seats = new JLabel(table.getSeats() + " ghế");
            seats.setFont(UiTheme.BODY);
            seats.setForeground(UiTheme.MUTED);
            JLabel status = new JLabel(statusText(table.getStatus()));
            status.setOpaque(true);
            status.setForeground(Color.WHITE);
            status.setBackground(statusColor(table.getStatus()));
            status.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            status.setAlignmentX(Component.LEFT_ALIGNMENT);
            JButton btn = createActionButton(table.getStatus() == TableStatus.OCCUPIED ? "Tiếp tục order" : "Chọn bàn & order", statusColor(table.getStatus()));
            btn.addActionListener(e -> quickStartOrderForTable(table));
            card.add(name);
            card.add(Box.createVerticalStrut(6));
            card.add(seats);
            card.add(Box.createVerticalStrut(10));
            card.add(status);
            card.add(Box.createVerticalGlue());
            card.add(btn);
            tablesGridPanel.add(card);
        }
        tablesGridPanel.revalidate();
        tablesGridPanel.repaint();
    }

    private String statusText(TableStatus status) {
        return switch (status) {
            case AVAILABLE -> "Bàn trống";
            case OCCUPIED -> "Đang phục vụ";
            case RESERVED -> "Đã đặt trước";
        };
    }

    private Color statusColor(TableStatus status) {
        return switch (status) {
            case AVAILABLE -> UiTheme.ACCENT;
            case OCCUPIED -> UiTheme.WARNING;
            case RESERVED -> UiTheme.INFO;
        };
    }

    private void refreshMenuCategories() {
        if (cboMenuCategory == null) return;
        String selected = cboMenuCategory.getSelectedItem() == null ? "Tất cả" : cboMenuCategory.getSelectedItem().toString();
        cboMenuCategory.setModel(new DefaultComboBoxModel<>(new Vector<>(cafeService.getCategories())));
        cboMenuCategory.setSelectedItem(selected);
    }

    private void refreshMenuTable() {
        modelMenu.setRowCount(0);
        String category = cboMenuCategory.getSelectedItem() == null ? "Tất cả" : cboMenuCategory.getSelectedItem().toString();
        List<MenuItem> filtered = cafeService.findMenu(txtMenuSearch == null ? "" : txtMenuSearch.getText(), category);
        for (MenuItem item : filtered) {
            modelMenu.addRow(new Object[]{item.getId(), item.getName(), item.getCategory(), FormatUtils.money(item.getPrice()), item.isAvailable() ? "Có" : "Hết", item.getDescription()});
        }
        refreshMenuGallery(filtered);
    }

    private void refreshMenuGallery(List<MenuItem> items) {
        if (menuGalleryPanel == null) return;
        menuGalleryPanel.removeAll();
        for (MenuItem item : items) {
            JButton button = createMenuImageButton(item, 180, 110);
            button.addActionListener(e -> selectMenuRowById(item.getId()));
            menuGalleryPanel.add(button);
        }
        menuGalleryPanel.revalidate();
        menuGalleryPanel.repaint();
    }

    private void selectMenuRowById(int menuId) {
        for (int i = 0; i < modelMenu.getRowCount(); i++) {
            if (Integer.parseInt(modelMenu.getValueAt(i, 0).toString()) == menuId) {
                tblMenu.setRowSelectionInterval(i, i);
                tblMenu.scrollRectToVisible(tblMenu.getCellRect(i, 0, true));
                fillMenuFormFromSelection();
                return;
            }
        }
    }

    private void fillMenuFormFromSelection() {
        int row = tblMenu.getSelectedRow();
        if (row < 0) return;
        int id = Integer.parseInt(modelMenu.getValueAt(row, 0).toString());
        MenuItem selected = cafeService.findMenuItem(id);
        txtMenuName.setText(modelMenu.getValueAt(row, 1).toString());
        txtMenuCategoryInput.setText(modelMenu.getValueAt(row, 2).toString());
        txtMenuPrice.setText(modelMenu.getValueAt(row, 3).toString().replaceAll("[^0-9]", ""));
        chkMenuAvailable.setSelected("Có".equals(modelMenu.getValueAt(row, 4).toString()));
        txtMenuDescription.setText(modelMenu.getValueAt(row, 5).toString());
        txtMenuImagePath.setText(selected == null ? "" : selected.getImageUrl());
        updateMenuPreview(selected == null ? "" : selected.getImageUrl());
    }


    private void exportMenuCsv() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Xuất danh sách menu ra CSV");
            chooser.setSelectedFile(new File("menu_export.csv"));
            chooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            Path path = cafeService.exportMenuCsv(chooser.getSelectedFile().toPath());
            toast("Đã export menu: " + path);
        } catch (Exception ex) {
            toastError("Không thể export CSV: " + ex.getMessage());
        }
    }

    private void importMenuCsv() {
        if (currentUser.getRole() != Role.ADMIN) { toastError("Chỉ quản lý mới được import menu."); return; }
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Import menu từ CSV");
            chooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));
            if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
            int option = JOptionPane.showConfirmDialog(this,
                    "Bạn muốn xóa menu hiện tại trước khi import không?\nChọn Yes: thay thế toàn bộ menu.\nChọn No: thêm vào menu hiện tại.",
                    "Tùy chọn import", JOptionPane.YES_NO_CANCEL_OPTION);
            if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) return;
            int count = cafeService.importMenuCsv(chooser.getSelectedFile().toPath(), option == JOptionPane.YES_OPTION);
            refreshAll();
            toast("Đã import " + count + " món từ CSV.");
        } catch (Exception ex) {
            toastError("Import thất bại: " + ex.getMessage());
        }
    }

    private void openAddMenuDialog() {
        if (currentUser.getRole() != Role.ADMIN) { toastError("Chỉ quản lý mới được thêm món mới."); return; }
        JTextField name = new JTextField(); styleField(name);
        JTextField category = new JTextField(); styleField(category);
        JTextField price = new JTextField(); styleField(price);
        JTextArea description = new JTextArea(4, 18); styleArea(description);
        JCheckBox available = new JCheckBox("Đang phục vụ"); available.setOpaque(false); available.setSelected(true);
        JTextField imagePath = new JTextField(); styleField(imagePath); imagePath.setEditable(false);
        JLabel preview = new JLabel(); preview.setHorizontalAlignment(SwingConstants.CENTER); preview.setPreferredSize(new Dimension(280, 130)); preview.setOpaque(true); preview.setBackground(new Color(250,246,242)); preview.setBorder(BorderFactory.createCompoundBorder(new RoundBorder(18, UiTheme.BORDER), BorderFactory.createEmptyBorder(8,8,8,8)));
        JButton choose = createActionButton("Thêm ảnh", UiTheme.INFO);
        choose.addActionListener(e -> chooseMenuImageForField(imagePath, preview));
        JButton skip = createActionButton("Không thêm ảnh", UiTheme.WARNING);
        skip.addActionListener(e -> { imagePath.setText(""); preview.setIcon(null); preview.setText("Không có ảnh"); });
        JPanel chooser = new JPanel(new GridLayout(1,2,10,0)); chooser.setOpaque(false); chooser.add(choose); chooser.add(skip);
        JPanel panel = new JPanel(); panel.setOpaque(false); panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(formRow("Tên món", name)); panel.add(formRow("Danh mục", category)); panel.add(formRow("Giá (VND)", price)); panel.add(formRow("Mô tả", new JScrollPane(description))); panel.add(available); panel.add(Box.createVerticalStrut(8)); panel.add(formRow("Ảnh món (tùy chọn)", imagePath)); panel.add(chooser); panel.add(Box.createVerticalStrut(8)); panel.add(preview);
        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm món mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        try {
            cafeService.addMenuItem(name.getText().trim(), category.getText().trim(), Double.parseDouble(price.getText().trim()), available.isSelected(), description.getText().trim(), imagePath.getText().trim());
            clearMenuForm(); refreshAll(); toast("Đã thêm món mới.");
        } catch (Exception ex) { toastError("Không thể thêm món: " + ex.getMessage()); }
    }

    private void addMenuItem() { openAddMenuDialog(); }

    private void updateMenuItem() {
        int row = tblMenu.getSelectedRow();
        if (row < 0) { toastError("Hãy chọn món cần sửa."); return; }
        try {
            int id = Integer.parseInt(modelMenu.getValueAt(row, 0).toString());
            cafeService.updateMenuItem(id, txtMenuName.getText().trim(), txtMenuCategoryInput.getText().trim(), Double.parseDouble(txtMenuPrice.getText().trim()), chkMenuAvailable.isSelected(), txtMenuDescription.getText().trim(), txtMenuImagePath.getText().trim());
            refreshAll(); toast("Đã cập nhật món.");
        } catch (Exception ex) { toastError("Không thể cập nhật món: " + ex.getMessage()); }
    }

    private void deleteMenuItem() {
        if (currentUser.getRole() != Role.ADMIN) { toastError("Chỉ quản lý mới được xóa món trong menu."); return; }
        int row = tblMenu.getSelectedRow();
        if (row < 0) { toastError("Hãy chọn món cần xóa."); return; }
        int id = Integer.parseInt(modelMenu.getValueAt(row, 0).toString());
        if (JOptionPane.showConfirmDialog(this, "Xóa món này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            cafeService.deleteMenuItem(id); clearMenuForm(); refreshAll(); toast("Đã xóa món.");
        }
    }

    private void clearMenuForm() {
        txtMenuName.setText(""); txtMenuCategoryInput.setText(""); txtMenuPrice.setText(""); txtMenuDescription.setText(""); txtMenuImagePath.setText(""); chkMenuAvailable.setSelected(true); updateMenuPreview(""); tblMenu.clearSelection();
    }

    private void chooseMenuImage() { chooseMenuImageForField(txtMenuImagePath, lblMenuImagePreview); }

    private void chooseMenuImageForField(JTextField field, JLabel preview) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn ảnh món");
        chooser.setFileFilter(new FileNameExtensionFilter("Hình ảnh", "png", "jpg", "jpeg", "webp"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            field.setText(file.getPath());
            preview.setIcon(RemoteImageCache.get(file.getPath(), 200, 120, "MÓN"));
            preview.setText("");
        }
    }

    private void updateMenuPreview(String path) {
        if (lblMenuImagePreview == null) return;
        if (path == null || path.isBlank()) { lblMenuImagePreview.setIcon(null); lblMenuImagePreview.setText("Chưa chọn ảnh"); return; }
        lblMenuImagePreview.setIcon(RemoteImageCache.get(path, 220, 130, "MENU"));
        lblMenuImagePreview.setText("");
    }

    private void refreshOrderTableCombo() {
        if (cboOrderTable == null) return;
        Integer selectedId = null;
        CafeTable selected = (CafeTable) cboOrderTable.getSelectedItem();
        if (selected != null) selectedId = selected.getId();
        if (selectedId == null && currentOpenOrderId > 0) {
            Order current = cafeService.findOrder(currentOpenOrderId);
            if (current != null) selectedId = current.getTableId();
        }
        syncingOrderCombo = true;
        DefaultComboBoxModel<CafeTable> model = new DefaultComboBoxModel<>();
        for (CafeTable table : cafeService.getTables()) model.addElement(table);
        cboOrderTable.setModel(model);
        if (selectedId != null) {
            for (int i = 0; i < cboOrderTable.getItemCount(); i++) {
                CafeTable table = cboOrderTable.getItemAt(i);
                if (table.getId() == selectedId) {
                    cboOrderTable.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            cboOrderTable.setSelectedIndex(-1);
            lblOrderInfo.setText("Chưa chọn bàn");
        }
        syncingOrderCombo = false;
        if (selectedId != null && cboOrderTable.getSelectedItem() != null && currentOpenOrderId > 0) {
            autoPrepareSelectedOrder(false);
        }
    }

    private void selectOrderTable(int tableId) {
        for (int i = 0; i < cboOrderTable.getItemCount(); i++) {
            CafeTable table = cboOrderTable.getItemAt(i);
            if (table.getId() == tableId) {
                cboOrderTable.setSelectedIndex(i);
                break;
            }
        }
        autoPrepareSelectedOrder(false);
    }

    private void onOrderTableChanged() {
        if (syncingOrderCombo) return;
        autoPrepareSelectedOrder(false);
    }

    private void autoPrepareSelectedOrder(boolean showToast) {
        CafeTable table = (CafeTable) cboOrderTable.getSelectedItem();
        if (table == null) {
            lblOrderInfo.setText("Chưa chọn bàn");
            currentOpenOrderId = -1;
            refreshCurrentOrderView();
            return;
        }
        Order order = cafeService.openOrResumeOrder(table.getId(), currentUser, txtOrderCustomer.getText().trim());
        currentOpenOrderId = order.getId();
        lblOrderInfo.setText(table.getName() + " • order #" + order.getId() + " • " + statusText(cafeService.findTable(table.getId()).getStatus()));
        if ((txtOrderCustomer.getText() == null || txtOrderCustomer.getText().isBlank()) && order.getCustomerName() != null) {
            txtOrderCustomer.setText(order.getCustomerName());
        }
        refreshCurrentOrderView();
        refreshTablesPanel();
        refreshDashboard();
        refreshHeader();
        if (showToast) {
            toast("Đã sẵn sàng order cho " + table.getName());
        }
    }

    private void quickStartOrderForTable(CafeTable table) {
        showCard("orders", "Order & thanh toán nhanh");
        selectOrderTable(table.getId());
        autoPrepareSelectedOrder(true);
    }

    private void openOrResumeOrder() {
        autoPrepareSelectedOrder(true);
    }

    private void refreshFeaturedMenuButtons() {
        if (featuredMenuPanel == null) return;
        featuredMenuPanel.removeAll();
        for (MenuItem item : cafeService.getFeaturedMenuItems()) {
            JButton button = createMenuImageButton(item, 230, 120);
            button.addActionListener(e -> addQuickPickItem(item));
            featuredMenuPanel.add(button);
        }
        featuredMenuPanel.revalidate();
        featuredMenuPanel.repaint();
    }

    private JButton createMenuImageButton(MenuItem item, int width, int height) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(0, 8));
        JLabel imageLabel = new JLabel(RemoteImageCache.get(item.getImageUrl(), width, height, item.getName()));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(250, 246, 242));
        imageLabel.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(16, UiTheme.BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(item.getName(), SwingConstants.CENTER);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setForeground(UiTheme.TEXT);
        JLabel price = new JLabel(FormatUtils.money(item.getPrice()), SwingConstants.CENTER);
        price.setAlignmentX(Component.CENTER_ALIGNMENT);
        price.setFont(UiTheme.BODY);
        price.setForeground(UiTheme.MUTED);
        footer.add(name);
        footer.add(Box.createVerticalStrut(2));
        footer.add(price);

        button.add(imageLabel, BorderLayout.CENTER);
        button.add(footer, BorderLayout.SOUTH);
        button.setBackground(Color.WHITE);
        button.setBorder(new RoundBorder(18, UiTheme.BORDER));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(width + 24, height + 78));
        button.setContentAreaFilled(false);
        return button;
    }

    private void addQuickPickItem(MenuItem item) {
        if (currentOpenOrderId <= 0) autoPrepareSelectedOrder(false);
        if (currentOpenOrderId <= 0) {
            toastError("Hãy chọn bàn trước.");
            return;
        }
        String note = JOptionPane.showInputDialog(this, "Ghi chú cho món " + item.getName() + ":", "");
        if (note == null) note = "";
        cafeService.addItemToOrder(currentOpenOrderId, item, 1, note);
        refreshCurrentOrderView();
        refreshDashboard();
        refreshHeader();
        toast("Đã thêm nhanh " + item.getName());
    }

    private void refreshOrderMenuTable() {
        modelOrderMenu.setRowCount(0);
        for (MenuItem item : cafeService.findMenu(txtOrderSearch == null ? "" : txtOrderSearch.getText(), "Tất cả")) {
            modelOrderMenu.addRow(new Object[]{item.getId(), item.getName(), item.getCategory(), FormatUtils.money(item.getPrice()), item.isAvailable() ? "Có" : "Hết", item.getDescription()});
        }
    }

    private void addSelectedMenuToOrder() {
        if (currentOpenOrderId <= 0) autoPrepareSelectedOrder(false);
        if (currentOpenOrderId <= 0) { toastError("Hãy chọn bàn trước."); return; }
        int row = tblOrderMenu.getSelectedRow();
        if (row < 0) { toastError("Hãy chọn món trong danh sách menu."); return; }
        int menuId = Integer.parseInt(modelOrderMenu.getValueAt(row, 0).toString());
        MenuItem menuItem = cafeService.findMenuItem(menuId);
        if (menuItem == null || !menuItem.isAvailable()) { toastError("Món đang không sẵn sàng."); return; }
        cafeService.addItemToOrder(currentOpenOrderId, menuItem, (int) spnQuantity.getValue(), txtItemNote.getText().trim());
        txtItemNote.setText("");
        spnQuantity.setValue(1);
        refreshCurrentOrderView();
        refreshTablesPanel();
        refreshDashboard();
        refreshHeader();
        toast("Đã thêm món vào order.");
    }

    private void openSelectedMenuDialog() {
        int row = tblOrderMenu.getSelectedRow();
        if (row < 0) {
            toastError("Hãy chọn món trong danh sách.");
            return;
        }
        int menuId = Integer.parseInt(modelOrderMenu.getValueAt(row, 0).toString());
        MenuItem menuItem = cafeService.findMenuItem(menuId);
        if (menuItem != null) showAddMenuDialog(menuItem);
    }

    private void openMenuSelectionDialog() {
        if (currentOpenOrderId <= 0) autoPrepareSelectedOrder(false);
        if (currentOpenOrderId <= 0) { toastError("Hãy chọn bàn trước."); return; }
        List<MenuItem> items = cafeService.findMenu("", "Tất cả").stream().filter(MenuItem::isAvailable).toList();
        if (items.isEmpty()) { toastError("Chưa có món khả dụng trong menu."); return; }
        JPanel grid = new JPanel(new GridLayout(0, 3, 12, 12));
        grid.setOpaque(false);
        JOptionPane optionPane = new JOptionPane();
        javax.swing.JDialog[] holder = new javax.swing.JDialog[1];
        for (MenuItem item : items) {
            JButton button = createMenuImageButton(item, 180, 110);
            button.addActionListener(e -> { if (holder[0] != null) holder[0].dispose(); showAddMenuDialog(item); });
            grid.add(button);
        }
        JScrollPane scrollPane = new JScrollPane(grid);
        scrollPane.setPreferredSize(new Dimension(680, 460));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        optionPane.setMessage(scrollPane);
        optionPane.setMessageType(JOptionPane.PLAIN_MESSAGE);
        optionPane.setOptions(new Object[]{"Đóng"});
        holder[0] = optionPane.createDialog(this, "Gọi món bằng danh sách có hình ảnh");
        holder[0].setModal(true);
        holder[0].setVisible(true);
    }

    private void showAddMenuDialog(MenuItem menuItem) {
        if (currentOpenOrderId <= 0) autoPrepareSelectedOrder(false);
        if (currentOpenOrderId <= 0) { toastError("Hãy chọn bàn trước."); return; }
        JSpinner qty = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        JTextField note = new JTextField();
        styleField(note);
        JTextArea info = new JTextArea(menuItem.getDescription());
        info.setEditable(false);
        styleArea(info);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1; gbc.insets = new Insets(6, 6, 6, 6);
        panel.add(new JLabel(menuItem.getName() + " • " + FormatUtils.money(menuItem.getPrice())), gbc);
        gbc.gridy++; panel.add(new JLabel("Số lượng"), gbc);
        gbc.gridy++; panel.add(qty, gbc);
        gbc.gridy++; panel.add(new JLabel("Ghi chú"), gbc);
        gbc.gridy++; panel.add(note, gbc);
        gbc.gridy++; panel.add(new JLabel("Mô tả"), gbc);
        gbc.gridy++; panel.add(new JScrollPane(info), gbc);
        int result = JOptionPane.showConfirmDialog(this, panel, "Gọi món", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            cafeService.addItemToOrder(currentOpenOrderId, menuItem, (int) qty.getValue(), note.getText().trim());
            refreshCurrentOrderView();
            refreshHeader();
            refreshTablesPanel();
            toast("Đã thêm món vào order.");
        }
    }

    private void refreshCurrentOrderView() {
        modelCart.setRowCount(0);
        if (currentOpenOrderId <= 0) {
            lblOrderInfo.setText("Chưa chọn bàn");
            lblOrderSubtotal.setText("Tạm tính: -");
            lblOrderTax.setText("Thuế: -");
            lblOrderService.setText("Phí dịch vụ: -");
            lblOrderTotal.setText("Tổng cộng: -");
            return;
        }
        Order order = cafeService.findOrder(currentOpenOrderId);
        if (order == null) return;
        for (OrderItem item : order.getItems()) {
            modelCart.addRow(new Object[]{item.getMenuItemId(), item.getName(), item.getQuantity(), FormatUtils.money(item.getUnitPrice()), item.getNote(), FormatUtils.money(item.getLineTotal())});
        }
        lblOrderSubtotal.setText("Tạm tính: " + FormatUtils.money(order.getSubtotal()));
        lblOrderTax.setText("Thuế: " + FormatUtils.money(order.getTaxAmount()));
        lblOrderService.setText("Phí dịch vụ: " + FormatUtils.money(order.getServiceChargeAmount()));
        lblOrderTotal.setText("Tổng cộng: " + FormatUtils.money(order.getTotal()));
    }

    private void updateCartItem() {
        if (currentOpenOrderId <= 0) { toastError("Chưa có order."); return; }
        int row = tblCart.getSelectedRow();
        if (row < 0) { toastError("Hãy chọn món trong giỏ order."); return; }
        int itemId = Integer.parseInt(modelCart.getValueAt(row, 0).toString());
        String qtyStr = JOptionPane.showInputDialog(this, "Nhập số lượng mới:", modelCart.getValueAt(row, 2).toString());
        if (qtyStr == null) return;
        String note = JOptionPane.showInputDialog(this, "Ghi chú mới:", modelCart.getValueAt(row, 4).toString());
        try {
            int qty = Integer.parseInt(qtyStr.trim());
            cafeService.updateOrderItem(currentOpenOrderId, itemId, qty, note == null ? "" : note);
            refreshCurrentOrderView();
            refreshHeader();
            toast("Đã cập nhật món.");
        } catch (Exception ex) {
            toastError("Số lượng không hợp lệ.");
        }
    }

    private void removeCartItem() {
        if (currentOpenOrderId <= 0) { toastError("Chưa có order."); return; }
        int row = tblCart.getSelectedRow();
        if (row < 0) { toastError("Hãy chọn món cần xóa."); return; }
        int itemId = Integer.parseInt(modelCart.getValueAt(row, 0).toString());
        cafeService.removeItem(currentOpenOrderId, itemId);
        Order order = cafeService.findOrder(currentOpenOrderId);
        if (order != null && order.getStatus() != OrderStatus.OPEN) {
            currentOpenOrderId = -1;
        }
        refreshAll();
        toast("Đã xóa món khỏi order.");
    }

    private void releaseCurrentTable() {
        CafeTable table = (CafeTable) cboOrderTable.getSelectedItem();
        if (table == null) {
            toastError("Hãy chọn bàn cần giải phóng.");
            return;
        }
        if (table.getStatus() == TableStatus.AVAILABLE && table.getCurrentOrderId() == null) {
            toast("Bàn này đang ở trạng thái trống.");
            clearOrderSelection("Bàn hiện đang trống và chưa có bàn nào được chọn.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Giải phóng " + table.getName() + " và đóng order hiện tại?",
                "Xác nhận giải phóng bàn", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        cafeService.releaseTable(table.getId());
        clearOrderSelection(table.getName() + " đã được chuyển về trạng thái bàn trống.");
        refreshAll();
        toast("Đã giải phóng bàn thành công.");
    }

    private void checkoutOrder() {
        if (currentOpenOrderId <= 0) { toastError("Chưa có order để thanh toán."); return; }
        try {
            String paymentMethod = cboPaymentMethod.getSelectedItem() == null ? "Tiền mặt" : cboPaymentMethod.getSelectedItem().toString();
            if ("Chuyển khoản".equalsIgnoreCase(paymentMethod) && !showTransferQrDialog()) {
                return;
            }
            int paidOrderId = currentOpenOrderId;
            Path invoice = cafeService.checkoutOrder(paidOrderId, paymentMethod, txtOrderCustomer.getText().trim());
            Order paidOrder = cafeService.findOrders("", null, null).stream().filter(o -> o.getId() == paidOrderId).findFirst().orElse(null);
            String invoiceText = paidOrder == null ? java.nio.file.Files.readString(invoice) : cafeService.buildInvoiceText(paidOrder);
            JTextArea area = new JTextArea(invoiceText);
            area.setEditable(false);
            area.setFont(new Font("Consolas", Font.PLAIN, 13));
            JScrollPane pane = new JScrollPane(area);
            pane.setPreferredSize(new Dimension(520, 420));
            Object[] options = {"In hóa đơn", "Không in"};
            int choice = JOptionPane.showOptionDialog(this, pane, "Hóa đơn đã lưu trong hệ thống", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[1]);
            if (choice == JOptionPane.YES_OPTION) {
                try { area.print(); } catch (Exception ex) { toastError("Không thể in hóa đơn: " + ex.getMessage()); }
            }
            clearOrderSelection("Đã thanh toán thành công. Bàn đã chuyển về trạng thái bàn trống.");
            refreshAll();
            toast("Thanh toán thành công. Hóa đơn đã lưu tại: " + invoice.toAbsolutePath());
        } catch (Exception ex) {
            toastError(ex.getMessage());
        }
    }

    private void clearOrderSelection(String infoText) {
        currentOpenOrderId = -1;
        txtOrderCustomer.setText("");
        txtItemNote.setText("");
        syncingOrderCombo = true;
        if (cboOrderTable != null) {
            cboOrderTable.setSelectedIndex(-1);
        }
        syncingOrderCombo = false;
        lblOrderInfo.setText(infoText == null || infoText.isBlank() ? "Chưa chọn bàn" : infoText);
        refreshCurrentOrderView();
    }

    private void refreshReports() {
        if (modelReports == null) return;
        modelReports.setRowCount(0);
        LocalDate from = parseDate(txtReportFrom == null ? null : txtReportFrom.getText().trim());
        LocalDate to = parseDate(txtReportTo == null ? null : txtReportTo.getText().trim());
        for (Order order : cafeService.findOrders(txtReportSearch == null ? "" : txtReportSearch.getText(), from, to)) {
            modelReports.addRow(new Object[]{order.getId(), order.getStatus(), order.getTableName(), order.getCustomerName(), order.getCreatedBy(), FormatUtils.money(order.getTotal()), FormatUtils.dateTime(order.getCreatedAt())});
        }
        if (modelReports.getRowCount() > 0 && tblReports.getSelectedRow() < 0) {
            tblReports.setRowSelectionInterval(0, 0);
            previewSelectedInvoice();
        } else if (modelReports.getRowCount() == 0 && txtInvoicePreview != null) {
            txtInvoicePreview.setText("");
        }

        if (modelDailyStats != null) {
            modelDailyStats.setRowCount(0);
            for (DailySalesStats stats : cafeService.getDailySalesStats(7)) {
                modelDailyStats.addRow(new Object[]{FormatUtils.date(stats.getDate()), stats.getPaidOrders(), stats.getCupsSold(), FormatUtils.money(stats.getRevenue())});
            }
        }
        if (lblMonthRevenueSummary != null) {
            DailySalesStats monthStats = cafeService.getCurrentMonthSalesStats();
            double monthCost = estimateCost(monthStats.getRevenue());
            double monthProfit = monthStats.getRevenue() - monthCost;
            lblMonthRevenueSummary.setText("Tháng này: " + monthStats.getPaidOrders() + " hóa đơn • " + monthStats.getCupsSold() + " ly/món • Doanh thu " + FormatUtils.money(monthStats.getRevenue()) + " • Chi phí ước tính " + FormatUtils.money(monthCost) + " • Lợi nhuận ước tính " + FormatUtils.money(monthProfit));
            lblMonthRevenueSummary.setFont(UiTheme.BODY);
            lblMonthRevenueSummary.setForeground(UiTheme.MUTED);
        }
    }

    private void previewSelectedInvoice() {
        int row = tblReports.getSelectedRow();
        if (row < 0) return;
        int orderId = Integer.parseInt(modelReports.getValueAt(row, 0).toString());
        Order order = cafeService.findOrder(orderId);
        if (order != null) {
            txtInvoicePreview.setText(cafeService.buildInvoiceText(order) + "\n\nFile tổng hợp: exports/invoices/hoa_don_tong_hop.txt");
        }
    }

    private void exportSelectedInvoice() {
        int row = tblReports.getSelectedRow();
        if (row < 0) { toastError("Hãy chọn hóa đơn trong báo cáo."); return; }
        int orderId = Integer.parseInt(modelReports.getValueAt(row, 0).toString());
        Order order = cafeService.findOrder(orderId);
        if (order == null) return;
        Path path = cafeService.exportInvoice(order);
        toast("Đã xuất lại hóa đơn: " + path.toAbsolutePath());
    }

    private void refreshSettings() {
        AppSettings settings = cafeService.getData().getSettings();
        txtBizName.setText(settings.getBusinessName());
        txtBizSlogan.setText(settings.getSlogan());
        txtTaxRate.setText(String.valueOf(settings.getTaxRate()));
        txtServiceRate.setText(String.valueOf(settings.getServiceChargeRate()));
        txtInvoiceFooter.setText(settings.getInvoiceFooter());
        boolean canEditFees = cafeService.canEditFees(currentUser);
        txtTaxRate.setEditable(canEditFees);
        txtServiceRate.setEditable(canEditFees);
        txtTaxRate.setEnabled(canEditFees);
        txtServiceRate.setEnabled(canEditFees);
        if (btnSaveSettings != null) {
            btnSaveSettings.setEnabled(canEditFees);
            btnSaveSettings.setToolTipText(canEditFees ? "Quản lý có thể thay đổi thuế và phí dịch vụ" : "Chỉ tài khoản quản lý mới được thay đổi thuế và phí dịch vụ");
        }
    }

    private void saveSettings() {
        if (!cafeService.canEditFees(currentUser)) {
            toastError("Chỉ tài khoản quản lý mới được thay đổi thuế và phí dịch vụ.");
            return;
        }
        try {
            cafeService.saveSettings(txtBizName.getText().trim(), txtBizSlogan.getText().trim(), Double.parseDouble(txtTaxRate.getText().trim()), Double.parseDouble(txtServiceRate.getText().trim()), txtInvoiceFooter.getText().trim());
            refreshAll();
            toast("Đã lưu cài đặt.");
        } catch (Exception ex) {
            toastError("Không thể lưu cài đặt: " + ex.getMessage());
        }
    }

    private void refreshUsersTable() {
        if (modelUsers == null) return;
        modelUsers.setRowCount(0);
        for (User user : cafeService.getData().getUsers()) {
            modelUsers.addRow(new Object[]{user.getId(), user.getFullName(), user.getUsername(), user.getRole(), user.isActive() ? "Hoạt động" : "Khóa", FormatUtils.hours(cafeService.getTodayHoursForUser(user))});
        }
    }

    private void createStaffAccount() {
        String error = authService.registerStaff(txtStaffName.getText().trim(), txtStaffUser.getText().trim(), txtStaffPass.getText().trim(), (Role) cboStaffRole.getSelectedItem());
        if (error != null) {
            toastError(error);
            return;
        }
        txtStaffName.setText("");
        txtStaffUser.setText("");
        txtStaffPass.setText("");
        refreshUsersTable();
        toast("Đã tạo tài khoản nhân viên.");
    }

    private void toggleAttendance() {
        if (cafeService.isClockedIn(currentUser)) {
            ShiftRecord record = cafeService.clockOut(currentUser);
            toast("Đã chấm công ra lúc " + FormatUtils.dateTime(record == null ? null : record.getClockOut()));
        } else {
            ShiftRecord record = cafeService.clockIn(currentUser);
            toast("Đã chấm công vào lúc " + FormatUtils.dateTime(record == null ? null : record.getClockIn()));
        }
        refreshAll();
    }

    private void refreshShiftRecords() {
        if (modelShiftRecords == null && modelShiftRecordsDetail == null) return;
        if (modelShiftRecords != null) modelShiftRecords.setRowCount(0);
        if (modelShiftRecordsDetail != null) modelShiftRecordsDetail.setRowCount(0);
        for (ShiftRecord record : cafeService.getTodayShiftRecords()) {
            Object[] row = new Object[]{
                    FormatUtils.date(record.getDate()),
                    record.getUserName(),
                    FormatUtils.time(record.getClockIn()),
                    FormatUtils.time(record.getClockOut()),
                    FormatUtils.workedTime(record.getWorkedMinutes()),
                    record.isOpen() ? "Đang trong ca" : "Đã kết thúc"
            };
            if (modelShiftRecords != null) modelShiftRecords.addRow(row);
            if (modelShiftRecordsDetail != null) modelShiftRecordsDetail.addRow(row.clone());
        }
    }


    private void refreshAuthHistory() {
        if (modelAuthHistory == null) return;
        modelAuthHistory.setRowCount(0);
        for (AuthHistoryRecord record : authService.getAuthHistory()) {
            modelAuthHistory.addRow(new Object[]{
                    record.getFullName(),
                    record.getUsername(),
                    record.getRoleName(),
                    FormatUtils.dateTime(record.getLoginAt()),
                    FormatUtils.dateTime(record.getLogoutAt()),
                    record.isOnline() ? "Đang online" : "Đã đăng xuất"
            });
        }
    }

    private void showTodayRevenueSummary() {
        DailySalesStats today = cafeService.getTodaySalesStats();
        DailySalesStats month = cafeService.getCurrentMonthSalesStats();
        String message = "Doanh thu hôm nay: " + FormatUtils.money(today.getRevenue())
                + "\nSố hóa đơn hôm nay: " + today.getPaidOrders()
                + "\nTổng số ly / món hôm nay: " + today.getCupsSold()
                + "\n\nDoanh thu tháng này: " + FormatUtils.money(month.getRevenue())
                + "\nSố hóa đơn tháng này: " + month.getPaidOrders()
                + "\nTổng ly / món tháng này: " + month.getCupsSold()
                + "\n\nFile hóa đơn tổng hợp: exports/invoices/hoa_don_tong_hop.txt";
        JOptionPane.showMessageDialog(this, message, "Doanh thu ngày / tháng", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        if (JOptionPane.showConfirmDialog(this, "Bạn muốn đăng xuất?", "Đăng xuất", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            authService.recordLogout(currentUser);
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame(authService, user -> new MainFrame(authService, cafeService, user).setVisible(true)).setVisible(true));
        }
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(244, 235, 226));
        table.getTableHeader().setForeground(UiTheme.TEXT);
        table.setFont(UiTheme.BODY);
        table.setGridColor(UiTheme.BORDER);
        table.setBackground(Color.WHITE);
        return table;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.SUBTITLE);
        label.setForeground(UiTheme.TEXT);
        return label;
    }

    private JPanel formRow(String label, Component component) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UiTheme.BODY);
        lbl.setForeground(UiTheme.TEXT);
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private void styleField(JTextField field) {
        field.setFont(UiTheme.BODY);
        field.setBorder(new RoundBorder(14, UiTheme.BORDER));
        field.setMargin(new Insets(8, 10, 8, 10));
        field.setBackground(UiTheme.SURFACE_ALT);
    }

    private void styleArea(JTextArea area) {
        area.setFont(UiTheme.BODY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new RoundBorder(14, UiTheme.BORDER));
        area.setMargin(new Insets(10, 10, 10, 10));
        area.setBackground(UiTheme.SURFACE_ALT);
    }

    private void refreshMonthlyStaffSummary() {
        if (modelMonthlyStaffSummary == null) return;
        modelMonthlyStaffSummary.setRowCount(0);
        YearMonth month = YearMonth.now();
        for (User user : cafeService.getData().getUsers()) {
            if (!user.isActive()) continue;
            List<ShiftRecord> records = cafeService.getData().getShiftRecords().stream()
                    .filter(s -> s.getUserId() == user.getId())
                    .filter(s -> YearMonth.from(s.getClockIn().toLocalDate()).equals(month))
                    .sorted(Comparator.comparing(ShiftRecord::getClockIn).reversed())
                    .toList();
            long days = records.stream().map(s -> s.getClockIn().toLocalDate()).distinct().count();
            double hours = records.stream().mapToDouble(ShiftRecord::getWorkedHours).sum();
            boolean hasOpen = records.stream().anyMatch(ShiftRecord::isOpen);
            modelMonthlyStaffSummary.addRow(new Object[]{user.getFullName(), user.getUsername(), month.getMonthValue() + "/" + month.getYear(), days, FormatUtils.hours(hours), hasOpen ? "Đang có ca mở" : "Đã chốt ca"});
        }
    }

    private void showRevenueReportWindow() {
        JDialog dialog = new JDialog(this, "Báo cáo doanh thu chi tiết", true);
        dialog.setSize(1260, 820);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(16, 16));

        JPanel body = new JPanel(new BorderLayout(16, 16));
        body.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        body.setBackground(UiTheme.BG);

        DailySalesStats today = cafeService.getTodaySalesStats();
        DailySalesStats month = cafeService.getCurrentMonthSalesStats();
        double todayCost = estimateCost(today.getRevenue());
        double todayProfit = today.getRevenue() - todayCost;
        double monthCost = estimateCost(month.getRevenue());
        double monthProfit = month.getRevenue() - monthCost;

        JPanel hero = new JPanel(new GridLayout(2, 3, 12, 12));
        hero.setOpaque(false);
        hero.add(createStatCard("Doanh thu hôm nay", createValueLabel(FormatUtils.money(today.getRevenue())), UiTheme.ACCENT));
        hero.add(createStatCard("Chi phí hôm nay (ước tính)", createValueLabel(FormatUtils.money(todayCost)), UiTheme.WARNING));
        hero.add(createStatCard("Lợi nhuận hôm nay (ước tính)", createValueLabel(FormatUtils.money(todayProfit)), UiTheme.INFO));
        hero.add(createStatCard("Doanh thu tháng", createValueLabel(FormatUtils.money(month.getRevenue())), UiTheme.PRIMARY));
        hero.add(createStatCard("Chi phí tháng (ước tính)", createValueLabel(FormatUtils.money(monthCost)), UiTheme.WARNING));
        hero.add(createStatCard("Lợi nhuận tháng (ước tính)", createValueLabel(FormatUtils.money(monthProfit)), UiTheme.ACCENT));
        body.add(hero, BorderLayout.NORTH);

        DefaultTableModel dayModel = new DefaultTableModel(new Object[]{"Ngày", "Số hóa đơn", "Số ly", "Doanh thu", "Chi phí", "Lợi nhuận"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (DailySalesStats stat : cafeService.getDailySalesStats(31)) {
            double cost = estimateCost(stat.getRevenue());
            double profit = stat.getRevenue() - cost;
            dayModel.addRow(new Object[]{FormatUtils.date(stat.getDate()), stat.getPaidOrders(), stat.getCupsSold(), FormatUtils.money(stat.getRevenue()), FormatUtils.money(cost), FormatUtils.money(profit)});
        }
        JTable dayTable = createTable(dayModel);

        DefaultTableModel invoiceModel = new DefaultTableModel(new Object[]{"Mã", "Khách", "Nhân viên", "Thanh toán", "Tổng", "Ngày"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Order> monthOrders = cafeService.findOrders("", month.getDate().withDayOfMonth(1), LocalDate.now()).stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .sorted((a, b) -> b.getClosedAt().compareTo(a.getClosedAt()))
                .toList();
        for (Order o : monthOrders) {
            invoiceModel.addRow(new Object[]{o.getId(), o.getCustomerName(), o.getCreatedBy(), o.getPaymentMethod(), FormatUtils.money(o.getTotal()), FormatUtils.dateTime(o.getClosedAt())});
        }
        JTable invoiceTable = createTable(invoiceModel);

        DefaultTableModel itemModel = new DefaultTableModel(new Object[]{"Món", "Số ly hôm nay"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        Map<String, Integer> top = cafeService.getTopSellingItemsToday();
        if (top.isEmpty()) {
            itemModel.addRow(new Object[]{"Chưa có dữ liệu", 0});
        } else {
            top.forEach((name, qty) -> itemModel.addRow(new Object[]{name, qty}));
        }
        JTable itemTable = createTable(itemModel);

        DefaultTableModel staffModel = new DefaultTableModel(new Object[]{"Nhân viên", "Tài khoản", "Số ngày làm", "Tổng giờ"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        YearMonth currentMonth = YearMonth.now();
        for (User user : cafeService.getData().getUsers()) {
            if (!user.isActive()) continue;
            List<ShiftRecord> records = cafeService.getData().getShiftRecords().stream()
                    .filter(s -> s.getUserId() == user.getId())
                    .filter(s -> YearMonth.from(s.getClockIn().toLocalDate()).equals(currentMonth))
                    .toList();
            long days = records.stream().map(s -> s.getClockIn().toLocalDate()).distinct().count();
            double hours = records.stream().mapToDouble(ShiftRecord::getWorkedHours).sum();
            staffModel.addRow(new Object[]{user.getFullName(), user.getUsername(), days, FormatUtils.hours(hours)});
        }
        JTable staffTable = createTable(staffModel);

        CardPanel left = new CardPanel();
        left.setLayout(new BorderLayout(0, 12));
        JLabel leftTitle = new JLabel("Chi tiết doanh thu theo ngày / tháng");
        leftTitle.setFont(UiTheme.SUBTITLE);
        left.add(leftTitle, BorderLayout.NORTH);
        left.add(new JScrollPane(dayTable), BorderLayout.CENTER);

        JTabbedPane rightTabs = new JTabbedPane();
        rightTabs.addTab("Hóa đơn tháng", new JScrollPane(invoiceTable));
        rightTabs.addTab("Top món hôm nay", new JScrollPane(itemTable));
        rightTabs.addTab("Báo cáo nhân viên", new JScrollPane(staffTable));

        CardPanel right = new CardPanel();
        right.setLayout(new BorderLayout(0, 12));
        JLabel rightTitle = new JLabel("Báo cáo mở rộng");
        rightTitle.setFont(UiTheme.SUBTITLE);
        right.add(rightTitle, BorderLayout.NORTH);
        right.add(rightTabs, BorderLayout.CENTER);
        right.setPreferredSize(new Dimension(520, 0));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setResizeWeight(0.58);
        body.add(split, BorderLayout.CENTER);

        JButton close = createActionButton("Đóng báo cáo", UiTheme.PRIMARY);
        close.addActionListener(e -> dialog.dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(close);
        body.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(body);
        dialog.setVisible(true);
    }

    private double estimateCost(double revenue) {
        return revenue * 0.40;
    }

    private JLabel createValueLabel(String value) {
        JLabel label = new JLabel(value);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(UiTheme.TEXT);
        return label;
    }

    private boolean showTransferQrDialog() {
        JDialog dialog = new JDialog(this, "Thanh toán chuyển khoản", true);
        dialog.setSize(460, 620);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 12));

        JPanel root = new JPanel();
        root.setBackground(UiTheme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Quét mã QR để thanh toán");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel hint = new JLabel("Cảm ơn quý khách hihii!!");
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setFont(UiTheme.BODY);
        hint.setForeground(UiTheme.MUTED);

        JLabel qr = new JLabel(loadTransferQrIcon());
        qr.setAlignmentX(Component.CENTER_ALIGNMENT);
        qr.setBorder(BorderFactory.createCompoundBorder(new RoundBorder(24, UiTheme.BORDER), BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        qr.setOpaque(true);
        qr.setBackground(Color.WHITE);

        final boolean[] ok = {false};
        JButton done = createActionButton("Đã thanh toán thành công", UiTheme.ACCENT);
        done.setPreferredSize(new Dimension(320, 52));
        done.setMaximumSize(new Dimension(320, 52));
        done.setAlignmentX(Component.CENTER_ALIGNMENT);
        done.addActionListener(e -> { ok[0] = true; dialog.dispose(); });
        JButton cancel = createActionButton("Hủy", UiTheme.DANGER);
        cancel.setPreferredSize(new Dimension(140, 44));
        cancel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancel.addActionListener(e -> dialog.dispose());

        root.add(title);
        root.add(Box.createVerticalStrut(8));
        root.add(hint);
        root.add(Box.createVerticalStrut(18));
        root.add(qr);
        root.add(Box.createVerticalStrut(18));
        root.add(done);
        root.add(Box.createVerticalStrut(10));
        root.add(cancel);

        dialog.setContentPane(root);
        dialog.setVisible(true);
        return ok[0];
    }

    private ImageIcon loadTransferQrIcon() {
        Path qrPath = Path.of("assets", "qr_anh_kiet_thanh_toan.png");
        ImageIcon icon = new ImageIcon(qrPath.toString());
        Image img = icon.getImage().getScaledInstance(320, 320, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(UiTheme.BODY);
        combo.setBorder(new RoundBorder(14, UiTheme.BORDER));
        combo.setBackground(UiTheme.SURFACE_ALT);
    }

    private JButton createActionButton(String text, Color color) {
        return createActionButton(text, color, null);
    }

    private JButton createActionButton(String text, Color color, java.awt.event.ActionListener listener) {
        RoundButton btn = new RoundButton(text, color, hover(color), hover(hover(color)));
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setPreferredSize(new Dimension(200, 46));
        btn.setForeground(textColor(color));
        if (listener != null) btn.addActionListener(listener);
        return btn;
    }

    private void applyButtonStyle(JButton button, String text, Color color) {
        button.setText(text);
        button.setBackground(color);
        button.setForeground(textColor(color));
        button.setBorder(new RoundBorder(16, color));
    }

    private Color hover(Color c) {
        return new Color(
                Math.min(255, c.getRed() + 16),
                Math.min(255, c.getGreen() + 16),
                Math.min(255, c.getBlue() + 16));
    }

    private Color textColor(Color background) {
        int luminance = (background.getRed() * 299 + background.getGreen() * 587 + background.getBlue() * 114) / 1000;
        return luminance >= 170 ? UiTheme.TEXT : Color.WHITE;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private void toast(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void toastError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
