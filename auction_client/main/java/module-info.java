module auction_client {
    requires auction_common;

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.auction.controller to javafx.fxml;

    exports com.auction;
}