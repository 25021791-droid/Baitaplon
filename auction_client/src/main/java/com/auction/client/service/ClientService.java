package com.auction.client.service;

import java.io.*;
import java.net.Socket;
import com.auction.common.model.User;
import com.auction.common.model.Bidder;
import com.auction.common.model.Seller;
import com.auction.common.model.Admin;

/*
 * class để gửi/nhận thông tin user với Server
 */
public class ClientService {
    private static ClientService instance;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    private ClientService() {
        try {
            this.socket = new Socket("localhost", 8080);
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ClientService getInstance() {
        if (instance == null) instance = new ClientService();
        return instance;
    }

    public User login(String username, String password) {

        try {
            String cmd = String.format("LOGIN|%s|%s", username, password);
            out.writeUTF(cmd);

            String response = in.readUTF(); // Đọc phản hồi từ Server

            if (response != null && response.startsWith("LOGIN_SUCCESS|")) {
                String[] parts = response.split("\\|");

                // Trích xuất dữ liệu (format: LOGIN_SUCCESS|ID|Name|Email|Balance|Role)
                int id = Integer.parseInt(parts[1]);
                String name = parts[2];
                String email = parts[3];
                double balance = Double.parseDouble(parts[4]);
                String role = parts[5];

                // Tạo User
                User user;
                if (role.equals("ADMIN")) {
                    user = new Admin(id, name, email);
                } else if (role.equals("SELLER")) {
                    user = new Seller(id, name, email);
                } else {
                    user = new Bidder(id, name, email);
                }

                return user;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean register(String username, String email, String password) {

        try {
            String cmd = String.format("REGISTER|%s|%s|%s", username, email, password);
            out.writeUTF(cmd);

            String response = in.readUTF();
            return "REGISTER_SUCCESS".equals(response);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}