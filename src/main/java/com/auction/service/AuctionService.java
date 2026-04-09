// createAuction()
// closeAuction()
// show winner or something
package com.auction.service;

import com.auction.model.*;
import java.util.*;

public class AuctionService {
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

}
