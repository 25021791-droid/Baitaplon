package com.auction.server.service;

import com.auction.common.model.Auction;
import com.auction.common.model.AuctionStatus;
import com.auction.common.model.Bid;
import com.auction.common.model.Bidder;
import java.util.ArrayList;
import java.util.List;

public class AuctionService {
    private static long nextAuctionId = 1;
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
        // Tự động gán ID nếu chưa có
        if (auction.getId() == null) {
            auction.setId(nextAuctionId++);
        }
        auctionList.add(auction);
        System.out.println("[AuctionService] Đã thêm auction ID=" + auction.getId() + ". Tổng số: " + auctionList.size());
        System.out.println("[AuctionService] Status: " + auction.getStatus());
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
    public synchronized List<Auction> getPendingAuctions() {
        List<Auction> pending = new ArrayList<>();
        System.out.println("[AuctionService] Tổng số auction trong hệ thống: " + auctionList.size());
        for (Auction a : auctionList) {
            System.out.println("[AuctionService] Auction " + a.getId() + " - Status: " + a.getStatus());
            if (a.getStatus() == AuctionStatus.ONQUEUE) {
                pending.add(a);
            }
        }
        System.out.println("[AuctionService] Số auction ONQUEUE: " + pending.size());
        return pending;
    }
    public synchronized List<Auction> getAuctionsBySellerId(int sellerId) {
        List<Auction> result = new ArrayList<>();
        for (Auction a : auctionList) {
            if (a.getSellerId() == sellerId) {
                result.add(a);
            }
        }
        return result;
    }
    public synchronized boolean approveAuction(long auctionId) {
        for (Auction a : auctionList) {
            if (a.getId() == auctionId && a.getStatus() == AuctionStatus.ONQUEUE) {
                a.setStatus(AuctionStatus.RUNNING);
                System.out.println("[Server] Admin đã duyệt + bắt đầu auction ID: " + auctionId);
                return true;
            }
        }
        return false;
    }
}