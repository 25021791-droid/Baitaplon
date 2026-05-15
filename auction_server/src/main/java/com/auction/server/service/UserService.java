package com.auction.server.service;

import com.auction.common.model.Admin;
import com.auction.common.model.Bidder;
import com.auction.common.model.Seller;
import com.auction.common.model.User;

import java.sql.*;

public class UserService {
    public User login(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (PasswordService.checkPassword(password, storedHash)) {

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
                }
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean register(String username, String rawPassword, String email, String role) {
        String sql = "INSERT INTO users (username, password, email, role, balance) VALUES (?, ?, ?, ?, 10000.0)";

        String sql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, 'USER')";
        String hashedPass = PasswordService.hashPassword(rawPassword);

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPass);
            pstmt.setString(3, email);
            pstmt.setString(4, role); // Set giá trị cho role (BIDDER hoặc SELLER)

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}