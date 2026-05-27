package com.auction.client.controller;

import com.auction.common.model.Auction;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BidderCardController {
    @FXML private ImageView imgItem;
    @FXML private Label lblCategory;
    @FXML private Label lblTitle;
    @FXML private Label lblPrice;
    @FXML private Label lblTime;

    public void setData(Auction auction) {
        lblTitle.setText(auction.getItem().getName());
        lblPrice.setText("đ " + String.format("%,.0f", auction.getCurrentPrice()));

        // Bạn có thể tùy biến thêm thuộc tính Category và Time trong class Auction của bạn
        lblCategory.setText(auction.getItem().getClass().getSimpleName().toUpperCase());
        lblTime.setText("14h 12m"); // Dữ liệu thời gian tính toán từ server

        try {
            Image image = new Image(getClass().getResourceAsStream("/images/default.png"));
            imgItem.setImage(image);
        } catch (Exception e) {
            // Xử lý nếu lỗi ảnh
        }
    }
}