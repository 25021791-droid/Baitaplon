package com.auction.model;

import java.util.List;
import java.util.ArrayList;

public abstract class Item {
    protected int id;
    protected String name;
    protected List<Bid> bidHistory;

    public Item(int id, String name) {
        this.id = id;
        this.name = name;
        this.bidHistory = new ArrayList<>();
    }
    public int getId() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }
    public void addBid(Bid newBid) {
        this.bidHistory.add(newBid);
    }
}
