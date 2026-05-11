package com.auction.network;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (InputStream input = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input));
             OutputStream output = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true)) {

            String request;
            while ((request = reader.readLine()) != null) {
                System.out.println("Client gửi yêu cầu: " + request);

                if (request.startsWith("LOGIN")) {
                    writer.println("LOGIN_SUCCESS");
                } else {
                    writer.println("UNKNOWN_COMMAND");
                }
            }
        } catch (IOException e) {
            System.out.println("Một Client đã ngắt kết nối.");
        }
    }
}