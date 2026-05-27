package com.auction.client.service;

import com.auction.common.model.*;
import javafx.application.Platform;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.function.Consumer;
import java.io.File;
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
    private Consumer<Boolean> onProfileUpdateResult;
    private Consumer<Boolean> onPasswordChangeResult;
    public void setOnRegisterResult(Consumer<Boolean> callback) { this.onRegisterResult = callback; }
    public void setOnActiveAuctionsReceived(Consumer<List<Auction>> callback) { this.onActiveAuctionsReceived = callback; }
    public void setOnProfileUpdateResult(Consumer<Boolean> callback) { this.onProfileUpdateResult = callback; }
    public void setOnPasswordChangeResult(Consumer<Boolean> callback) { this.onPasswordChangeResult = callback; }

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

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed() && out != null;
    }
    private Consumer<List<Auction>> onPendingAuctionsReceived;
    private Consumer<Boolean> onApproveAuctionResult;

    public void setOnPendingAuctionsReceived(Consumer<List<Auction>> callback) {
        this.onPendingAuctionsReceived = callback;
    }
    public void setOnApproveAuctionResult(Consumer<Boolean> callback) {
        this.onApproveAuctionResult = callback;
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
                    }
                    else if ("PROFILE_UPDATE_SUCCESS".equals(command)) {
                        if (onProfileUpdateResult != null) Platform.runLater(() -> onProfileUpdateResult.accept(true));
                    }
                    else if ("PROFILE_UPDATE_FAIL".equals(command)) {
                        if (onProfileUpdateResult != null) Platform.runLater(() -> onProfileUpdateResult.accept(false));
                    }
                    else if ("PASSWORD_CHANGE_SUCCESS".equals(command)) {
                        if (onPasswordChangeResult != null) Platform.runLater(() -> onPasswordChangeResult.accept(true));
                    }
                    else if ("PASSWORD_CHANGE_FAIL".equals(command)) {
                        if (onPasswordChangeResult != null) Platform.runLater(() -> onPasswordChangeResult.accept(false));
                    }
                    else if ("CREATE_AUCTION_SUCCESS".equals(command)) {
                        if (onCreateAuctionResult != null)
                            Platform.runLater(() -> onCreateAuctionResult.accept(true));
                    }
                    else if ("CREATE_AUCTION_FAIL".equals(command)) {
                        if (onCreateAuctionResult != null)
                            Platform.runLater(() -> onCreateAuctionResult.accept(false));
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
                    else if ("PENDING_AUCTIONS".equals(command)) {
                        System.out.println("[Client] ===== NHẬN PENDING_AUCTIONS =====");
                        System.out.println("[Client] Raw response: " + message);

                        List<Auction> auctionList = new java.util.ArrayList<>();

                        if (parts.length > 1 && !parts[1].isEmpty()) {
                            String[] auctionsRaw = parts[1].split(";");
                            System.out.println("[Client] Số auction parse được: " + auctionsRaw.length);

                            for (String raw : auctionsRaw) {
                                System.out.println("[Client] Đang parse: " + raw);
                                String[] data = raw.split("\\|");

                                if (data.length >= 3) {
                                    String idStr = data[0];
                                    String itemName = data[1];
                                    String priceStr = data[2];

                                    System.out.println("[Client] ID=" + idStr + " Name=" + itemName + " Price=" + priceStr);

                                    // Xử lý ID null
                                    long auctionId = 0;
                                    if (!"null".equals(idStr) && !idStr.isEmpty()) {
                                        auctionId = Long.parseLong(idStr);
                                    }

                                    double currentPrice = Double.parseDouble(priceStr);

                                    Item item = new Electronics((int) auctionId, itemName);
                                    Auction auction = new Auction(item, currentPrice);
                                    auction.setId(auctionId);
                                    auction.setCurrentPrice(currentPrice);
                                    auction.setStatus(AuctionStatus.ONQUEUE);

                                    auctionList.add(auction);
                                }
                            }
                        }

                        System.out.println("[Client] Tổng auction parse được: " + auctionList.size());

                        if (onPendingAuctionsReceived != null) {
                            List<Auction> finalList = auctionList;
                            Platform.runLater(() -> {
                                System.out.println("[Client] Gọi callback với " + finalList.size() + " auction");
                                onPendingAuctionsReceived.accept(finalList);
                            });
                        } else {
                            System.out.println("[Client] *** CẢNH BÁO: onPendingAuctionsReceived = NULL ***");
                        }
                    }
                    else if ("APPROVE_AUCTION_SUCCESS".equals(command)) {
                        if (onApproveAuctionResult != null)
                            Platform.runLater(() -> onApproveAuctionResult.accept(true));
                    }
                    else if ("APPROVE_AUCTION_FAIL".equals(command)) {
                        if (onApproveAuctionResult != null)
                            Platform.runLater(() -> onApproveAuctionResult.accept(false));
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
        if (!isConnected()) {
            if (onLoginFail != null) {
                Platform.runLater(() -> onLoginFail.accept("Cannot connect to server. Start the server first."));
            }
            return;
        }

        try {
            out.writeUTF("LOGIN," + username + "," + password);
            out.flush();
        } catch (IOException e) {
            if (onLoginFail != null) {
                Platform.runLater(() -> onLoginFail.accept("Cannot send login request."));
            }
            e.printStackTrace();
        }
    }

    public void placeBid(long auctionId, int userId, double amount) {
        if (!isConnected()) {
            if (onBidResult != null) {
                Platform.runLater(() -> onBidResult.accept(false));
            }
            return;
        }

        try {
            out.writeUTF(String.format("BID,%d,%d,%.2f", auctionId, userId, amount));
            out.flush();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void register(String username, String password, String email, String role) {
        if (!isConnected()) {
            if (onRegisterResult != null) {
                Platform.runLater(() -> onRegisterResult.accept(false));
            }
            return;
        }

        try {
            out.writeUTF(String.format("REGISTER,%s,%s,%s,%s", username, password, email, role));
            out.flush();
        } catch (IOException e) {
            if (onRegisterResult != null) {
                Platform.runLater(() -> onRegisterResult.accept(false));
            }
            e.printStackTrace();
        }
    }

    public void updateProfile(int userId, String username, String email) {
        if (!isConnected()) {
            if (onProfileUpdateResult != null) {
                Platform.runLater(() -> onProfileUpdateResult.accept(false));
            }
            return;
        }

        try {
            out.writeUTF(String.format("UPDATE_PROFILE,%d,%s,%s", userId, username, email));
            out.flush();
        } catch (IOException e) {
            if (onProfileUpdateResult != null) {
                Platform.runLater(() -> onProfileUpdateResult.accept(false));
            }
            e.printStackTrace();
        }
    }

    public void changePassword(int userId, String currentPassword, String newPassword) {
        if (!isConnected()) {
            if (onPasswordChangeResult != null) {
                Platform.runLater(() -> onPasswordChangeResult.accept(false));
            }
            return;
        }

        try {
            out.writeUTF(String.format("CHANGE_PASSWORD,%d,%s,%s", userId, currentPassword, newPassword));
            out.flush();
        } catch (IOException e) {
            if (onPasswordChangeResult != null) {
                Platform.runLater(() -> onPasswordChangeResult.accept(false));
            }
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
            out.writeUTF("GET_ACTIVE_AUCTIONS");
            out.flush();
        } catch (IOException e) {
            System.err.println("[Lỗi] Không thể gửi yêu cầu lấy danh sách!");
            e.printStackTrace();
        }
    }
    // ========== THÊM MỚI: Callback cho tạo auction ==========
    private Consumer<Boolean> onCreateAuctionResult;

    public void setOnCreateAuctionResult(Consumer<Boolean> callback) {
        this.onCreateAuctionResult = callback;
    }

    /**
     * Gửi yêu cầu tạo phiên đấu giá mới lên Server.
     * @param itemName   Tên sản phẩm
     * @param startPrice Giá khởi điểm
     * @param sellerId   ID của Seller đang đăng nhập
     */
    public void createAuction(String itemName, double startPrice, int sellerId, File imageFile) {
        if (!isConnected()) {
            if (onCreateAuctionResult != null) {
                Platform.runLater(() -> onCreateAuctionResult.accept(false));
            }
            return;
        }

        try {
            // Bước 1: Gửi lệnh tạo auction
            String request = String.format("CREATE_AUCTION,%s,%.2f,%d", itemName, startPrice, sellerId);
            out.writeUTF(request);
            out.flush();
            System.out.println("[Client] Đã gửi lệnh tạo auction");

            // Bước 2: Gửi ảnh (nếu có)
            if (imageFile != null && imageFile.exists()) {
                byte[] imageBytes = java.nio.file.Files.readAllBytes(imageFile.toPath());

                // Gửi độ dài ảnh (số byte)
                out.writeInt(imageBytes.length);
                out.flush();

                // Gửi toàn bộ bytes của ảnh
                out.write(imageBytes);
                out.flush();

                System.out.println("[Client] Đã gửi ảnh: " + imageBytes.length + " bytes");
            } else {
                // Không có ảnh → gửi độ dài = 0
                out.writeInt(0);
                out.flush();
                System.out.println("[Client] Không có ảnh đính kèm");
            }

        } catch (IOException e) {
            if (onCreateAuctionResult != null) {
                Platform.runLater(() -> onCreateAuctionResult.accept(false));
            }
            e.printStackTrace();
        }
    }
    /**
     * Admin: Lấy danh sách auction chờ duyệt
     */
    public void requestPendingAuctions() {
        if (!isConnected()) {
            System.err.println("[Client] Không kết nối được đến Server!");
            return;
        }
        try {
            System.out.println("[Client] Admin yêu cầu danh sách chờ duyệt");
            out.writeUTF("GET_PENDING_AUCTIONS");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Admin: Duyệt 1 auction
     */
    public void approveAuction(long auctionId) {
        if (!isConnected()) return;
        try {
            out.writeUTF("APPROVE_AUCTION," + auctionId);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
