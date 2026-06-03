package com.auction.server.service;

import com.auction.common.model.Auction;
import com.auction.common.model.Bid;

import java.util.List;

public class BidService {
    private final AuctionRepository auctionRepo = new AuctionRepository();

    public Bid getWinner(Auction auction) {

        System.out.println("[Server] Đang tính toán người thắng cuộc cho cuộc đấu giá ID: " + auction.getId());
        List<Bid> bids = auction.getBids();
        if (bids == null || bids.isEmpty()) {
            return null;
        }

        Bid winner = bids.get(0);
        for (Bid bid : bids) {
            if (bid.getAmount() > winner.getAmount()) {
                winner = bid;
            }
        }
        return winner;
    }
}
