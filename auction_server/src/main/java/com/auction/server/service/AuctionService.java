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

            // Anti-sniping: Nếu cược đặt trong vòng 30 giây cuối cùng, gia hạn phiên thêm 30 giây
            if (auction.getEndTime() != null) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                java.time.Duration timeRemaining = java.time.Duration.between(now, auction.getEndTime());
                long secondsRemaining = timeRemaining.toSeconds();
                if (secondsRemaining > 0 && secondsRemaining <= 30) {
                    java.time.LocalDateTime newEndTime = auction.getEndTime().plusSeconds(30);
                    auction.setEndTime(newEndTime);
                    auctionRepository.updateTimes(auctionId, auction.getStartTime(), newEndTime);
                    System.out.println("[AuctionService] Kích hoạt Anti-sniping: Gia hạn phiên " + auctionId + " thêm 30 giây. Thời gian kết thúc mới: " + newEndTime);
                }
            }

            // Ghi nhận giá mới và lưu Bid vào DB mà không thực hiện trừ/hoàn trả tiền ngay lập tức
            auction.setCurrentPrice(bidAmount);

            Bid newBid = new Bid(bidder, bidAmount, java.time.LocalDateTime.now());
            try {
                auctionRepository.addBidToRepo((int) auctionId, newBid);
            } catch (Exception e) {
                System.err.println("[AuctionService] Lỗi khi lưu log Bid vào DB: " + e.getMessage());
            }

            boolean isUpdated = auctionRepository.updateCurrentPrice(auctionId, bidAmount);
            if (!isUpdated) {
                isUpdated = auctionRepository.saveOrUpdate(auction);
            }

            System.out.println("[AuctionService] Cập nhật giá mới thành công lên hệ thống (Chưa trừ tiền): đ " + bidAmount);
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
            System.out.println("Chỉ có phiên đang CHẠY mới có thể kết thúc!");
            return;
        }

        auction.setStatus(AuctionStatus.FINISHED);
        auctionRepository.updateStatus(auction.getId(), AuctionStatus.FINISHED);
        System.out.println("[Server] Phiên đấu giá ID: " + auction.getId() + " đã kết thúc và chuyển trạng thái sang FINISHED.");

        List<Bid> bids = auctionRepository.getBidsByAuctionId(auction.getId().intValue());
        if (bids != null && !bids.isEmpty()) {
            Bid winnerBid = bids.get(bids.size() - 1);
            Bidder bidder = winnerBid.getBidder();
            double winningAmount = winnerBid.getAmount();

            // Trừ tiền của người thắng cuộc khi kết thúc phiên đấu giá
            double newBalance = bidder.getBalance() - winningAmount;
            userService.updateBalance(bidder.getId(), newBalance);

            // Cập nhật người thắng cuộc vào CSDL
            auctionRepository.updateWinner(auction.getId(), bidder.getId());
            auction.setWinnerId(bidder.getId());

            System.out.println("[Server] Tìm thấy người thắng cuộc: " + bidder.getName() + " (ID: " + bidder.getId() + "). Đã khấu trừ: đ " + winningAmount + ". Số dư còn lại: đ " + newBalance);
        } else {
            System.out.println("[Server] Phiên đấu giá kết thúc nhưng không có ai tham gia.");
        }
    }

    public synchronized boolean payAuction(Auction auction) {
        if (auction.getStatus() != AuctionStatus.FINISHED) {
            System.out.println("Phiên đấu giá phải ở trạng thái FINISHED để thanh toán!");
            return false;
        }

        auction.setStatus(AuctionStatus.PAID);
        System.out.println("Phiên đấu giá đã được đánh dấu là ĐÃ THANH TOÁN!");
        return true;
    }

    public synchronized boolean cancelAuction(int auctionId) {
        boolean success = auctionRepository.updateStatus(auctionId, AuctionStatus.CANCELED);
        if (success) {
            System.out.println("Phiên đấu giá đã bị hủy!");
        }
        return true;
    }

    public synchronized boolean approveAuction(int auctionId) {
        Auction auction = auctionRepository.getAuctionById(auctionId);

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime endTime = null;

        if (auction != null && auction.getStartTime() != null && auction.getEndTime() != null) {
            long durationMinutes = java.time.Duration.between(auction.getStartTime(), auction.getEndTime()).toMinutes();

            if (durationMinutes <= 0) {
                durationMinutes = 4320;
            }

            endTime = now.plusMinutes(durationMinutes);
            System.out.println("[Server] Tính toán thời gian thành công cho phiên ID: " + auctionId
                    + " (Thời lượng: " + durationMinutes + " phút).");
        } else {
            endTime = now.plusMinutes(4320);
            System.out.println("[Server Cảnh báo] Không tìm thấy mốc thời gian gốc của phiên ID: " + auctionId
                    + ". Tự động thiết lập mặc định 3 ngày.");
        }

        boolean success = auctionRepository.updateStatus(auctionId, AuctionStatus.RUNNING);

        if (success && endTime != null) {
            auctionRepository.updateTimes(auctionId, now, endTime);
            System.out.println("[Server] Đã kích hoạt phiên đấu giá ID: " + auctionId
                    + " thành công công khai. Thời gian chạy: " + now + " -> " + endTime);
        }

        return success;
    }
}