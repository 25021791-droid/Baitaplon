package com.auction.model;

public abstract class Item {
    protected int itemId;
    protected String name;

    public Item(int itemId, String name) {
        this.itemId = itemId;
        this.name = name;
    }
}