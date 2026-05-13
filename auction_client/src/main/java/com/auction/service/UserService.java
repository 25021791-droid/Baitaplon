package com.auction.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class UserService {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8080;

    public boolean register(String username, String password, String email) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            String request = "REGISTER," + username + "," + password + "," + email;
            out.writeUTF(request);

            String response = in.readUTF();

            return "SUCCESS".equals(response);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean login(String username, String password) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            String request = "LOGIN," + username + "," + password;
            out.writeUTF(request);

            String response = in.readUTF();

            return "SUCCESS".equals(response);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}