package com.auction.client.controller;

import com.auction.common.model.Auction;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;

public class BidderCardController {

    @FXML private ImageView imgItem;
    @FXML private Label lblCategory;
    @FXML private Label lblTitle;
    @FXML private Label lblPrice;
    @FXML private Label lblTime;

    public void setData(Auction auction) {
        if (auction == null || auction.getItem() == null) {
            return;
        }

        lblTitle.setText(auction.getItem().getName());

        lblPrice.setText("đ " + String.format(Locale.forLanguageTag("vi-VN"), "%,.0f", auction.getCurrentPrice()));

        lblCategory.setText(auction.getItem().getClass().getSimpleName().toUpperCase());

        if (auction.getEndTime() != null) {
            lblTime.setText(calculateTimeRemaining(auction.getEndTime()));
        } else {
            lblTime.setText("Không xác định");
        }

        // --- ĐÃ CẬP NHẬT: GIẢI MÃ CHUỖI BASE64 ĐỂ HIỂN THỊ ẢNH TRÊN CARD ---
        String base64Image = auction.getItem().getImagePath();

        if (base64Image != null && !base64Image.equals("NO_IMAGE") && !base64Image.isEmpty()) {
            try {
                // Giải mã chuỗi ký tự Base64 nhận từ mạng LAN thành mảng bytes
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                // Tạo đối tượng Image trực tiếp từ luồng byte thô
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                imgItem.setImage(image);
            } catch (Exception e) {
                System.err.println("Lỗi giải mã ảnh Base64 trên Card, chuyển sang dùng ảnh mặc định.");
                loadDefaultImage();
            }
        } else {
            // Nếu không có ảnh đi kèm từ Server, dùng ảnh fallback mặc định
            loadDefaultImage();
        }
    }

    /**
     * Hàm phụ trợ nạp ảnh mặc định từ tài nguyên hệ thống
     */
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

    private String calculateTimeRemaining(LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(endTime)) {
            return "Đã kết thúc";
        }

        Duration duration = Duration.between(now, endTime);
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();

        if (days > 0) {
            return days + " ngày " + hours + " giờ";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else if (minutes > 0) {
            return minutes + " phút";
        } else {
            return "Sắp kết thúc";
        }
    }
}