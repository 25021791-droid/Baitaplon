package com.auction.server;

import java.sql.*;

public class DescribeBids {
    public static void main(String[] args) throws Exception {
        Connection c = DriverManager.getConnection(
            "jdbc:mysql://mysql-24ec3754-prject.b.aivencloud.com:23268/defaultdb?useSSL=true&trustServerCertificate=true",
            "avnadmin", "AVNS_5zRsqM5L8lWsvYIKcf5");
        ResultSet rs = c.createStatement().executeQuery("DESCRIBE bids");
        System.out.println("=== COLUMNS IN BIDS TABLE ===");
        while (rs.next()) {
            System.out.println("Column: " + rs.getString("Field") + " | Type: " + rs.getString("Type"));
        }
        rs.close();
        c.close();
    }
}
