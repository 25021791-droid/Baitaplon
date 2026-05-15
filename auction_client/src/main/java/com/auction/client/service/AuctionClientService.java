package com.auction.client.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class AuctionClientService {
    private static AuctionClientService instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private AuctionClientService() {
        try {
            this.socket = new Socket("localhost", 8080);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AuctionClientService getInstance() {
        if (instance == null) instance = new AuctionClientService();
        return instance;
    }

    public boolean placeBid(long auctionId, int userId, double amount) {

        try {
            String message = String.format("BID|%d|%d|%.2f", auctionId, userId, amount);
            out.println(message);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void joinAuction(int auctionId) {
        out.println("JOIN_AUCTION:" + auctionId);
    }
}