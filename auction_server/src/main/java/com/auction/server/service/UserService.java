package com.auction.server.service;

import com.auction.common.model.Admin;
import com.auction.common.model.Bidder;
import com.auction.common.model.Seller;
import com.auction.common.model.User;

import java.sql.*;

public class UserService {
    public static User login(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ?";
        System.out.println("[Server] Login start: " + username);

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("[DEBUG SERVER] -> Tìm thấy username: " + username);

                String storedHash = rs.getString("password");
                System.out.println("[DEBUG SERVER] -> Độ dài chuỗi mật khẩu trong DB: " + storedHash.length());
                System.out.println("[Server] Login password length: " + password.length());

                boolean passwordMatches = PasswordService.checkPassword(password, storedHash);
                System.out.println("[Server] Password match: " + passwordMatches);

                if (passwordMatches) {
                    System.out.println("[DEBUG SERVER] -> Mật khẩu TRÙNG KHỚP!");
                    String role = rs.getString("role");
                    int id = rs.getInt("id");
                    String name = rs.getString("username");
                    String email = rs.getString("email");
                    double balance = rs.getDouble("balance");

                    if ("ADMIN".equals(role)) {
                        return new Admin(id, name, email);
                    } else if ("BIDDER".equals(role)) {
                        return new Bidder(id, name, email);
                    } else {
                        return new Seller(id, name, email);
                    }
                } else {
                    System.out.println("[DEBUG SERVER] -> Mật khẩu BỊ SAI hoặc lỗi Hash!");
                }
            } else {
                System.out.println("[DEBUG SERVER] -> KHÔNG tìm thấy username này trong Database!");
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean register(String username, String rawPassword, String email, String role) {
        String sql = "INSERT INTO users (username, password, email, role, balance) VALUES (?, ?, ?, ?, ?)";

        System.out.println("[Server] Hashing password...");
        String hashedPass = PasswordService.hashPassword(rawPassword);
        System.out.println("[Server] Opening database connection...");

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            System.out.println("[Server] Inserting user...");
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPass);
            pstmt.setString(3, email);
            pstmt.setString(4, role);
            pstmt.setDouble(5, 0.0);

            boolean success = pstmt.executeUpdate() > 0;
            System.out.println("[Server] Insert finished.");
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
