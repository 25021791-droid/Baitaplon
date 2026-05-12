package com.auction.service;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DatabaseService {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseService.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("-- không thấy [application.properties]");
            } else {
                properties.load(input);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        );
    }
}