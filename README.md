# Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)

Hệ thống quản lý và tổ chức các phiên đấu giá trực tuyến được xây dựng theo mô hình kiến trúc **Client - Server**. Hệ thống cho phép nhiều đối tượng người dùng tương tác trong thời gian thực, đảm bảo tính minh bạch, chính xác và tối ưu hóa hiệu năng trong quá trình đấu giá tài sản.

---

## 1. Mô tả bài toán và phạm vi hệ thống

### Mô tả bài toán
Tổ chức đấu giá truyền thống thường gặp khó khăn về địa lý và giới hạn lượng người tham gia. Hệ thống đấu giá trực tuyến giải quyết bài toán này bằng cách cung cấp sàn đấu giá ảo chạy qua mạng socket, cho phép người dùng đăng ký, tạo phiên đấu giá, nạp tiền và đấu giá trực tiếp theo thời gian thực (Real-time).

### Phạm vi hệ thống
Hệ thống bao gồm 3 phân hệ người dùng chính:
* **Người quản trị (Admin):** Duyệt các phiên đấu giá chờ đăng tải, hủy phiên đấu giá và quản lý người dùng.
* **Người bán (Seller):** Tạo phiên đấu giá mới, tải lên hình ảnh sản phẩm dưới dạng Base64, theo dõi trạng thái các sản phẩm của mình.
* **Người mua (Bidder):** Xem danh sách sản phẩm đang đấu giá (tự động cập nhật), vào phòng đấu giá trực tuyến chi tiết, đặt giá trực tiếp, theo dõi biểu đồ lịch sử giá thời gian thực và quản lý tài khoản/ví tiền.

---

## 2. Công nghệ sử dụng và yêu cầu cài đặt

### Công nghệ sử dụng:
* **Ngôn ngữ lập trình:** Java (hỗ trợ Java Modular System - JDK 17+).
* **Giao diện người dùng (GUI):** JavaFX (kết hợp FXML và CSS tối ưu hóa trải nghiệm người dùng).
* **Quản lý dự án & Build tool:** Maven (cấu trúc Multi-module tách biệt rõ ràng trách nhiệm).
* **Giao thức truyền thông:** Java Sockets (TCP/IP) hỗ trợ đa luồng chuyên biệt cho mỗi Client (`ClientHandler`).
* **Cơ sở dữ liệu:** MySQL (sử dụng dịch vụ điện toán đám mây Aiven Cloud).
* **Tối ưu hóa Database:** Tự xây dựng **Connection Pool** kết hợp cơ chế **Dynamic Proxy** giúp loại bỏ độ trễ handshake TCP/SSL, tối ưu hóa tốc độ phản hồi từ hàng chục giây xuống dưới `100ms`.
* **Thiết kế mẫu (Design Patterns):** Observer Pattern (đồng bộ hóa dữ liệu thời gian thực).

### Yêu cầu cài đặt môi trường:
* **Java Development Kit (JDK):** Phiên bản **17** trở lên.
* **Apache Maven:** Phiên bản **3.6.0** trở lên.
* **Hệ điều hành:** Hỗ trợ đầy đủ tất cả hệ điều hành bao gồm **Windows, Linux, và macOS**.

---

## 3. Cấu trúc thư mục dự án

Dự án được chia làm 3 module Maven chính:

```text
Baitaplon/
├── auction_common/               # Module chứa các thực thể và mô hình dùng chung
│   └── src/main/java/com/auction/common/
│       ├── model/                # Thực thể (User, Bidder, Seller, Admin, Auction, Bid, Item)
│       └── observer/             # Định nghĩa cấu trúc Observer Pattern
│
├── auction_server/               # Module quản lý Server, Socket và Cơ sở dữ liệu
│   ├── src/main/java/com/auction/server/
│   │   ├── network/              # Máy chủ Socket (AuctionServer, ClientHandler)
│   │   ├── service/              # Xử lý nghiệp vụ & DB (DatabaseService, AuctionService, UserService)
│   │   └── ServerMain.java       # Lớp Entry Point khởi động Server
│   └── src/main/resources/       # File application.properties cấu hình DB
│
└── auction_client/               # Module giao diện người dùng JavaFX (Client)
    ├── src/main/java/com/auction/client/
    │   ├── controller/           # Điều khiển màn hình (Login, Register, BidderController,...)
    │   ├── service/              # Quản lý Socket gửi/nhận dữ liệu (NetworkClientService)
    │   └── ClientMain.java       # Lớp Entry Point khởi động giao diện Client
    └── src/main/resources/       # File thiết kế .fxml và file định dạng .css
```

---

## 4. Hướng dẫn chạy chương trình (Thứ tự cụ thể)

### Bước 1: Cấu hình Cơ sở dữ liệu (Nếu cần thiết)
Các tham số kết nối CSDL được cấu hình trong tệp:
`auction_server/src/main/resources/application.properties`

### Bước 2: Compile và cài đặt thư viện
Mở Terminal / Command Prompt tại thư mục gốc của dự án (`Baitaplon/`) và chạy lệnh sau để build toàn bộ các module:
```bash
mvn clean install
```

### Bước 3: Khởi chạy Server trước
Chạy lệnh sau tại thư mục gốc để khởi động Server đấu giá:
```bash
mvn exec:java -pl auction_server -Dexec.mainClass="com.auction.server.ServerMain"
```
*Lưu ý: Lệnh này hoạt động tương thích trên cả **Windows, Linux và macOS**.*

### Bước 4: Khởi chạy Client(s)
Sau khi Server đã chạy thành công, mở một cửa sổ Terminal mới tại thư mục gốc và chạy lệnh sau để mở Client:
```bash
mvn javafx:run -pl auction_client
```
*Bạn có thể mở nhiều cửa sổ Terminal và chạy lệnh trên nhiều lần để giả lập nhiều người dùng cùng tham gia đấu giá đồng thời.*

---

## 5. Danh sách các chức năng đã hoàn thành

- [x] **Xử lý kết nối đồng thời:** Thiết lập kiến trúc mạng đa luồng trên socket TCP, cho phép hàng trăm client kết nối cùng lúc mà không bị nghẽn mạng.
- [x] **Đăng ký / Đăng nhập phân quyền:** Người dùng đăng ký tài khoản với các vai trò lựa chọn (Bidder/Seller) và được mã hóa mật khẩu bảo mật.
- [x] **Đăng ký sản phẩm (Seller):** Người bán có thể đăng bán sản phẩm mới kèm tên, giá khởi điểm và hình ảnh thực tế được mã hóa Base64 lưu trữ trực tiếp.
- [x] **Duyệt phiên đấu giá (Admin):** Quản trị viên kiểm tra danh sách chờ và duyệt để bắt đầu mở phiên đấu giá cho cộng đồng.
- [x] **Tự động làm mới danh sách (Auto-refresh):** Giao diện danh sách phiên đấu giá tự động tải lại định kỳ 5 giây một lần để đồng bộ trạng thái thực tế.
- [x] **Tạm dừng Auto-refresh thông minh:** Khi người dùng đang nhập tiền và thực hiện lệnh đấu giá, hệ thống sẽ tạm dừng việc tự động tải lại danh sách để tránh xung đột UI và tự động phục hồi khi có phản hồi.
- [x] **Đấu giá trực tiếp (Real-time bidding):** Người mua đặt giá cao hơn giá hiện tại, kiểm tra số dư ví tài khoản ngay lập tức.
- [x] **Tối ưu hóa phản hồi (Database Connection Pool):** Giảm thiểu thời gian truy xuất CSDL đám mây từ hơn 10 giây xuống **dưới 100ms** thông qua Pool kết nối ủy quyền động.
- [x] **Đồng bộ hóa phiên đấu giá kết thúc:** Luồng quét chạy ngầm trên Server tự động khóa phiên đấu giá khi hết thời gian, tìm ra người chiến thắng và thực hiện trừ tiền trong ví.

---

## 6. Tài liệu báo cáo & Video trình diễn

* **Link báo cáo PDF chi tiết:** [Xem tài liệu báo cáo tại đây (Thay link của bạn vào đây)](#)
* **Link video demo chương trình:** [Xem video demo chạy thực tế tại đây (Thay link của bạn vào đây)](#)
