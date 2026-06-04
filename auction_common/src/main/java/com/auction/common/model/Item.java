package com.auction.common.model;

public class Item extends Entity<Integer> {

    private String name;
    private String imagePath;

    public Item() {
        super();
    }

    public Item(Integer id, String name) {
        super(id);
        this.name = name;
    }

    public Item(Integer id, String name, String imagePath) {
        super(id);
        this.name = name;
        this.imagePath = imagePath;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}