package com.auction.server;

import com.auction.server.network.AuctionServer;
import com.auction.server.service.UserService;
public class ServerMain {
    public static void main(String[] args) {

        System.out.println(">>> Tài khoản Admin mặc định: username:admin , pass:123");

        int port = 8080;

        System.out.println("ĐANG KHỞI CHẠY AUCTION SERVER");

        AuctionServer server = new AuctionServer();
        server.start(port);
    }
}