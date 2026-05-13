package com.auction.network;

import com.auction.service.UserService;
import com.auction.model.User;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    private final UserService userService = new UserService();

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

            String request = in.readUTF();
            System.out.println("[Server] Nhận được yêu cầu: " + request);

            String[] parts = request.split(",");
            String command = parts[0];

            if ("REGISTER".equals(command)) {
                if (parts.length == 4) {
                    String username = parts[1];
                    String password = parts[2];
                    String email = parts[3];

                    boolean success = userService.register(username, password, email);

                    if (success) {
                        out.writeUTF("SUCCESS");
                    } else {
                        out.writeUTF("FAIL");
                    }
                } else {
                    out.writeUTF("FAIL");
                }
            }
            else if ("LOGIN".equals(command)) {
                if (parts.length == 3) {
                    String username = parts[1];
                    String password = parts[2];

                    User user = userService.login(username, password);

                    if (user != null) {
                        out.writeUTF("SUCCESS");
                        // Mẹo nhỏ: Sau này muốn làm xịn hơn, chỗ này bạn có thể gửi chuỗi:
                        // "SUCCESS," + user.getRole() + "," + user.getId() để Client biết ai đang đăng nhập
                    } else {
                        out.writeUTF("FAIL");
                    }
                } else {
                    out.writeUTF("FAIL");
                }
            }
            else {
                out.writeUTF("UNKNOWN_COMMAND");
            }

        } catch (Exception e) {
            System.out.println("[Server] Lỗi kết nối hoặc Client đã ngắt: " + e.getMessage());
        }
    }
}