package com.auction.service;

import com.auction.model.Auction;
import com.auction.model.Bid;
import com.auction.observer.BidObserver;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BidService {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8080;

    private final List<BidObserver> observers = new ArrayList<>();

    public void addObserver(BidObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(String message) {
        for (BidObserver observer : observers) {
            observer.update(message);
        }
    }

    public boolean placeBid(Auction auction, Bid bid) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            String request = "BID," + bid.getBidder().getId() + ","
                    + auction.getId() + "," + bid.getAmount();
            out.writeUTF(request);

            String response = in.readUTF();

            if ("SUCCESS".equals(response)) {
                notifyObservers("Đặt giá thành công: " + bid.getAmount());
                return true;
            } else {
                notifyObservers("Đặt giá thất bại hoặc bị người khác ra giá cao hơn!");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            notifyObservers("Lỗi kết nối máy chủ!");
            return false;
        }
    }
}
