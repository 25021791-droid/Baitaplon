package com.auction.server.service;

import com.auction.common.model.Item;
import java.sql.*;

public class ItemRepository {

    // -- Gán các thuộc tính từ object Java sang câu lệnh SQL để lưu vào bảng items
    public boolean addItemToRepo(Item item) {
        String sql = "INSERT INTO items (name, image_path) VALUES (?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getImagePath());

            int affectedRows = stmt.executeUpdate();

            // -- Lấy ID từ DB gán ngược lại cho object
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // -- Lấy item từ DB theo id
    public Item getItemFromRepo(int itemId) {
        String sql = "SELECT * FROM items WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, itemId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String imagePath = rs.getString("image_path");

                    Item item = new Item(itemId, name);
                    item.setImagePath(imagePath);

                    return item;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}