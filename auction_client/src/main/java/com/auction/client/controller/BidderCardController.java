package com.auction.client.controller;

import com.auction.common.model.Auction;
import com.auction.common.model.AuctionStatus;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.time.Duration;
import java.time.LocalDateTime;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Locale;

public class BidderCardController {

    @FXML private ImageView imgItem;
    @FXML private Label lblCategory;
    @FXML private Label lblTitle;
    @FXML private Label lblPrice;
    @FXML private Label lblTime;

    private Timeline countdownTimeline;

    public void setData(Auction auction) {
        if (auction == null || auction.getItem() == null) {
            return;
        }

        lblTitle.setText(auction.getItem().getName());
        lblPrice.setText("đ " + String.format(Locale.forLanguageTag("vi-VN"), "%,.0f", auction.getCurrentPrice()));
        lblCategory.setText(auction.getItem().getClass().getSimpleName().toUpperCase());

        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        if (auction.getStatus() == AuctionStatus.FINISHED ||
                auction.getStatus() == AuctionStatus.PAID ||
                auction.getStatus() == AuctionStatus.CANCELED) {
            lblTime.setText("--:--");
            lblTime.setStyle("-fx-text-fill: red;");

        } else {
            Runnable updateCardTime = () -> {
                LocalDateTime now = LocalDateTime.now();

                Duration duration = Duration.between(now, auction.getEndTime());

                if (duration.isNegative() || duration.isZero()) {
                    lblTime.setText("Phiên đấu giá đã kết thúc!");
                    lblTime.setStyle("-fx-text-fill: red;");
                    countdownTimeline.stop();
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
            };

            updateCardTime.run();
            countdownTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), (ActionEvent event) -> {
                updateCardTime.run();
            }));

            countdownTimeline.setCycleCount(Timeline.INDEFINITE);
            countdownTimeline.play();
        }

        String base64Image = auction.getItem().getImagePath();
        if (base64Image != null && !base64Image.equals("NO_IMAGE") && !base64Image.isEmpty()) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                imgItem.setImage(image);
            } catch (Exception e) {
                System.err.println("Lỗi giải mã ảnh Base64 trên Card, chuyển sang dùng ảnh mặc định.");
                loadDefaultImage();
            }
        } else {
            loadDefaultImage();
        }
    }

    private void loadDefaultImage() {
        try {
            InputStream stream = getClass().getResourceAsStream("/images/default.png");
            if (stream != null) {
                Image image = new Image(stream);
                imgItem.setImage(image);
            } else {
                System.err.println("Cảnh báo: Không tìm thấy file ảnh tại /images/default.png");
            }
        } catch (Exception e) {
            System.err.println("Lỗi load ảnh mặc định: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
    }
}