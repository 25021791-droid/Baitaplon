package com.auction.server.service;

import com.auction.common.model.Auction;
import com.auction.common.model.AuctionStatus;
import com.auction.common.model.Bid;
import com.auction.common.model.Bidder;
import java.util.ArrayList;
import java.util.List;

public class AuctionService {
    private final AuctionRepository auctionRepository = new AuctionRepository();

    private final BidService bidService = new BidService();
    private final UserService userService = new UserService();

    // -- Lấy danh sách auction đang chạy
    public synchronized List<Auction> getActiveAuctions() {
        return auctionRepository.getAuctionsByStatus(AuctionStatus.RUNNING);
    }

    // -- Lấy danh sách auction đang chờ duyệt
    public synchronized List<Auction> getPendingAuctions() {
        return auctionRepository.getAuctionsByStatus(AuctionStatus.ONQUEUE);
    }

    // -- Lấy danh sách auction đã end
    public synchronized List<Auction> getEndedAuctions() {
        return auctionRepository.getAuctionsByStatus(AuctionStatus.FINISHED);
    }

    // -- Lấy danh sách auction theo người bán
    public synchronized List<Auction> getAuctionsBySellerId(int sellerId) {
        return auctionRepository.getAuctionsBySellerId(sellerId);
    }

    // -- Thêm auction vào DB
    public synchronized void addAuction(Auction auction) {
        boolean success = auctionRepository.addAuctionToRepo(auction);
        if (success) {
            System.out.println("[AuctionService] Đã lưu thành công auction ID=" + auction.getId() + " vào DB.");
        } else {
            System.out.println("[AuctionService] Thất bại khi lưu auction vào DB!");
        }
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

    public synchronized boolean approveAuction(long auctionId) {
        boolean success = auctionRepository.updateStatus(auctionId, AuctionStatus.RUNNING);
        if (success) {
            System.out.println("[Server] Admin đã duyệt + bắt đầu dữ liệu trong DB cho auction ID: " + auctionId);
        }
        return success;
    }

}