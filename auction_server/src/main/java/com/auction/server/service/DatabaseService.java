package com.auction.server.service;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.lang.reflect.Proxy;

public class DatabaseService {
    private static final Properties properties = new Properties();
    private static final BlockingQueue<Connection> connectionPool = new LinkedBlockingQueue<>(15);
    private static final int POOL_SIZE = 8;

    static {
        try (InputStream input = DatabaseService.class.getResourceAsStream("/application.properties")) {
            if (input == null) {
                throw new RuntimeException("CRITICAL ERROR: Không tìm thấy file /application.properties trong thư mục resources!");
            }
            properties.load(input);
            System.out.println("[Server] Đã tải cấu hình Database thành công.");

            // Khởi tạo kết nối sẵn để tối ưu hóa thời gian phản hồi
            for (int i = 0; i < POOL_SIZE; i++) {
                try {
                    Connection conn = createNewConnection();
                    connectionPool.offer(conn);
                } catch (SQLException e) {
                    System.err.println("[Server Database] Lỗi khởi tạo kết nối CSDL ban đầu: " + e.getMessage());
                }
            }
            System.out.println("[Server Database] Đã tạo sẵn " + connectionPool.size() + " kết nối trong pool.");

        } catch (Exception ex) {
            System.err.println("Lỗi khởi tạo cấu hình Database!");
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        );
    }

    public static Connection getConnection() throws SQLException {
        Connection rawConn = connectionPool.poll();
        try {
            if (rawConn != null && !rawConn.isClosed() && rawConn.isValid(2)) {
                return createProxy(rawConn);
            }
        } catch (Exception e) {
            if (rawConn != null) {
                try { rawConn.close(); } catch (SQLException ignored) {}
            }
        }
        return createProxy(createNewConnection());
    }

    private static Connection createProxy(Connection rawConn) {
        return (Connection) Proxy.newProxyInstance(
            DatabaseService.class.getClassLoader(),
            new Class<?>[]{Connection.class},
            (proxy, method, args) -> {
                if ("close".equals(method.getName())) {
                    releaseConnection(rawConn);
                    return null;
                }
                try {
                    return method.invoke(rawConn, args);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        );
    }

    private static void releaseConnection(Connection conn) {
        if (conn == null) return;
        try {
            if (!conn.isClosed() && conn.isValid(2)) {
                if (!connectionPool.offer(conn)) {
                    conn.close(); // Pool đầy, đóng bớt kết nối
                }
            } else {
                conn.close();
            }
        } catch (SQLException e) {
            // Bỏ qua
        }
    }
}