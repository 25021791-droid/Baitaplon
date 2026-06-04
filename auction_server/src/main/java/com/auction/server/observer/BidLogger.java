package com.auction.server.observer;

import com.auction.common.model.Bid;
import com.auction.common.observer.BidObserver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BidLogger implements BidObserver {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public void update(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.println("[AUDIT LOG | " + timestamp + "] " + message);
    }

    public void update(Bid newBid) {
        if (newBid == null || newBid.getBidder() == null) return;

        String timestamp = LocalDateTime.now().format(FORMATTER);
        String bidderName = newBid.getBidder().getName();
        double amount = newBid.getAmount();

        System.out.printf("[BID LOG | %s] Tài khoản '%s' vừa đặt giá: %.0f VNĐ%n",
                timestamp, bidderName, amount);
    }
}