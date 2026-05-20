package com.auction.server.service;

import com.auction.common.model.Auction;
import com.auction.common.model.AuctionStatus;
import com.auction.common.model.Bid;
import com.auction.common.model.Bidder;
import java.util.ArrayList;
import java.util.List;

public class AuctionService {

    private static final List<Auction> auctionList = new ArrayList<>();

    private final BidService bidService = new BidService();
    private final UserService userService = new UserService();

    public synchronized List<Auction> getActiveAuctions() {
        List<Auction> activeAuctions = new ArrayList<>();
        for (Auction auction : auctionList) {
            if (auction.getStatus() == AuctionStatus.RUNNING) {
                activeAuctions.add(auction);
            }
        }
        return activeAuctions;
    }

    public synchronized void addAuction(Auction auction) {
        auctionList.add(auction);
        System.out.println("Đã thêm phiên đấu giá mới vào hệ thống hệ thống!");
    }

    public synchronized boolean startAuction(Auction auction) {
        if (auction.getStatus() != AuctionStatus.OPEN) {
            System.out.println("Auction must be OPEN to start!");
            return false;
        }

        auction.setStatus(AuctionStatus.RUNNING);
        System.out.println("Auction started successfully!");
        return true;
    }

    public synchronized void endAuction(Auction auction) {
        if (auction.getStatus() != AuctionStatus.RUNNING) {
            System.out.println("Only RUNNING auctions can be ended!");
            return;
        }

        auction.setStatus(AuctionStatus.FINISHED);
        System.out.println("Auction finished!");

        Bid winner = bidService.getWinner(auction);
        if (winner != null) {
            System.out.println("Winner found: " + winner.getBidder().getName());

            Bidder bidder = winner.getBidder();
            double newBalance = bidder.getBalance() - winner.getAmount();

            bidder.setBalance(newBalance);

        } else {
            System.out.println("No one participated in this auction.");
        }
    }

    public synchronized boolean payAuction(Auction auction) {
        if (auction.getStatus() != AuctionStatus.FINISHED) {
            System.out.println("Auction must be FINISHED to pay!");
            return false;
        }

        auction.setStatus(AuctionStatus.PAID);
        System.out.println("Auction marked as PAID!");
        return true;
    }

    public synchronized boolean cancelAuction(Auction auction) {
        if (auction.getStatus() == AuctionStatus.FINISHED ||
                auction.getStatus() == AuctionStatus.PAID) {
            System.out.println("Cannot cancel finished or paid auction!");
            return false;
        }

        auction.setStatus(AuctionStatus.CANCELED);
        System.out.println("Auction canceled!");
        return true;
    }
}