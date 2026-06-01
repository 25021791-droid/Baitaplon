package com.auction.server.service;

import com.auction.common.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuctionRepository {

    // -- Thêm một auction vào Database
    public boolean addAuctionToRepo(Auction auction) {
        String sql = "INSERT INTO auctions (seller_id, item_id, starting_price, current_price, start_time, end_time, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, auction.getSellerId());
            stmt.setLong(2, auction.getItem().getId());
            stmt.setDouble(3, auction.getCurrentPrice());
            stmt.setDouble(4, auction.getCurrentPrice()); // Ban đầu current_price = starting_price
            stmt.setTimestamp(5, Timestamp.valueOf(auction.getStartTime()));
            stmt.setTimestamp(6, Timestamp.valueOf(auction.getEndTime()));
            stmt.setString(7, auction.getStatus().name());

            int affectedRows = stmt.executeUpdate();

            // -- Lấy ID từ DB gán ngược lại cho object
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        auction.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // -- Lấy danh sách các auction theo người bán
    public List<Auction> getAuctionsBySellerId(int sellerId) {
        List<Auction> list = new ArrayList<>();

        String sql = "SELECT a.*, i.name AS item_name, i.image_path AS item_image " +
                "FROM auctions a " +
                "INNER JOIN items i ON a.item_id = i.id " +
                "WHERE a.seller_id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sellerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    Auction auction = mapResultSetToAuction(rs);
                    list.add(auction);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // -- Lấy danh sách các auction theo trạng thái
    public List<Auction> getAuctionsByStatus(AuctionStatus status) {
        List<Auction> list = new ArrayList<>();

        String sql = "SELECT a.*, i.name AS item_name, i.image_path AS item_image " +
                "FROM auctions a " +
                "INNER JOIN items i ON a.item_id = i.id " +
                "WHERE a.status = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    Auction auction = mapResultSetToAuction(rs);
                    list.add(auction);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // -- Cập nhật trạng thái (Dùng cho approve, start, end, cancel auction)
    public boolean updateStatus(long auctionId, AuctionStatus newStatus) {
        String sql = "UPDATE auctions SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());
            stmt.setLong(2, auctionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // -- Hàm helper ánh xạ từ SQL sang object Java
    private Auction mapResultSetToAuction(ResultSet rs) throws SQLException {

        int itemId = rs.getInt("item_id");
        String itemName = rs.getString("item_name");
        Item item = new Item(itemId, itemName);

        Auction a = new Auction();
        a.setId(rs.getLong("id"));
        a.setSellerId(rs.getInt("seller_id"));
        a.setItem(item);
        a.setStartingPrice(rs.getDouble("starting_price"));
        a.setCurrentPrice(rs.getDouble("current_price"));
        a.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        a.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        a.setStatus(AuctionStatus.valueOf(rs.getString("status")));
        // Đối với winner_id có thể null
        int winnerId = rs.getInt("winner_id");
        if (!rs.wasNull()) {
            a.setWinnerId(winnerId);
        }
        return a;
    }
}