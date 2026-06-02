# Bài Tập Lớn: Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)

## 1. Mô tả ngắn gọn bài toán và phạm vi hệ thống
Hệ thống quản lý và tổ chức các phiên đấu giá trực tuyến theo mô hình **Client - Server**. Hệ thống cho phép nhiều đối tượng người dùng tương tác trong thời gian thực (Real-time), đảm bảo tính minh bạch, chính xác và bảo mật trong quá trình đấu giá các loại tài sản khác nhau.

**Phạm vi hệ thống bao gồm 3 phân hệ chính:**
* **Người quản trị (Admin):** Quản lý người dùng, giám sát các phiên đấu giá và xem thống kê hệ thống.
* **Người bán (Seller):** Đăng tải, quản lý các sản phẩm cần đấu giá thuộc nhiều danh mục (Nghệ thuật, Điện tử, Xe cộ).
* **Người mua (Bidder):** Tham gia vào các phòng đấu giá trực tuyến, trả giá công khai và theo dõi biểu đồ giá cả/xu hướng theo thời gian thực.

---

## 2. Công nghệ sử dụng và yêu cầu môi trường

### Công nghệ sử dụng:
* **Ngôn ngữ lập trình:** Java (Hỗ trợ Java mã nguồn mở / Java Modular System với `module-info.java`).
* **Giao diện người dùng (GUI):** JavaFX (kết hợp FXML và CSS để thiết kế giao diện).
* **Quản lý dự án & Build công cụ:** Maven (Cấu trúc Multi-module).
* **Kiến trúc mạng:** Java Socket Programming (TCP/IP) hỗ trợ xử lý đa luồng (`ClientHandler`).
* **Thiết kế mẫu (Design Patterns):** Observer Pattern (Cập nhật trạng thái phòng đấu giá và giao diện người dùng thời gian thực).

### Yêu cầu cài đặt môi trường:
* **Java Development Kit (JDK):** Phiên bản 11 hoặc mới hơn (Khuyến nghị JDK 17 hoặc 21).
* **Apache Maven:** Phiên bản 3.6 trở lên.
* **Cơ sở dữ liệu:** (Thêm tên DB bạn dùng vào đây, ví dụ: MySQL / SQLite / PostgreSQL).

---

## 3. Cấu trúc thư mục dự án

Dự án được chia thành 3 module Maven chính để đảm bảo tính đóng gói và tái sử dụng mã nguồn:

```text
Baitaplon/
├── auction_common/               # Module chứa các thành phần dùng chung cho cả Client và Server
│   └── src/main/java/com/auction/common/
│       ├── model/                # Các thực thể dữ liệu (Admin, Art, Auction, Bid, User, Vehicle,...)
│       └── observer/             # Interface định nghĩa mẫu thiết kế Observer (BidObserver)
│
├── auction_server/               # Module xử lý Logic phía Server và kết nối Cơ sở dữ liệu
│   └── src/main/java/com/auction/server/
│       ├── network/              # Quản lý Socket kết nối (AuctionServer, ClientHandler)
│       ├── observer/             # Log và theo dõi lịch sử đấu giá (BidLogger)
│       ├── service/              # Xử lý nghiệp vụ và DB (AuctionService, DatabaseService, UserService,...)
│       └── ServerMain.java       # Lớp kích hoạt (Entry Point) chạy Server
│
└── auction_client/               # Module giao diện người dùng phía Client (JavaFX)  
    └── src/main/
        ├── java/com/auction/client/
        │   ├── controller/       # Bộ điều khiển giao diện (Login, Register, BiddingRoom, RealtimeChart,...)
        │   ├── service/          # Kết nối truyền thông với Server (NetworkClientService)
        │   ├── utils/            # Tiện ích bổ trợ (DialogUtils, UserSession)
        │   └── AppLauncher.java  # Lớp khởi chạy ứng dụng Client
        └── resources/com/auction/# File thiết kế giao diện (.fxml) và định dạng giao diện (.css)
