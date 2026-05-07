package com.auction.service;

import java.sql.*;

public class DatabaseService {
    public static Connection getConnection() throws SQLException {

        String url = "jdbc:mysql://mysql-24ec3754-prject.b.aivencloud.com:23268/defaultdb?useSSL=true&trustServerCertificate=true";
        return DriverManager.getConnection(url, "avnadmin", "AVNS_MUnq7F2SF5Li4obd6GP");
    }
}