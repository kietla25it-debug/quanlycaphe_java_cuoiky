-- =========================================================
-- DATABASE MYSQL CHO UNG DUNG KIET NAM CAFE LUXURY
-- Dung voi code Java hien tai: AuroraCafeApp / MySqlStore
-- Database: cofeManager
--
-- File nay tao day du bang de luu:
-- 1. Cau hinh ung dung
-- 2. Tai khoan nguoi dung
-- 3. Menu mon
-- 4. Ban cafe
-- 5. Order / hoa don
-- 6. Chi tiet mon trong order
-- 7. Cham cong
-- 8. Lich su dang nhap / dang xuat
--
-- Luu y ve hoa don:
-- - MySQL luu du lieu order trong bang orders va order_items.
-- - File hoa don .txt do code Java tu luu ra thu muc exports/invoices
--   khi ban bam thanh toan trong ung dung.
--
-- CANH BAO:
-- File nay se XOA database cofeManager cu va TAO LAI TU DAU.
-- Neu co du lieu quan trong, hay backup truoc khi chay.
-- =========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS cofeManager;

CREATE DATABASE cofeManager
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE cofeManager;

-- =========================================================
-- 1. BANG CAU HINH UNG DUNG
-- =========================================================
CREATE TABLE app_settings (
    id TINYINT PRIMARY KEY,
    business_name VARCHAR(255),
    slogan VARCHAR(255),
    tax_rate DOUBLE,
    service_charge_rate DOUBLE,
    invoice_footer TEXT,
    accent_color_hex VARCHAR(32)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 2. BANG TAI KHOAN NGUOI DUNG
-- Role trong code chi nhan: ADMIN, STAFF
-- =========================================================
CREATE TABLE users (
    id INT PRIMARY KEY,
    full_name VARCHAR(255),
    username VARCHAR(120) UNIQUE,
    password VARCHAR(255) COMMENT 'Luu password da hash dang sha256$salt$hash, khong luu plain text',
    role VARCHAR(20),
    active BOOLEAN
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 3. BANG MENU MON
-- =========================================================
CREATE TABLE menu_items (
    id INT PRIMARY KEY,
    name VARCHAR(255),
    category VARCHAR(100),
    price DOUBLE,
    available BOOLEAN,
    description TEXT,
    image_url TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 4. BANG BAN CAFE
-- status trong code chi nhan: AVAILABLE, OCCUPIED, RESERVED
-- =========================================================
CREATE TABLE cafe_tables (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    seats INT,
    status VARCHAR(30),
    current_order_id INT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 5. BANG ORDER / HOA DON
-- status trong code chi nhan: OPEN, PAID, CANCELLED
-- =========================================================
CREATE TABLE orders (
    id INT PRIMARY KEY,
    table_id INT,
    table_name VARCHAR(100),
    created_by VARCHAR(255),
    created_at DATETIME,
    closed_at DATETIME NULL,
    status VARCHAR(20),
    payment_method VARCHAR(100),
    tax_rate DOUBLE,
    service_charge_rate DOUBLE,
    customer_name VARCHAR(255),
    CONSTRAINT fk_orders_table FOREIGN KEY (table_id) REFERENCES cafe_tables(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 6. BANG CHI TIET MON TRONG ORDER
-- Cot row_no BAT BUOC phai co.
-- Code Java doc bang lenh:
-- SELECT * FROM order_items ORDER BY order_id, row_no
-- =========================================================
CREATE TABLE order_items (
    order_id INT,
    row_no INT,
    menu_item_id INT,
    name VARCHAR(255),
    unit_price DOUBLE,
    quantity INT,
    note TEXT,
    PRIMARY KEY(order_id, row_no),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 7. BANG CHAM CONG
-- =========================================================
CREATE TABLE shift_records (
    id INT PRIMARY KEY,
    user_id INT,
    user_name VARCHAR(255),
    clock_in DATETIME,
    clock_out DATETIME NULL,
    CONSTRAINT fk_shift_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 8. BANG LICH SU DANG NHAP / DANG XUAT
-- =========================================================
CREATE TABLE auth_history (
    id INT PRIMARY KEY,
    user_id INT,
    full_name VARCHAR(255),
    username VARCHAR(120),
    role_name VARCHAR(20),
    login_at DATETIME,
    logout_at DATETIME NULL,
    CONSTRAINT fk_auth_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- DU LIEU MAU BAN DAU
-- =========================================================

INSERT INTO app_settings
(id, business_name, slogan, tax_rate, service_charge_rate, invoice_footer, accent_color_hex)
VALUES
(1,
 'KIỆT NAM CAFÉ LUXURY',
 'Không gian cà phê đẳng cấp • Phục vụ nhanh • Quản lý thông minh',
 0.08,
 0.05,
 'Cảm ơn bạn đã ghé Anh Kiệt CAFÉ. Chúc bạn có một ngày thật nhiều năng lượng và cảm hứng!',
 '#744D32');

-- Tai khoan quan ly mac dinh:
-- username: quanly
-- password demo: KNLuxury@2026 (trong DB da duoc hash, khong luu plain text)
INSERT INTO users
(id, full_name, username, password, role, active)
VALUES
(1, 'QUẢN LÝ KIỆT NAM CAFÉ LUXURY', 'quanly', 'sha256$S05MdXh1cnlTYWx0MjAyNg==$n5AD2n93PJEJ5BGQ8IIrxq3C8BgV3kJjNYaoiiN/bYE=', 'ADMIN', 1);

INSERT INTO menu_items
(id, name, category, price, available, description, image_url)
VALUES
(1, 'Aurora Latte', 'Cà phê', 49000, 1, 'Latte thơm béo với lớp sữa mịn và latte art đẹp mắt.', 'assets/menu/aurora_latte.png'),
(2, 'Cold Brew Cam', 'Cà phê', 55000, 1, 'Cold brew pha cùng syrup cam thanh mát, hậu vị dễ chịu.', 'assets/menu/cold_brew_cam.png'),
(3, 'Espresso Tonic', 'Signature', 59000, 1, 'Espresso mạnh mẽ kết hợp tonic mát lạnh, rất hợp ngày nóng.', 'assets/menu/espresso_tonic.png'),
(4, 'Americano Đá', 'Cà phê', 39000, 1, 'Americano đậm vị, ít ngọt, uống hằng ngày rất hợp.', 'assets/menu/aurora_latte.png'),
(5, 'Bạc Xỉu Kem Mặn', 'Cà phê', 46000, 1, 'Bạc xỉu thơm sữa cùng lớp kem mặn béo nhẹ.', 'assets/menu/bac_xiu_kem_man.png'),
(6, 'Cà phê Muối', 'Signature', 47000, 1, 'Cà phê đậm đà cân bằng giữa vị đắng nhẹ và kem mặn.', 'assets/menu/ca_phe_muoi.png'),
(7, 'Matcha Cloud', 'Trà', 52000, 1, 'Matcha Nhật cùng lớp kem cloud bồng bềnh, màu sắc đẹp mắt.', 'assets/menu/matcha_cloud.png'),
(8, 'Oolong Peach Tea', 'Trà', 45000, 1, 'Trà ô long đào dịu thơm, thanh mát dễ uống.', 'assets/menu/oolong_peach_tea.png'),
(9, 'Trà Vải Hoa Hồng', 'Trà', 48000, 1, 'Vải ngọt thanh hòa cùng hương hoa hồng nhẹ nhàng.', 'assets/menu/tra_vai_hoa_hong.png'),
(10, 'Sakura Milk', 'Signature', 58000, 1, 'Sữa hoa anh đào pastel dịu ngọt và nổi bật.', 'assets/menu/sakura_milk.png'),
(11, 'Chocolate Mousse', 'Bánh', 42000, 1, 'Mousse chocolate mềm mịn, ngọt vừa.', 'assets/menu/chocolate_mousse.png'),
(12, 'Cheese Croffle', 'Bánh', 47000, 1, 'Croffle nướng giòn, thơm bơ và phô mai.', 'assets/menu/cheese_croffle.png'),
(13, 'Berry Yogurt', 'Đá xay', 53000, 1, 'Sữa chua berry mát lạnh, vị trái cây dịu nhẹ.', 'assets/menu/berry_yogurt.png'),
(14, 'Mint Lemon Soda', 'Nước trái cây', 39000, 1, 'Chanh bạc hà tươi mát, giải nhiệt tốt.', 'assets/menu/mint_lemon_soda.png'),
(15, 'Caramel Frappe', 'Đá xay', 57000, 1, 'Đá xay caramel béo thơm, topping hấp dẫn.', 'assets/menu/caramel_frappe.png'),
(16, 'Coco Coffee', 'Signature', 56000, 1, 'Cà phê dừa mịn, thơm và béo vừa phải.', 'assets/menu/coco_coffee.png'),
(17, 'Affogato', 'Dessert', 62000, 1, 'Kem vanilla dùng cùng shot espresso nóng.', 'assets/menu/affogato.png'),
(18, 'Butter Croissant', 'Bánh', 36000, 1, 'Bánh sừng bò bơ giòn xốp, thơm ngon.', 'assets/menu/butter_croissant.png'),
(19, 'Matcha Latte', 'Trà', 54000, 1, 'Matcha latte đậm vị, ít ngọt.', 'assets/menu/matcha_latte.png'),
(20, 'Mocha Nóng', 'Cà phê', 51000, 1, 'Socola và espresso cân bằng ngọt đắng.', 'assets/menu/mocha_nong.png'),
(21, 'Blue Ocean Soda', 'Nước trái cây', 43000, 1, 'Soda xanh mát lạnh, màu sắc bắt mắt.', 'assets/menu/blue_ocean_soda.png'),
(22, 'Tiramisu Cup', 'Dessert', 45000, 1, 'Tiramisu ly nhỏ mềm thơm, vị cà phê nhẹ.', 'assets/menu/tiramisu_cup.png'),
(23, 'Choco Mint', 'Đá xay', 52000, 1, 'Chocolate bạc hà mát lạnh, dễ uống.', 'assets/menu/choco_mint.png'),
(24, 'Yuzu Sparkling', 'Nước trái cây', 47000, 1, 'Yuzu sparkling thanh chua, rất sảng khoái.', 'assets/menu/espresso_tonic.png'),
(25, 'Bánh Red Velvet', 'Bánh', 46000, 1, 'Bánh Red Velvet mềm ẩm, thơm kem phô mai.', 'assets/menu/banh_red_velvet.png'),
(26, 'Cappuccino', 'Cà phê', 48000, 1, 'Cappuccino foam dày, espresso đậm vừa phải.', 'assets/menu/cappuccino.png');

INSERT INTO cafe_tables
(id, name, seats, status, current_order_id)
VALUES
(1, 'Bàn 1', 3, 'AVAILABLE', NULL),
(2, 'Bàn 2', 4, 'AVAILABLE', NULL),
(3, 'Bàn 3', 5, 'AVAILABLE', NULL),
(4, 'Bàn 4', 2, 'AVAILABLE', NULL),
(5, 'Bàn 5', 3, 'AVAILABLE', NULL),
(6, 'Bàn 6', 4, 'AVAILABLE', NULL),
(7, 'Bàn 7', 5, 'AVAILABLE', NULL),
(8, 'Bàn 8', 2, 'AVAILABLE', NULL),
(9, 'Bàn 9', 3, 'AVAILABLE', NULL),
(10, 'Bàn 10', 4, 'AVAILABLE', NULL),
(11, 'Bàn 11', 5, 'AVAILABLE', NULL),
(12, 'Bàn 12', 2, 'AVAILABLE', NULL),
(13, 'Bàn 13', 3, 'AVAILABLE', NULL),
(14, 'Bàn 14', 4, 'AVAILABLE', NULL),
(15, 'Bàn 15', 5, 'AVAILABLE', NULL),
(16, 'Bàn 16', 2, 'AVAILABLE', NULL),
(17, 'Mang đi', 1, 'AVAILABLE', NULL);

-- De trong cac bang nay de ung dung tu luu du lieu khi su dung:
-- orders, order_items, shift_records, auth_history

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- HUONG DAN SAU KHI CHAY FILE NAY
-- =========================================================
-- 1. Mo file config/mysql.properties trong project Java:
--    enabled=true
--    host=localhost
--    port=3306
--    database=cofeManager
--    username=root
--    password=MAT_KHAU_MYSQL_CUA_BAN
--    autoCreateSchema=true
--
-- 2. Chay MySqlConnectionTest de test ket noi.
-- 3. Chay AuroraCafeApp.
-- 4. Khi thanh toan order, du lieu se luu vao MySQL,
--    con file hoa don se duoc ung dung luu trong exports/invoices.
-- =========================================================
