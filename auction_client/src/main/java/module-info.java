module auction_client {
    requires javafx.controls;
    requires javafx.fxml;
    requires auction_common;

    opens com.auction.client.controller to javafx.fxml;

    exports com.auction.client;
}