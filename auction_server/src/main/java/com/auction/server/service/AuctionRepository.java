package com.auction.server.service;

import com.auction.common.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuctionRepository {

    
    public boolean addAuctionToRepo(Auction auction) {
        String sql = "INSERT INTO auctions (seller_id, item_id, starting_price, current_price, start_time, end_time, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, auction.getSellerId());
            stmt.setLong(2, auction.getItem().getId());
            stmt.setDouble(3, auction.getCurrentPrice());
            stmt.setDouble(4, auction.getCurrentPrice()); 
            stmt.setTimestamp(5, Timestamp.valueOf(auction.getStartTime()));
            stmt.setTimestamp(6, Timestamp.valueOf(auction.getEndTime()));
            stmt.setString(7, auction.getStatus().name());

            int affectedRows = stmt.executeUpdate();

            
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

    
    public Auction getAuctionById(long auctionId) {
        String sql = "SELECT a.*, i.name AS item_name, i.image_path AS item_image " +
                "FROM auctions a " +
                "INNER JOIN items i ON a.item_id = i.id " +
                "WHERE a.id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, auctionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAuction(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    
    public boolean updateCurrentPrice(long auctionId, double newPrice) {
        String sql = "UPDATE auctions SET current_price = ? WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newPrice);
            stmt.setLong(2, auctionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    
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

    
    private Auction mapResultSetToAuction(ResultSet rs) throws SQLException {

        int itemId = rs.getInt("item_id");
        String itemName = rs.getString("item_name");

        
        Item item = new Item(itemId, itemName) {};
        item.setImagePath(rs.getString("item_image"));

        Auction a = new Auction();
        a.setId(rs.getLong("id"));
        a.setSellerId(rs.getInt("seller_id"));
        a.setItem(item);

        
        a.setStartingPrice(rs.getDouble("starting_price"));
        a.setCurrentPrice(rs.getDouble("current_price"));
        a.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        a.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        a.setStatus(AuctionStatus.valueOf(rs.getString("status")));

        
        int winnerId = rs.getInt("winner_id");
        if (!rs.wasNull()) {
            a.setWinnerId(winnerId);
        }
        return a;
    }

    public boolean saveOrUpdate(Auction auction) {
        
        if (auction.getId() == null || auction.getId() == 0) {
            return addAuctionToRepo(auction);
        } else {
            
            String sql = "UPDATE auctions SET seller_id = ?, item_id = ?, starting_price = ?, " +
                    "current_price = ?, start_time = ?, end_time = ?, status = ? WHERE id = ?";

            try (Connection conn = DatabaseService.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, auction.getSellerId());
                stmt.setLong(2, auction.getItem().getId());
                stmt.setDouble(3, auction.getStartingPrice());
                stmt.setDouble(4, auction.getCurrentPrice()); 
                stmt.setTimestamp(5, Timestamp.valueOf(auction.getStartTime()));
                stmt.setTimestamp(6, Timestamp.valueOf(auction.getEndTime()));
                stmt.setString(7, auction.getStatus().name());
                stmt.setLong(8, auction.getId());

                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public List<Bid> getBidsByAuctionId(int auctionId) {
        return new ArrayList<>();
    }

    public boolean addBidToRepo(int auctionId, Bid bid) {
        return false;
    }
}