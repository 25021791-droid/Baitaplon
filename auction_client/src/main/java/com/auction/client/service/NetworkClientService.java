package com.auction.client.service;

import com.auction.common.model.*;
import javafx.application.Platform;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

public class NetworkClientService {
    private static NetworkClientService instance;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    private Consumer<User> onLoginSuccess;
    private Consumer<String> onLoginFail;
    private Consumer<Boolean> onBidResult;
    private Consumer<String> onNewBidBroadcast;
    private Consumer<Boolean> onRegisterResult;
    private Consumer<List<Auction>> onActiveAuctionsReceived;
    public void setOnRegisterResult(Consumer<Boolean> callback) { this.onRegisterResult = callback; }
    public void setOnActiveAuctionsReceived(Consumer<List<Auction>> callback) { this.onActiveAuctionsReceived = callback; }

    private NetworkClientService() {
        Thread initThread = new Thread(() -> {
            try {
                System.out.println("[Client] Đang kết nối tới Server...");
                this.socket = new Socket("localhost", 8080);
                this.out = new DataOutputStream(socket.getOutputStream());
                this.in = new DataInputStream(socket.getInputStream());
                System.out.println("[Client] Kết nối thành công! Bắt đầu lắng nghe...");

                startListening();

            } catch (IOException e) {
                System.err.println("[Lỗi] Không thể kết nối tới Server (Hãy chắc chắn Server đã bật trước)!");
                e.printStackTrace();
            }
        });

        initThread.setDaemon(true); // Đảm bảo Thread này tự tắt khi tắt App
        initThread.start(); // Kích hoạt chạy ngầm ngay lập tức
    }

    public static NetworkClientService getInstance() {
        if (instance == null) instance = new NetworkClientService();
        return instance;
    }

    private void startListening() {
        Thread listenerThread = new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();
                    System.out.println("[Client nhận]: " + message);
                    String[] parts = message.split(",");
                    String command = parts[0];

                    if ("LOGIN_SUCCESS".equals(command)) {
                        int id = Integer.parseInt(parts[1]);
                        String name = parts[2];
                        String email = parts[3];
                        double balance = Double.parseDouble(parts[4]);
                        String role = parts[5];

                        User user = null;
                        if ("ADMIN".equals(role)) user = new Admin(id, name, email);
                        else if ("SELLER".equals(role)) user = new Seller(id, name, email);
                        else {
                            Bidder bidder = new Bidder(id, name, email);
                            bidder.setBalance(balance);
                            user = bidder;
                        }

                        if (onLoginSuccess != null) {
                            User finalUser = user;
                            Platform.runLater(() -> onLoginSuccess.accept(finalUser));
                        }
                    }
                    else if ("LOGIN_FAIL".equals(command)) {
                        if (onLoginFail != null) Platform.runLater(() -> onLoginFail.accept("Đăng nhập thất bại!"));
                    }
                    else if ("BID_OK".equals(command)) {
                        if (onBidResult != null) Platform.runLater(() -> onBidResult.accept(true));
                    }
                    else if ("BID_FAIL".equals(command)) {
                        if (onBidResult != null) Platform.runLater(() -> onBidResult.accept(false));
                    }
                    else if ("NEW_BID".equals(command)) {
                        if (onNewBidBroadcast != null) Platform.runLater(() -> onNewBidBroadcast.accept(message));
                    }
                    else if ("REGISTER_SUCCESS".equals(command)) {
                        if (onRegisterResult != null) Platform.runLater(() -> onRegisterResult.accept(true));
                    }
                    else if ("REGISTER_FAIL".equals(command)) {
                        if (onRegisterResult != null) Platform.runLater(() -> onRegisterResult.accept(false));
                    }else if ("ACTIVE_AUCTIONS".equals(command)) {
                        List<Auction> auctionList = new java.util.ArrayList<>();

                        if (parts.length > 1 && !parts[1].isEmpty()) {
                            String[] auctionsRaw = parts[1].split(";");

                            for (String raw : auctionsRaw) {
                                String[] data = raw.split("\\|");

                                long auctionId = Long.parseLong(data[0]);
                                String itemName = data[1];
                                double currentPrice = Double.parseDouble(data[2]);

                                Item item = new Electronics((int) auctionId, itemName);
                                Auction auction = new Auction(item, currentPrice);
                                auction.setId(auctionId);
                                auction.setCurrentPrice(currentPrice);

                                auctionList.add(auction);
                            }
                        }

                        if (onActiveAuctionsReceived != null) {
                            Platform.runLater(() -> onActiveAuctionsReceived.accept(auctionList));
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Mất kết nối với Server.");
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void setOnLoginSuccess(Consumer<User> callback) { this.onLoginSuccess = callback; }
    public void setOnLoginFail(Consumer<String> callback) { this.onLoginFail = callback; }
    public void setOnBidResult(Consumer<Boolean> callback) { this.onBidResult = callback; }
    public void setOnNewBidBroadcast(Consumer<String> callback) { this.onNewBidBroadcast = callback; }

    public void login(String username, String password) {
        try {
            out.writeUTF("LOGIN," + username + "," + password);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void placeBid(long auctionId, int userId, double amount) {
        try {
            out.writeUTF(String.format("BID,%d,%d,%.2f", auctionId, userId, amount));
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void register(String username, String password, String email, String role) {
        try {
            out.writeUTF(String.format("REGISTER,%s,%s,%s,%s", username, password, email, role));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestActiveAuctions() {
        try {
            System.out.println("[Client] Đang yêu cầu danh sách phiên đấu giá từ Server...");
            out.writeUTF("GET_ACTIVE_AUCTIONS");
        } catch (IOException e) {
            System.err.println("[Lỗi] Không thể gửi yêu cầu lấy danh sách!");
            e.printStackTrace();
        }
    }
}