// bidding rules
// check if auction is active
// check if bid is higher than current highest
package com.auction.service;

import com.auction.model.*;
import java.util.*;

public class BidService {

    public boolean placeBid(Auction auction, Bid bid) {

        if (auction.getStatus() != AuctionStatus.OPEN) {
            System.out.println("Auction not open!");
            return false;
        }

        if (bid.getAmount() <= auction.getCurrentPrice()) {
            System.out.println("Bid must be higher!");
            return false;
        }

        if (bid.getBidder().getBalance() < bid.getAmount()) {
            System.out.println("Not enough money!");
            return false;
        }

        auction.getBids().add(bid);
        auction.setCurrentPrice(bid.getAmount());
        return true;
    }


    public Bid getWinner(Auction auction) {
        List<Bid> bids = auction.getBids();
        if (bids.isEmpty()) return null;
        return bids.get(bids.size() - 1);
    }

    public void printBids(Auction auction) {
        for (Bid b : auction.getBids()) {
            System.out.println(
                    b.getBidder().getName() + " : " + b.getAmount()
            );
        }
    }
}
