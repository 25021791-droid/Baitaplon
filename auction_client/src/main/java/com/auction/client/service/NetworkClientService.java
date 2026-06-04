package com.auction.client.service;

import com.auction.common.model.*;
import com.auction.client.utils.UserSession;
import javafx.application.Platform;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.function.Consumer;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

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
    private Consumer<List<Auction>> onEndedAuctionsReceived;
    private Consumer<Boolean> onProfileUpdateResult;
    private Consumer<Boolean> onCancelAuctionResult;
    private Consumer<Boolean> onPasswordChangeResult;
    private Consumer<List<Auction>> onMyAuctionsReceived;
    private Consumer<List<Auction>> onAllAuctionsReceived;
    private Consumer<List<Auction>> onPendingAuctionsReceived;
    private Consumer<Boolean> onApproveAuctionResult;
    private Consumer<Boolean> onCreateAuctionResult;

    

    public void setOnEndedAuctionsReceived(Consumer<List<Auction>> c) { this.onEndedAuctionsReceived = c; }
    public void setOnCancelAuctionResult(Consumer<Boolean> c) { this.onCancelAuctionResult = c; }
    public void setOnRegisterResult(Consumer<Boolean> callback) { this.onRegisterResult = callback; }
    public void setOnActiveAuctionsReceived(Consumer<List<Auction>> callback) { this.onActiveAuctionsReceived = callback; }
    public void setOnProfileUpdateResult(Consumer<Boolean> callback) { this.onProfileUpdateResult = callback; }
    public void setOnPasswordChangeResult(Consumer<Boolean> callback) { this.onPasswordChangeResult = callback; }
    public void setOnMyAuctionsReceived(Consumer<List<Auction>> c) { this.onMyAuctionsReceived = c; }
    public void setOnAllAuctionsReceived(Consumer<List<Auction>> c) { this.onAllAuctionsReceived = c; }
    public void setOnPendingAuctionsReceived(Consumer<List<Auction>> callback) { this.onPendingAuctionsReceived = callback; }
    public void setOnApproveAuctionResult(Consumer<Boolean> callback) { this.onApproveAuctionResult = callback; }
    public void setOnLoginSuccess(Consumer<User> callback) { this.onLoginSuccess = callback; }
    public void setOnLoginFail(Consumer<String> callback) { this.onLoginFail = callback; }
    public void setOnBidResult(Consumer<Boolean> callback) { this.onBidResult = callback; }
    public void setOnNewBidBroadcast(Consumer<String> callback) { this.onNewBidBroadcast = callback; }
    public void setOnCreateAuctionResult(Consumer<Boolean> callback) { this.onCreateAuctionResult = callback; }

    

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

        initThread.setDaemon(true);
        initThread.start();
    }

    public static NetworkClientService getInstance() {
        if (instance == null) instance = new NetworkClientService();
        return instance;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed() && out != null;
    }

    private void sendMessage(String message) throws IOException {
        byte[] payload = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }

    private void startListening() {
        Thread listenerThread = new Thread(() -> {
            try {
                while (true) {
                    
                    int length = in.readInt();

                    if (length > 20 * 1024 * 1024) {
                        System.err.println("[Cảnh báo] Gói tin quá lớn (>20MB), bỏ qua để chống tràn RAM!");
                        continue;
                    }

                    byte[] payload = new byte[length];
                    in.readFully(payload);
                    String message = new String(payload, java.nio.charset.StandardCharsets.UTF_8);

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
                        else user = new Bidder(id, name, email, balance);

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
                    }
                    else if ("PROFILE_UPDATE_SUCCESS".equals(command)) {
                        if (onProfileUpdateResult != null) Platform.runLater(() -> onProfileUpdateResult.accept(true));
                    }
                    else if ("PROFILE_UPDATE_FAIL".equals(command)) {
                        if (onProfileUpdateResult != null) Platform.runLater(() -> onProfileUpdateResult.accept(false));
                    }
                    else if ("ENDED_AUCTIONS".equals(command)) {
                        List<Auction> list = new java.util.ArrayList<>();
                        if (parts.length > 1 && !parts[1].isEmpty()) {
                            for (String raw : parts[1].split(";")) {
                                String[] d = raw.split("\\|");
                                if (d.length >= 4) {
                                    Auction a = new Auction(new Item(0, d[1]), Double.parseDouble(d[2]));
                                    a.setId(Integer.parseInt(d[0]));
                                    a.setStatus(AuctionStatus.valueOf(d[3]));
                                    list.add(a);
                                }
                            }
                        }
                        if (onEndedAuctionsReceived != null) {
                            Platform.runLater(() -> onEndedAuctionsReceived.accept(list));
                        }
                    }
                    else if ("PASSWORD_CHANGE_SUCCESS".equals(command)) {
                        if (onPasswordChangeResult != null) Platform.runLater(() -> onPasswordChangeResult.accept(true));
                    }
                    else if ("PASSWORD_CHANGE_FAIL".equals(command)) {
                        if (onPasswordChangeResult != null) Platform.runLater(() -> onPasswordChangeResult.accept(false));
                    }
                    else if ("MY_AUCTIONS".equals(command)) {
                        List<Auction> list = new java.util.ArrayList<>();
                        if (parts.length > 1 && !parts[1].isEmpty()) {
                            for (String raw : parts[1].split(";")) {
                                String[] d = raw.split("\\|");
                                if (d.length >= 4) {
                                    Auction a = new Auction(new Item(0, d[1]), Double.parseDouble(d[2]));
                                    a.setId(Integer.parseInt(d[0]));
                                    a.setStatus(AuctionStatus.valueOf(d[3]));
                                    list.add(a);
                                }
                            }
                        }
                        if (onMyAuctionsReceived != null) {
                            Platform.runLater(() -> onMyAuctionsReceived.accept(list));
                        }
                    }
                    else if ("CREATE_AUCTION_SUCCESS".equals(command)) {
                        if (onCreateAuctionResult != null) Platform.runLater(() -> onCreateAuctionResult.accept(true));
                    }
                    else if ("CREATE_AUCTION_FAIL".equals(command)) {
                        if (onCreateAuctionResult != null) Platform.runLater(() -> onCreateAuctionResult.accept(false));
                    }
                    else if ("ACTIVE_AUCTIONS".equals(command)) {
                        List<Auction> auctionList = new java.util.ArrayList<>();

                        if (parts.length > 1 && !parts[1].isEmpty()) {
                            String[] auctionsRaw = parts[1].split(";");

                            for (String raw : auctionsRaw) {
                                String[] data = raw.split("\\|");

                                if (data.length >= 3) {
                                    int auctionId = Integer.parseInt(data[0]);
                                    String itemName = data[1];
                                    double currentPrice = Double.parseDouble(data[2]);
                                    String base64Image = (data.length >= 4) ? data[3] : "NO_IMAGE";
                                    String endTimeStr = (data.length >= 5) ? data[4] : "";
                                    String bidsRaw = (data.length >= 6) ? data[5] : "";

                                    Item item = new Item(auctionId, itemName) {};
                                    item.setImagePath(base64Image);

                                    Auction auction = new Auction(item, currentPrice);
                                    auction.setId(auctionId);
                                    auction.setCurrentPrice(currentPrice);

                                    if (endTimeStr != null && !endTimeStr.isEmpty()) {
                                        try {
                                            auction.setEndTime(java.time.LocalDateTime.parse(endTimeStr));
                                        } catch (Exception e) {
                                            System.err.println("[Client] Lỗi phân tích endTime của auction ID " + auctionId + ": " + e.getMessage());
                                        }
                                    }

                                    // Phân tích lịch sử đặt giá gửi từ Server
                                    List<Bid> bidList = new java.util.ArrayList<>();
                                    if (bidsRaw != null && !bidsRaw.isEmpty()) {
                                        String[] bidsSplit = bidsRaw.split("_");
                                        for (String bRaw : bidsSplit) {
                                            String[] bData = bRaw.split("#");
                                            if (bData.length >= 3) {
                                                String bUsername = bData[0];
                                                double bAmount = Double.parseDouble(bData[1]);
                                                java.time.LocalDateTime bTime = java.time.LocalDateTime.parse(bData[2]);
                                                Bidder dummyBidder = new Bidder(0, bUsername, "", 0.0);
                                                Bid bid = new Bid(dummyBidder, bAmount, bTime);
                                                bidList.add(bid);
                                            }
                                        }
                                    }
                                    auction.setBids(bidList);

                                    auctionList.add(auction);
                                }
                            }
                        }

                        if (onActiveAuctionsReceived != null) Platform.runLater(() -> onActiveAuctionsReceived.accept(auctionList));
                        if (onAllAuctionsReceived != null) Platform.runLater(() -> onAllAuctionsReceived.accept(auctionList));
                    }
                    else if ("CANCEL_AUCTION_SUCCESS".equals(command)) {
                        System.out.println("[Client] Hủy auction thành công");
                        if (onCancelAuctionResult != null) Platform.runLater(() -> onCancelAuctionResult.accept(true));
                    }
                    else if ("CANCEL_AUCTION_FAIL".equals(command)) {
                        System.out.println("[Client] Hủy auction thất bại");
                        if (onCancelAuctionResult != null) Platform.runLater(() -> onCancelAuctionResult.accept(false));
                    }
                    else if ("PENDING_AUCTIONS".equals(command)) {
                        System.out.println("[Client] ===== NHẬN PENDING_AUCTIONS =====");
                        List<Auction> auctionList = new java.util.ArrayList<>();

                        if (parts.length > 1 && !parts[1].isEmpty()) {
                            String[] auctionsRaw = parts[1].split(";");

                            for (String raw : auctionsRaw) {
                                String[] data = raw.split("\\|");

                                if (data.length >= 3) {
                                    String idStr = data[0];
                                    String itemName = data[1];
                                    String priceStr = data[2];

                                    int auctionId = 0;
                                    if (!"null".equals(idStr) && !idStr.isEmpty()) {
                                        auctionId = Integer.parseInt(idStr);
                                    }

                                    double currentPrice = Double.parseDouble(priceStr);

                                    Item item = new Item(auctionId, itemName);
                                    Auction auction = new Auction(item, currentPrice);
                                    auction.setId(auctionId);
                                    auction.setCurrentPrice(currentPrice);
                                    auction.setStatus(AuctionStatus.ONQUEUE);

                                    auctionList.add(auction);
                                }
                            }
                        }

                        if (onPendingAuctionsReceived != null) {
                            List<Auction> finalList = auctionList;
                            Platform.runLater(() -> onPendingAuctionsReceived.accept(finalList));
                        }
                    }
                    else if ("APPROVE_AUCTION_SUCCESS".equals(command)) {
                        if (onApproveAuctionResult != null) Platform.runLater(() -> onApproveAuctionResult.accept(true));
                    }
                    else if ("APPROVE_AUCTION_FAIL".equals(command)) {
                        if (onApproveAuctionResult != null) Platform.runLater(() -> onApproveAuctionResult.accept(false));
                    }
                }
            } catch (IOException e) {
                System.out.println("Mất kết nối với Server.");
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }



    public void requestMyAuctions(int sellerId) {
        if (!isConnected()) return;
        try {
            sendMessage("GET_MY_AUCTIONS," + sellerId);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void login(String username, String password) {
        if (!isConnected()) {
            if (onLoginFail != null) Platform.runLater(() -> onLoginFail.accept("Cannot connect to server. Start the server first."));
            return;
        }
        try {
            sendMessage("LOGIN," + username + "," + password);
        } catch (IOException e) {
            if (onLoginFail != null) Platform.runLater(() -> onLoginFail.accept("Cannot send login request."));
            e.printStackTrace();
        }
    }

    public void placeBid(long auctionId, int userId, double amount) {
        if (!isConnected()) {
            if (onBidResult != null) Platform.runLater(() -> onBidResult.accept(false));
            return;
        }
        try {
            sendMessage("BID," + auctionId + "," + userId + "," + amount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void register(String username, String password, String email, String role) {
        if (!isConnected()) {
            if (onRegisterResult != null) Platform.runLater(() -> onRegisterResult.accept(false));
            return;
        }
        try {
            sendMessage(String.format("REGISTER,%s,%s,%s,%s", username, password, email, role));
        } catch (IOException e) {
            if (onRegisterResult != null) Platform.runLater(() -> onRegisterResult.accept(false));
            e.printStackTrace();
        }
    }

    public void updateProfile(int userId, String username, String email) {
        if (!isConnected()) {
            if (onProfileUpdateResult != null) Platform.runLater(() -> onProfileUpdateResult.accept(false));
            return;
        }
        try {
            sendMessage(String.format("UPDATE_PROFILE,%d,%s,%s", userId, username, email));
        } catch (IOException e) {
            if (onProfileUpdateResult != null) Platform.runLater(() -> onProfileUpdateResult.accept(false));
            e.printStackTrace();
        }
    }

    public void changePassword(int userId, String currentPassword, String newPassword) {
        if (!isConnected()) {
            if (onPasswordChangeResult != null) Platform.runLater(() -> onPasswordChangeResult.accept(false));
            return;
        }
        try {
            sendMessage(String.format("CHANGE_PASSWORD,%d,%s,%s", userId, currentPassword, newPassword));
        } catch (IOException e) {
            if (onPasswordChangeResult != null) Platform.runLater(() -> onPasswordChangeResult.accept(false));
            e.printStackTrace();
        }
    }

    public void requestActiveAuctions() {
        if (!isConnected()) {
            System.err.println("[Error] Cannot request auctions because the client is not connected.");
            return;
        }
        try {
            System.out.println("[Client] Đang yêu cầu danh sách phiên đấu giá từ Server...");
            sendMessage("GET_ACTIVE_AUCTIONS");
        } catch (IOException e) {
            System.err.println("[Lỗi] Không thể gửi yêu cầu lấy danh sách!");
            e.printStackTrace();
        }
    }

    public void createAuction(String itemName, double startPrice, int sellerId, File imageFile) {
        if (!isConnected()) {
            if (onCreateAuctionResult != null) Platform.runLater(() -> onCreateAuctionResult.accept(false));
            return;
        }
        try {
            String base64Image = "NO_IMAGE";
            if (imageFile != null && imageFile.exists()) {
                byte[] fileContent = Files.readAllBytes(imageFile.toPath());
                base64Image = Base64.getEncoder().encodeToString(fileContent);
            }

            String request = "CREATE_AUCTION," + itemName + "," + startPrice + "," + sellerId + "," + base64Image;

            sendMessage(request);
            System.out.println("[Client] Đã gửi lệnh tạo auction kèm ảnh dạng Base64 an toàn.");

        } catch (IOException e) {
            if (onCreateAuctionResult != null) Platform.runLater(() -> onCreateAuctionResult.accept(false));
            e.printStackTrace();
        }
    }

    public void requestEndedAuctions() {
        if (!isConnected()) return;
        try {
            sendMessage("GET_ENDED_AUCTIONS");
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void requestPendingAuctions() {
        if (!isConnected()) {
            System.err.println("[Client] Không kết nối được đến Server!");
            return;
        }
        try {
            System.out.println("[Client] Admin yêu cầu danh sách chờ duyệt");
            sendMessage("GET_PENDING_AUCTIONS");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancelAuction(long auctionId) {
        if (!isConnected()) return;
        try {
            sendMessage("CANCEL_AUCTION," + auctionId);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void approveAuction(long auctionId) {
        if (!isConnected()) return;
        try {
            sendMessage("APPROVE_AUCTION," + auctionId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}