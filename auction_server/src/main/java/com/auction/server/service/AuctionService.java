package com.auction.server.service;

import com.auction.common.model.Auction;
import com.auction.common.model.AuctionStatus;
import com.auction.common.model.Bid;
import com.auction.common.model.Bidder;
import com.auction.common.model.User;
import java.util.ArrayList;
import java.util.List;

public class AuctionService {
    private final AuctionRepository auctionRepository = new AuctionRepository();

    private final BidService bidService = new BidService();
    private final UserService userService = new UserService();

    public synchronized boolean placeBid(long auctionId, int bidderId, double bidAmount) {
        System.out.println("[AuctionService] Đang xử lý đặt giá cho phiên: " + auctionId + " bởi User: " + bidderId);

        Auction auction = auctionRepository.getAuctionById(auctionId);
        if (auction == null) {
            System.out.println("[AuctionService] Không tìm thấy phiên đấu giá với ID: " + auctionId);
            return false;
        }

        
        if (auction.getStatus() != AuctionStatus.RUNNING) {
            System.out.println("[AuctionService] Đặt giá thất bại: Phiên đấu giá không ở trạng thái RUNNING.");
            return false;
        }

        
        if (bidAmount <= auction.getCurrentPrice()) {
            System.out.println("[AuctionService] Đặt giá thất bại: Giá đặt mua phải lớn hơn giá hiện tại.");
            return false;
        }

        
        User user = userService.getUserById(bidderId); 
        if (user instanceof Bidder) {
            Bidder bidder = (Bidder) user;
            if (bidAmount > bidder.getBalance()) {
                System.out.println("[AuctionService] Đặt giá thất bại: Tài khoản người mua không đủ số dư.");
                return false;
            }

            
            auction.setCurrentPrice(bidAmount);

            try {
                Bid newBid = new Bid(bidder, bidAmount, java.time.LocalDateTime.now());
            } catch (Exception e) {
                System.out.println("[AuctionService] Bỏ qua ghi log chi tiết Bid: " + e.getMessage());
            }

            
            
            boolean isUpdated = auctionRepository.updateCurrentPrice(auctionId, bidAmount);
            if (!isUpdated) {
                
                isUpdated = auctionRepository.saveOrUpdate(auction);
            }

            System.out.println("[AuctionService] Cập nhật giá mới thành công lên hệ thống: đ " + bidAmount);
            return true;
        }

        return false;
    }

    
    public synchronized List<Auction> getActiveAuctions() {
        return auctionRepository.getAuctionsByStatus(AuctionStatus.RUNNING);
    }

    
    public synchronized List<Auction> getPendingAuctions() {
        return auctionRepository.getAuctionsByStatus(AuctionStatus.ONQUEUE);
    }

    
    public synchronized List<Auction> getEndedAuctions() {
        return auctionRepository.getAuctionsByStatus(AuctionStatus.FINISHED);
    }

    
    public synchronized List<Auction> getAuctionsBySellerId(int sellerId) {
        return auctionRepository.getAuctionsBySellerId(sellerId);
    }

    
    public synchronized Auction getAuctionById(int auctionId) {
        Auction auction = auctionRepository.getAuctionById(auctionId);

        if (auction != null) {
            List<Bid> bidList = auctionRepository.getBidsByAuctionId(auctionId);
            auction.setBids(bidList);
        }
        return auction;
    }

    
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