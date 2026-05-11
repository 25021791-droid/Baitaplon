package com.auction.network;

import java.net.ServerSocket;
import java.net.Socket;

public class AuctionServer {
    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server đang chạy tại cổng: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Có thiết bị mới kết nối!");

                new ClientHandler(clientSocket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}