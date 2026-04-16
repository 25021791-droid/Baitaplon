    package com.auction.model;

    import java.util.ArrayList;
    import java.util.List;

    public class Auction {
        private Item item;
        private List<Bid> bids;
        private double currentPrice;
        private AuctionStatus status;

        public Auction(Item item,double StartPrice) {
            this.item = item;
            this.currentPrice=StartPrice;
            this.bids = new ArrayList<>();
            this.status=AuctionStatus.OPEN;
        }
        public List<Bid> getBids() { return bids; }
        public double getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(double price) { this.currentPrice = price; }
        public AuctionStatus getStatus() { return status; }
        public void setStatus(AuctionStatus status) { this.status = status; }
    }