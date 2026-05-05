module com.auction.baitaplon {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.auction to javafx.fxml;
    opens com.auction.controller to javafx.fxml;

    exports com.auction;
    exports com.auction.controller;
}