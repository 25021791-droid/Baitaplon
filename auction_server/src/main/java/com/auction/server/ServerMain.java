package com.auction.server;

import com.auction.server.network.AuctionServer;
import com.auction.server.service.UserService;
public class ServerMain {
    public static void main(String[] args) {
        UserService userService = new UserService();
        boolean created = userService.register("datct", "dat", "admin@auction.com", "ADMIN");
        if (created) {
            System.out.println(">>> Đã tạo tài khoản Admin mặc định: admin / admin123");
        } else {
            System.out.println(">>> Tài khoản Admin đã tồn tại hoặc có lỗi khi tạo.");
        }
        int port = 8080;

        System.out.println("ĐANG KHỞI CHẠY AUCTION SERVER");

        AuctionServer server = new AuctionServer();
        server.start(port);
    }
}