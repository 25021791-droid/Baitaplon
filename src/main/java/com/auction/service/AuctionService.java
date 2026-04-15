package com.auction.service;

import com.auction.model.*;
import java.util.*;

public class AuctionService {
    public boolean startAuction(Auction auction) {
        if (auction.getStatus() != AuctionStatus.OPEN) {
            System.out.println("Auction must be OPEN to start!");
            return false;
        }

        auction.setStatus(AuctionStatus.RUNNING);
        System.out.println("Auction started!");
        return true;
    }
    private BidService bidService = new BidService();
    public void endAuction(Auction auction) {
        auction.setStatus(AuctionStatus.FINISHED);

        Bid winner = bidService.getWinner(auction);

        if (winner != null) {
            System.out.println("Winner: " + winner.getBidder().getName());

            Bidder b = winner.getBidder();
            b.setBalance(b.getBalance() - winner.getAmount());
        }
    }

    public boolean payAuction(Auction auction) {
        if (auction.getStatus() != AuctionStatus.FINISHED) {
            System.out.println("Auction must be FINISHED to pay!");
            return false;
        }

        auction.setStatus(AuctionStatus.PAID);
        System.out.println("Auction paid!");
        return true;
    }
    public boolean cancelAuction(Auction auction) {
        if (auction.getStatus() == AuctionStatus.FINISHED ||
                auction.getStatus() == AuctionStatus.PAID) {
            System.out.println("Cannot cancel finished/paid auction!");
            return false;
        }

        auction.setStatus(AuctionStatus.CANCELED);
        System.out.println("Auction canceled!");
        return true;
    }
}