package com.auction.server.service;

import com.auction.common.model.Admin;
import com.auction.common.model.Bidder;
import com.auction.common.model.Seller;
import com.auction.common.model.User;

import java.sql.*;

public class UserService {

    
    public User login(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ?";
        System.out.println("[Server] Bắt đầu đăng nhập: " + username);

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("[DEBUG SERVER] -> Tìm thấy username: " + username);

                String storedHash = rs.getString("password");
                System.out.println("[DEBUG SERVER] -> Độ dài chuỗi mật khẩu trong DB: " + storedHash.length());
                System.out.println("[Server] Độ dài mật khẩu đăng nhập: " + password.length());

                boolean passwordMatches = PasswordService.checkPassword(password, storedHash);
                System.out.println("[Server] Kết quả so khớp mật khẩu: " + passwordMatches);

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
            System.out.println("[Server] Tên đăng nhập đã tồn tại!");
            return false;
        }

        String sql = "INSERT INTO users (username, password, email, role, balance) VALUES (?, ?, ?, ?, ?)";

        System.out.println("[Server] Đang mã hóa mật khẩu...");
        String hashedPass = PasswordService.hashPassword(rawPassword);
        System.out.println("[Server] Đang mở kết nối cơ sở dữ liệu...");

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            System.out.println("[Server] Đang thêm người dùng...");
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPass);
            pstmt.setString(3, email);
            pstmt.setString(4, role);
            pstmt.setDouble(5, 100000000.0);

            boolean success = pstmt.executeUpdate() > 0;
            System.out.println("[Server] Thêm người dùng hoàn tất.");
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

    public boolean updateBalance(int userId, double newBalance) {
        String sql = "UPDATE users SET balance = ? WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserService] Lỗi cập nhật số dư cho user ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public java.util.List<String[]> getAllUsers() {
        java.util.List<String[]> users = new java.util.ArrayList<>();
        String sql = "SELECT id, username, email, role, balance FROM users";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String[] row = new String[5];
                row[0] = String.valueOf(rs.getInt("id"));
                row[1] = rs.getString("username");
                row[2] = rs.getString("email");
                row[3] = rs.getString("role");
                row[4] = String.format(java.util.Locale.US, "%.0f", rs.getDouble("balance"));
                users.add(row);
            }
        } catch (SQLException e) {
            System.err.println("[UserService] Lỗi khi lấy danh sách người dùng: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserService] Lỗi khi xóa người dùng ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}