package com.auction.server.service;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseService {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseService.class.getResourceAsStream("/application.properties")) {
            if (input == null) {
                throw new RuntimeException("CRITICAL ERROR: Không tìm thấy file /application.properties trong thư mục resources!");
            }
            properties.load(input);
            System.out.println("[Server] Đã tải cấu hình Database thành công.");

        } catch (Exception ex) {
            System.err.println("Lỗi khởi tạo cấu hình Database!");
            ex.printStackTrace();
            System.exit(1);
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