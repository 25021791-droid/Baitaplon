package com.auction.common.model;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class Auction {

    private Long id;
    private Item item;
    private List<Bid> bids;
    private double startingPrice;
    private double currentPrice;
    private AuctionStatus status;
    private int sellerId;
    private int winnerId;

    // Mặc định khởi tạo thời gian khi tạo object
    private LocalDateTime startTime = LocalDateTime.now();
    private LocalDateTime endTime = LocalDateTime.now().plusHours(24);

    public Auction() {
    }

    public Auction(Item item, double startPrice) {
        this.item = item;
        this.startingPrice = startPrice;

        // 🔥 ĐÃ FIX: Khởi tạo giá hiện tại bằng giá khởi điểm.
        // Nếu không có dòng này, currentPrice sẽ bằng 0.0 (mặc định của kiểu double).
        this.currentPrice = startPrice;

        this.bids = new ArrayList<>();
        this.status = AuctionStatus.OPEN;
    }

    public boolean isEnded() {
        return this.status == AuctionStatus.FINISHED ||
                this.status == AuctionStatus.CANCELED ||
                this.status == AuctionStatus.PAID;
    }

    // ================= GETTERS & SETTERS =================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public List<Bid> getBids() {
        return bids;
    }

    public void setBids(List<Bid> bids) {
        this.bids = bids;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double price) {
        this.startingPrice = price;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double price) {
        this.currentPrice = price;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public int getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(int winnerId) {
        this.winnerId = winnerId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}