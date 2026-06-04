package com.auction.server.service;

import com.auction.common.model.Auction;
import com.auction.common.model.Bid;
import com.auction.common.observer.BidObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BidService {

    private final List<BidObserver> observers = new ArrayList<>();

    // 🔥 ĐÃ THÊM: Sử dụng ReentrantLock để giải quyết bài toán đồng thời (Concurrency)
    // Đảm bảo tại một thời điểm, chỉ có 1 luồng (1 Client) được phép can thiệp vào giá của phiên đấu giá
    private final Lock bidLock = new ReentrantLock();

    public void addObserver(BidObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(String message) {
        for (BidObserver observer : observers) {
            observer.update(message);
        }
    }

    // Bổ sung nạp chồng phương thức cho BidLogger (Nếu bạn áp dụng Cách 2 ở bước trước)
    private void notifyObservers(Bid newBid) {
        for (BidObserver observer : observers) {
            // Ép kiểu hoặc gọi hàm hỗ trợ tùy thiết kế interface của bạn
            // observer.update(newBid);
        }
    }

    public boolean placeBid(Auction auction, Bid bid) {
        bidLock.lock();

        try {
            // 1. Kiểm tra trạng thái phiên đấu giá
            if (auction.isEnded()) {
                notifyObservers("Lỗi: Phiên đấu giá " + auction.getId() + " đã đóng!");
                return false;
            }

            // 2. So sánh giá (Logic nghiệp vụ cốt lõi)
            if (bid.getAmount() <= auction.getCurrentPrice()) {
                return false; // Giá không hợp lệ (thấp hơn hoặc bằng giá hiện tại)
            }

            // 3. Cập nhật dữ liệu vào phiên đấu giá
            auction.setCurrentPrice(bid.getAmount());
            auction.getBids().add(bid);

            // 4. Ghi Log Server
            notifyObservers(String.format("Phiên [%d] - Cập nhật giá mới: %,.0f VNĐ", auction.getId(), bid.getAmount()));

            return true;

        } catch (Exception e) {
            System.err.println("Lỗi xử lý cược: " + e.getMessage());
            return false;
        } finally {
            bidLock.unlock();
        }
    }

    public Bid getWinner(Auction auction) {
        if (auction == null || auction.getBids() == null || auction.getBids().isEmpty()) {
            return null; // Không có ai đặt giá
        }

        List<Bid> bids = auction.getBids();
        return bids.get(bids.size() - 1);
    }
}