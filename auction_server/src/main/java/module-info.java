module auction_server {
    requires auction_common;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires protobuf.java;

    opens com.auction.server.observer;

    exports com.auction.server.network;
    exports com.auction.server.service;
}