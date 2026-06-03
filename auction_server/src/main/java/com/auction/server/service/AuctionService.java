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

    // -- Lấy object auction theo id
    public synchronized Auction getAuctionById(int auctionId) {
        Auction auction = auctionRepository.getAuctionById(auctionId);

        if (auction != null) {
            List<Bid> bidList = auctionRepository.getBidsByAuctionId(auctionId);
            auction.setBids(bidList);
        }
        return auction;
    }

    // -- Cập nhật giá auction
    public synchronized boolean updateCurrentPrice(Auction auction, double amount) {
        boolean isPriceUpdated = auctionRepository.updateCurrentPrice(auction.getId(), amount);
        if (isPriceUpdated) {
            System.out.println("[AuctionService] Cập nhật giá tiền thành công!");
            return true;
        } else {
            System.out.println("[AuctionService] Lỗi khi cập nhật giá tiền!");
        }
        return false;
    }

    // -- Thêm auction vào DB
    public synchronized boolean addAuction(Auction auction) {
        boolean success = auctionRepository.addAuctionToRepo(auction);
        if (success) {
            System.out.println("[AuctionService] Đã lưu thành công auction ID=" + auction.getId() + " vào DB.");
            return true;
        } else {
            System.out.println("[AuctionService] Thất bại khi lưu auction vào DB!");
        }
        return false;
    }

    // -- Thêm bid vào DB
    public synchronized boolean addBid(int auctionId, Bid bid) {
        boolean success = auctionRepository.addBidToRepo(auctionId, bid);
        if (success) {
            System.out.println("[AuctionService] Đã lưu thành công bid vào DB.");
            return true;
        } else {
            System.out.println("[AuctionService] Thất bại khi lưu bid vào DB!");
        }
        return false;
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

    public synchronized boolean cancelAuction(int auctionId) {
        boolean success = auctionRepository.updateStatus(auctionId, AuctionStatus.CANCELED);
        if (success) {
            System.out.println("Auction canceled!");
        }
        return true;
    }

    public synchronized boolean approveAuction(int auctionId) {
        boolean success = auctionRepository.updateStatus(auctionId, AuctionStatus.RUNNING);
        if (success) {
            System.out.println("[Server] Admin đã duyệt + bắt đầu dữ liệu trong DB cho auction ID: " + auctionId);
        }
        return success;
    }

}