module auction_server {
    requires auction_common;

    requires java.sql;

    exports com.auction.network;
    exports com.auction.service;

    opens main.java.com.auction.observer;
}