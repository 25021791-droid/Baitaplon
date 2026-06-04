package com.auction.client.observer;

import com.auction.common.model.Bid;
import com.auction.common.observer.BidObserver;
import javafx.application.Platform;
import javafx.scene.control.Label;

public class BidUIObserver implements BidObserver {

    private final Label label;

    public BidUIObserver(Label label) {
        this.label = label;
    }

    @Override
    public void update(String message) {
        Platform.runLater(() -> {
            label.setText(message);
        });
    }

    // 🔥 ĐÃ FIX: Bổ sung phương thức bị thiếu để hoàn thành "hợp đồng" với Interface
    @Override
    public void update(Bid newBid) {
        Platform.runLater(() -> {
            if (newBid != null && newBid.getBidder() != null) {
                // Bạn có thể tùy biến chuỗi hiển thị ở đây cho đẹp mắt
                String uiMessage = String.format("%s vừa cược: %,.0f VNĐ",
                        newBid.getBidder().getName(),
                        newBid.getAmount());
                label.setText(uiMessage);
            }
        });
    }
}