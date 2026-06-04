package com.auction.server.service;

import com.auction.common.model.Admin;
import com.auction.common.model.Bidder;
import com.auction.common.model.Seller;
import com.auction.common.model.User;

import java.sql.*;

public class UserService {

    // ĐÃ SỬA: Loại bỏ từ khóa static để đồng bộ với cơ chế khởi tạo đối tượng (new UserService) từ ClientHandler
    public User login(String username, String password) {
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
                        return new Bidder(id, name, email, balance);
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

    /**
     * 🔥 BỔ SUNG: Lấy thông tin User theo ID để kiểm tra ví và quyền hạn khi thực hiện Đặt giá (Bid)
     */
    public User getUserById(int userId) {
        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    int id = rs.getInt("id");
                    String name = rs.getString("username");
                    String email = rs.getString("email");
                    double balance = rs.getDouble("balance");

                    if ("ADMIN".equals(role)) {
                        return new Admin(id, name, email);
                    } else if ("BIDDER".equals(role)) {
                        return new Bidder(id, name, email, balance);
                    } else {
                        return new Seller(id, name, email);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[Server] Lỗi khi truy vấn thông tin User theo ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean register(String username, String rawPassword, String email, String role) {
        if (isUsernameExists(username)) {
            System.out.println("[Server] Username already exists!");
            return false;
        }

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
            pstmt.setDouble(5, 100000.0); // Số dư ví thử nghiệm mặc định

            boolean success = pstmt.executeUpdate() > 0;
            System.out.println("[Server] Insert finished.");
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProfile(int userId, String username, String email) {
        String sql = "UPDATE users SET username = ?, email = ? WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setInt(3, userId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        String selectSql = "SELECT password FROM users WHERE id = ?";
        String updateSql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

            selectStmt.setInt(1, userId);
            ResultSet rs = selectStmt.executeQuery();

            if (!rs.next()) {
                return false;
            }

            String storedHash = rs.getString("password");
            if (!PasswordService.checkPassword(currentPassword, storedHash)) {
                return false;
            }

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, PasswordService.hashPassword(newPassword));
                updateStmt.setInt(2, userId);
                return updateStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        int count = 0;

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()){
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return count > 0;
    }
}