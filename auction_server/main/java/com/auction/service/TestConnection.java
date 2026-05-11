package main.java.com.auction.service;

import java.sql.Connection;

// Test kết nối server
public class TestConnection {
    public static void main(String[] args) {

        try {
            System.out.println("-- Connecting to Aiven...");
            Connection conn = DatabaseService.getConnection();
            if (conn != null) {
                System.out.println("--> Connection successful.");
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("--> Connection Failed.");
            e.printStackTrace();
        }

    }
}
