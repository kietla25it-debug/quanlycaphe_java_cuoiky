package auroracafe.store;

import auroracafe.model.AppData;
import auroracafe.model.AppSettings;
import auroracafe.model.CafeTable;
import auroracafe.model.MenuItem;
import auroracafe.model.Role;
import auroracafe.model.TableStatus;
import auroracafe.model.User;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStore implements DataStore {
    private final Path filePath;

    public FileStore(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public AppData load() {
        try {
            if (Files.exists(filePath)) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
                    Object obj = in.readObject();
                    if (obj instanceof AppData loaded) {
                        normalize(loaded);
                        loaded.syncCounters();
                        save(loaded);
                        return loaded;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        AppData data = seed();
        save(data);
        return data;
    }

    @Override
    public void save(AppData data) {
        try {
            Files.createDirectories(filePath.getParent());
            data.syncCounters();
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
                out.writeObject(data);
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu dữ liệu: " + e.getMessage(), e);
        }
    }

    public AppData loadNormalized(AppData data) {
        normalize(data);
        return data;
    }

    public AppData seed() {
        AppData data = new AppData();
        ensureManagerAccount(data);

        addMenu(data, "Aurora Latte", "Cà phê", 49000, true, "Latte thơm béo với lớp sữa mịn và latte art đẹp mắt.", "assets/menu/aurora_latte.png");
        addMenu(data, "Cold Brew Cam", "Cà phê", 55000, true, "Cold brew pha cùng syrup cam thanh mát, hậu vị dễ chịu.", "assets/menu/cold_brew_cam.png");
        addMenu(data, "Espresso Tonic", "Signature", 59000, true, "Espresso mạnh mẽ kết hợp tonic mát lạnh, rất hợp ngày nóng.", "assets/menu/espresso_tonic.png");
        addMenu(data, "Americano Đá", "Cà phê", 39000, true, "Americano đậm vị, ít ngọt, uống hằng ngày rất hợp.", "assets/menu/aurora_latte.png");
        addMenu(data, "Bạc Xỉu Kem Mặn", "Cà phê", 46000, true, "Bạc xỉu thơm sữa cùng lớp kem mặn béo nhẹ.", "assets/menu/bac_xiu_kem_man.png");
        addMenu(data, "Cà phê Muối", "Signature", 47000, true, "Cà phê đậm đà cân bằng giữa vị đắng nhẹ và kem mặn.", "assets/menu/ca_phe_muoi.png");
        addMenu(data, "Matcha Cloud", "Trà", 52000, true, "Matcha Nhật cùng lớp kem cloud bồng bềnh, màu sắc đẹp mắt.", "assets/menu/matcha_cloud.png");
        addMenu(data, "Oolong Peach Tea", "Trà", 45000, true, "Trà ô long đào dịu thơm, thanh mát dễ uống.", "assets/menu/oolong_peach_tea.png");
        addMenu(data, "Trà Vải Hoa Hồng", "Trà", 48000, true, "Vải ngọt thanh hòa cùng hương hoa hồng nhẹ nhàng.", "assets/menu/tra_vai_hoa_hong.png");
        addMenu(data, "Sakura Milk", "Signature", 58000, true, "Sữa hoa anh đào pastel dịu ngọt và nổi bật.", "assets/menu/sakura_milk.png");
        addMenu(data, "Chocolate Mousse", "Bánh", 42000, true, "Mousse chocolate mềm mịn, ngọt vừa.", "assets/menu/chocolate_mousse.png");
        addMenu(data, "Cheese Croffle", "Bánh", 47000, true, "Croffle nướng giòn, thơm bơ và phô mai.", "assets/menu/cheese_croffle.png");
        addMenu(data, "Berry Yogurt", "Đá xay", 53000, true, "Sữa chua berry mát lạnh, vị trái cây dịu nhẹ.", "assets/menu/berry_yogurt.png");
        addMenu(data, "Mint Lemon Soda", "Nước trái cây", 39000, true, "Chanh bạc hà tươi mát, giải nhiệt tốt.", "assets/menu/mint_lemon_soda.png");
        addMenu(data, "Caramel Frappe", "Đá xay", 57000, true, "Đá xay caramel béo thơm, topping hấp dẫn.", "assets/menu/caramel_frappe.png");
        addMenu(data, "Coco Coffee", "Signature", 56000, true, "Cà phê dừa mịn, thơm và béo vừa phải.", "assets/menu/coco_coffee.png");
        addMenu(data, "Affogato", "Dessert", 62000, true, "Kem vanilla dùng cùng shot espresso nóng.", "assets/menu/affogato.png");
        addMenu(data, "Butter Croissant", "Bánh", 36000, true, "Bánh sừng bò bơ giòn xốp, thơm ngon.", "assets/menu/butter_croissant.png");
        addMenu(data, "Matcha Latte", "Trà", 54000, true, "Matcha latte đậm vị, ít ngọt.", "assets/menu/matcha_latte.png");
        addMenu(data, "Mocha Nóng", "Cà phê", 51000, true, "Socola và espresso cân bằng ngọt đắng.", "assets/menu/mocha_nong.png");
        addMenu(data, "Blue Ocean Soda", "Nước trái cây", 43000, true, "Soda xanh mát lạnh, màu sắc bắt mắt.", "assets/menu/blue_ocean_soda.png");
        addMenu(data, "Tiramisu Cup", "Dessert", 45000, true, "Tiramisu ly nhỏ mềm thơm, vị cà phê nhẹ.", "assets/menu/tiramisu_cup.png");
        addMenu(data, "Choco Mint", "Đá xay", 52000, true, "Chocolate bạc hà mát lạnh, dễ uống.", "assets/menu/choco_mint.png");
        addMenu(data, "Yuzu Sparkling", "Nước trái cây", 47000, true, "Yuzu sparkling thanh chua, rất sảng khoái.", "assets/menu/espresso_tonic.png");
        addMenu(data, "Bánh Red Velvet", "Bánh", 46000, true, "Bánh Red Velvet mềm ẩm, thơm kem phô mai.", "assets/menu/banh_red_velvet.png");
        addMenu(data, "Cappuccino", "Cà phê", 48000, true, "Cappuccino foam dày, espresso đậm vừa phải.", "assets/menu/cappuccino.png");

        for (int i = 1; i <= 16; i++) {
            data.getTables().add(new CafeTable(data.nextTableId(), "Bàn " + i, (i % 4) + 2, TableStatus.AVAILABLE));
        }
        data.getTables().add(new CafeTable(data.nextTableId(), "Mang đi", 1, TableStatus.AVAILABLE));

        AppSettings settings = data.getSettings();
        settings.setBusinessName("KIỆT NAM CAFÉ LUXURY");
        settings.setSlogan("Không gian cà phê đẳng cấp • Phục vụ nhanh • Quản lý thông minh");
        settings.setInvoiceFooter("Cảm ơn bạn đã ghé Anh Kiệt CAFÉ. Chúc bạn có một ngày thật nhiều năng lượng và cảm hứng!");
        settings.setTaxRate(0.08);
        settings.setServiceChargeRate(0.05);
        settings.setAccentColorHex("#744D32");
        data.syncCounters();
        return data;
    }

    private void normalize(AppData data) {
        while (data.getTables().stream().filter(t -> t.getName().startsWith("Bàn ")).count() < 16) {
            int i = (int) data.getTables().stream().filter(t -> t.getName().startsWith("Bàn ")).count() + 1;
            data.getTables().add(new CafeTable(data.nextTableId(), "Bàn " + i, (i % 4) + 2, TableStatus.AVAILABLE));
        }
        boolean hasTakeAway = data.getTables().stream().anyMatch(t -> t.getName().equalsIgnoreCase("Mang đi"));
        if (!hasTakeAway) {
            data.getTables().add(new CafeTable(data.nextTableId(), "Mang đi", 1, TableStatus.AVAILABLE));
        }
        if (data.getSettings() == null) data.setSettings(new AppSettings());
        data.getMenuItems().forEach(item -> { if (item.getImageUrl() == null) item.setImageUrl(""); });
        normalizeMenuImages(data);
        ensureManagerAccount(data);
        data.getSettings().setBusinessName("KIỆT NAM CAFÉ LUXURY");
        data.syncCounters();
    }

    private void normalizeMenuImages(AppData data) {
        for (MenuItem item : data.getMenuItems()) {
            String normalized = item.getName() == null ? "" : item.getName().trim().toLowerCase();
            switch (normalized) {
                case "butter croissant" -> item.setImageUrl("assets/menu/butter_croissant.png");
                case "bánh red velvet" -> item.setImageUrl("assets/menu/banh_red_velvet.png");
                case "cheese croffle" -> item.setImageUrl("assets/menu/cheese_croffle.png");
                case "chocolate mousse" -> item.setImageUrl("assets/menu/chocolate_mousse.png");
                case "aurora latte" -> item.setImageUrl("assets/menu/aurora_latte.png");
                case "bạc xỉu kem mặn" -> item.setImageUrl("assets/menu/bac_xiu_kem_man.png");
                case "cappuccino" -> item.setImageUrl("assets/menu/cappuccino.png");
                case "cold brew cam" -> item.setImageUrl("assets/menu/cold_brew_cam.png");
                case "mocha nóng" -> item.setImageUrl("assets/menu/mocha_nong.png");
                case "affogato" -> item.setImageUrl("assets/menu/affogato.png");
                case "tiramisu cup" -> item.setImageUrl("assets/menu/tiramisu_cup.png");
                case "blue ocean soda" -> item.setImageUrl("assets/menu/blue_ocean_soda.png");
                case "mint lemon soda" -> item.setImageUrl("assets/menu/mint_lemon_soda.png");
                case "yuzu sparkling" -> item.setImageUrl("assets/menu/yuzu_sparkling.png");
                case "coco coffee" -> item.setImageUrl("assets/menu/coco_coffee.png");
                case "cà phê muối" -> item.setImageUrl("assets/menu/ca_phe_muoi.png");
                case "espresso tonic" -> item.setImageUrl("assets/menu/espresso_tonic.png");
                case "sakura milk" -> item.setImageUrl("assets/menu/sakura_milk.png");
                case "matcha cloud" -> item.setImageUrl("assets/menu/matcha_cloud.png");
                case "matcha latte" -> item.setImageUrl("assets/menu/matcha_latte.png");
                case "oolong peach tea" -> item.setImageUrl("assets/menu/oolong_peach_tea.png");
                case "trà vải hoa hồng" -> item.setImageUrl("assets/menu/tra_vai_hoa_hong.png");
                case "berry yogurt" -> item.setImageUrl("assets/menu/berry_yogurt.png");
                case "caramel frappe" -> item.setImageUrl("assets/menu/caramel_frappe.png");
                case "choco mint" -> item.setImageUrl("assets/menu/choco_mint.png");
            }
        }
    }

    private void ensureManagerAccount(AppData data) {
        String hiddenUsername = "quanly";
        String hiddenPassword = "KNLuxury@2026";
        User manager = data.getUsers().stream().filter(u -> u.getRole() == Role.ADMIN).findFirst().orElse(null);
        if (manager == null) {
            data.getUsers().add(new User(data.nextUserId(), "QUẢN LÝ KIỆT NAM CAFÉ LUXURY", hiddenUsername, hiddenPassword, Role.ADMIN, true));
        } else {
            manager.setFullName("QUẢN LÝ KIỆT NAM CAFÉ LUXURY");
            manager.setUsername(hiddenUsername);
            manager.setPassword(hiddenPassword);
            manager.setActive(true);
        }
    }

    private void addMenu(AppData data, String name, String category, double price, boolean available, String description, String imageUrl) {
        data.getMenuItems().add(new MenuItem(data.nextMenuItemId(), name, category, price, available, description, imageUrl));
    }
}
