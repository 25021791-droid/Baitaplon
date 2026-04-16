package com.auction.model;

import java.util.List;
import java.util.ArrayList;

public abstract class Item {
    protected int id;
    protected String item;
    protected List<Bid> bidHistory;

    public Item(int id, String item) {
        this.id = id;
        this.item = item;
        this.bidHistory = new ArrayList<>();
    }
    public int getId() {
        return this.id;
    }
    public String getItem() {
        return this.item;
    }
    public void addBid(Bid newBid) {
        this.bidHistory.add(newBid);
    }
}
