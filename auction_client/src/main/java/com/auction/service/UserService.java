package com.auction.service;

import com.auction.model.User;
import com.auction.model.Bidder;
import com.auction.model.Seller;
import com.auction.model.Admin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class UserService {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8080;

    public boolean register(String username, String password, String email, String role) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            String request = "REGISTER," + username + "," + password + "," + email + "," + role;
            out.writeUTF(request);

            String response = in.readUTF();

            return "SUCCESS".equals(response);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public User login(String username, String password) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            String request = "LOGIN," + username + "," + password;
            out.writeUTF(request);

            String response = in.readUTF();
            String[] parts = response.split(",");

            if ("SUCCESS".equals(parts[0]) && parts.length >= 6) {
                int id = Integer.parseInt(parts[1]);
                String name = parts[2];
                String email = parts[3];
                double balance = Double.parseDouble(parts[4]);
                String role = parts[5];

                switch (role.toUpperCase()) {
                    case "ADMIN":
                        return new Admin(id, name, email);
                    case "SELLER":
                        return new Seller(id, name, email);
                    case "BIDDER":
                    default:
                        return new Bidder(id, name, email, balance);
                }
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}