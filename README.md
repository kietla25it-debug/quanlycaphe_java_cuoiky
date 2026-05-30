# Anh Kiệt CAFÉ Desktop (JDK 26 + MySQL)

## Điểm mới
- Có hỗ trợ kết nối **MySQL** qua JDBC.
- Nút **Chấm công** đã gộp cả:
  - chấm công vào/ra ca
  - ca làm hôm nay
  - lịch sử đăng nhập / đăng xuất
- Nút **Đăng xuất** nổi bật hơn các nút khác.
- Màn hình **Order** có thêm ảnh món từ internet cho các món trong phần chọn món nhanh và gọi món bằng danh sách.
- Chỉ **tài khoản quản lý** mới đổi được **thuế** và **phí dịch vụ**.
- Có thống kê **doanh thu hôm nay** và **doanh thu tháng này**.
- Khi **Thanh toán**, app hiện hóa đơn ngay để chọn **In hóa đơn** hoặc **Không in**. Dù chọn gì, hóa đơn vẫn được lưu vào hệ thống và file TXT.

## Tài khoản quản lý ẩn trong code
- Username: `ak_manager_2026`
- Password: `AKCafe@QuanLy26`

## Cấu hình MySQL
Sửa file `config/mysql.properties` nếu cần:
- host
- port
- database
- username
- password

Mặc định:
- host = `localhost`
- port = `3306`
- database = `anh_kiet_cafe`
- username = `root`
- password = rỗng

## Chạy bằng file bat
1. Cài **JDK 26**
2. Bảo đảm máy có internet lần đầu để script tải `mysql-connector-j-9.6.0.jar`
3. Mở file `run.bat`

## Chạy bằng VS Code
1. Mở thư mục project
2. Cài **Extension Pack for Java**
3. Mở file `src/auroracafe/AuroraCafeApp.java`
4. Nếu muốn chạy MySQL, chạy `scripts/fetch_mysql_driver.bat` một lần để tải driver vào thư mục `lib`
5. Nhấn **Run Java**

## SQL mẫu
File `sql/schema_mysql.sql` đã có sẵn để bạn import thủ công nếu muốn.


## Cấu hình MySQL mặc định
Ứng dụng đã được đặt sẵn để kết nối MySQL với cấu hình:
- host: localhost
- port: 3306
- database: cofeManager
- username: root
- password: 1234

Nếu bạn dùng đúng cấu hình này thì chỉ cần chạy app.
