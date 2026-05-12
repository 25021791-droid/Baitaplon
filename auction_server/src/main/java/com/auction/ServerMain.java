package com.auction;

import com.auction.network.AuctionServer;

public class ServerMain {
    public static void main(String[] args) {
        int port = 8080;

        System.out.println("--- ĐANG KHỞI CHẠY AUCTION SERVER ---");

        AuctionServer server = new AuctionServer();
        server.start(port);
    }
}