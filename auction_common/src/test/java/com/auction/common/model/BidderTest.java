package com.auction.common.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

public class BidderTest {

    private Bidder bidder;
    private Auction auction;
    private Item item;

    @BeforeEach
    public void setUp() {
        bidder = new Bidder(1, "test_bidder", "test@example.com", 1000.0);
        item = new Item(101, "Test Item") {};
        auction = new Auction(item, 500.0);
        auction.setBids(new ArrayList<>());
    }

    @Test
    public void testPlaceBidSuccess() {
        boolean result = bidder.placeBid(auction, 600.0);
        assertTrue(result);
        assertEquals(600.0, auction.getCurrentPrice());
        assertEquals(1, auction.getBids().size());
        assertEquals(600.0, auction.getBids().get(0).getAmount());
        assertEquals(1, bidder.getBalance() / 1000.0); // balance shouldn't be deducted immediately
    }

    @Test
    public void testPlaceBidFailureInsufficentBalance() {
        boolean result = bidder.placeBid(auction, 1200.0);
        assertFalse(result);
        assertEquals(500.0, auction.getCurrentPrice()); // unchanged
        assertEquals(0, auction.getBids().size());
    }

    @Test
    public void testPlaceBidFailureLowerThanCurrentPrice() {
        boolean result = bidder.placeBid(auction, 450.0);
        assertFalse(result);
        assertEquals(500.0, auction.getCurrentPrice());
        assertEquals(0, auction.getBids().size());
    }

    @Test
    public void testPlaceBidFailureEqualToCurrentPrice() {
        boolean result = bidder.placeBid(auction, 500.0);
        assertFalse(result);
        assertEquals(500.0, auction.getCurrentPrice());
        assertEquals(0, auction.getBids().size());
    }
}
