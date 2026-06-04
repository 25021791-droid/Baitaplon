package com.auction.client.controller;

import com.auction.common.model.Auction;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.time.LocalDateTime;

public class BidderCardController {
    @FXML private ImageView imgItem;
    @FXML private Label lblCategory;
    @FXML private Label lblTitle;
    @FXML private Label lblPrice;
    @FXML private Label lblTime;

    private Timeline countdown;

    public void setData(Auction auction) {
        lblTitle.setText(auction.getItem().getName());
        lblPrice.setText("đ " + String.format("%,.0f", auction.getCurrentPrice()));
        lblCategory.setText(auction.getItem().getClass().getSimpleName().toUpperCase());

        if (countdown != null) {
            countdown.stop();
        }

        LocalDateTime endTime = auction.getEndTime();

        countdown = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            LocalDateTime now = LocalDateTime.now();

            // -- Tính khoảng cách thời gian giữa hiện tại và kết thúc
            java.time.Duration duration = java.time.Duration.between(now, endTime);

            if (duration.isNegative() || duration.isZero()) {
                lblTime.setText("Phiên đấu giá đã kết thúc!");
                lblTime.setStyle("-fx-text-fill: red;");
                countdown.stop();
            } else {
                long hours = duration.toHours();
                long minutes = duration.toMinutes() % 60;
                long seconds = duration.getSeconds() % 60;


                String timeString = "";
                if (hours > 0) {
                    timeString += hours + "h ";
                }
                timeString += String.format("%02dm %02ds", minutes, seconds);

                lblTime.setText(timeString);
            }
        }));

        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();

        try {
            Image image = new Image(getClass().getResourceAsStream("/images/default.png"));
            imgItem.setImage(image);
        } catch (Exception e) {
            // Xử lý nếu lỗi ảnh
        }
    }

    // -- Tắt timeline của card cũ
    public void shutdown() {
        if (countdown != null) {
            countdown.stop();
        }
    }
}