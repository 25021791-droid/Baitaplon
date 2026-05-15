package com.auction.client.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
 * A class for sending messages from Client to Server(Socket)
 */
public class ClientService {
    private static ClientService instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private ClientService() {
        try {
            this.socket = new Socket("localhost", 8080);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ClientService getInstance() {
        if (instance == null) instance = new ClientService();
        return instance;
    }

    public boolean login(String username, String password) {
        String message = "LOGIN|" + username + "|" + password;
        out.println(message);

        try {
            String response = in.readLine();
            return "LOGIN_SUCCESS".equals(response);
        } catch (IOException e) {
            return false;
        }
    }

    public boolean register(String username, String email, String password) {
        String cmd = String.format("REGISTER|%s|%s|%s", username, email, password);
        out.println(cmd);

        try {
            String response = in.readLine();
            return "REGISTER_SUCCESS".equals(response);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}