package com.auction.server.network;

import com.auction.common.model.Item;
import com.auction.common.model.AuctionStatus;
import java.util.List;
import java.util.Locale;
import java.io.File;
import com.auction.common.model.Auction;
import com.auction.server.service.AuctionRepository;
import com.auction.server.service.AuctionService;
import com.auction.server.service.ItemRepository;
import com.auction.server.service.UserService;
import com.auction.common.model.User;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final UserService userService = new UserService();
    private final AuctionService auctionService = new AuctionService();

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    // 🔥 HÀM TIỆN ÍCH MỚI: Gửi dữ liệu bằng mảng byte (Thay thế cho out.writeUTF)
    private void sendMessage(DataOutputStream out, String message) throws IOException {
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            boolean isRunning = true;
            while (isRunning) {
                // ====================================================================
                // 🔥 CƠ CHẾ ĐỌC MỚI: Đọc mảng byte để bypass giới hạn 64KB của readUTF
                // ====================================================================
                int length = in.readInt();

                // Bảo vệ Server: Chặn các gói tin lớn hơn 20MB để chống tràn RAM (DDoS/OOM)
                if (length > 20 * 1024 * 1024) {
                    System.err.println("[Server Cảnh báo] Client gửi gói tin quá lớn (" + length + " bytes). Đóng kết nối!");
                    break;
                }

                byte[] payload = new byte[length];
                in.readFully(payload);
                String request = new String(payload, StandardCharsets.UTF_8);

                System.out.println("[Server] Nhận được yêu cầu: " + request.substring(0, Math.min(request.length(), 100)) + "...");

                // Tách sơ bộ để nhận diện tên Lệnh (Command)
                String[] temporaryParts = request.split(",");
                String command = temporaryParts[0];

                if ("LOGIN".equals(command)) {
                    String[] parts = request.split(",");
                    String username = parts[1];
                    String password = parts[2];

                    User user = userService.login(username, password);

                    if (user != null) {
                        int id = user.getId();
                        String name = user.getName();
                        String email = user.getEmail();
                        double balance = 0.0;
                        if (user instanceof com.auction.common.model.Bidder) {
                            balance = ((com.auction.common.model.Bidder) user).getBalance();
                        }
                        String role = "BIDDER";
                        if (user instanceof com.auction.common.model.Admin) role = "ADMIN";
                        else if (user instanceof com.auction.common.model.Seller) role = "SELLER";

                        String response = String.format(Locale.US, "LOGIN_SUCCESS,%d,%s,%s,%.2f,%s", id, name, email, balance, role);
                        sendMessage(out, response);
                    } else {
                        sendMessage(out, "LOGIN_FAIL");
                    }

                } else if ("BID".equals(command)) {
                    String[] parts = request.split(",");
                    try {
                        int auctionId = Integer.parseInt(parts[1]);
                        int bidderId = Integer.parseInt(parts[2]);
                        double bidAmount = Double.parseDouble(parts[3]);

                        boolean isBidSuccess = auctionService.placeBid(auctionId, bidderId, bidAmount);
                        sendMessage(out, isBidSuccess ? "BID_SUCCESS" : "BID_FAIL");
                    } catch (Exception e) {
                        System.err.println("[Server] Lỗi xử lý đặt giá: " + e.getMessage());
                        sendMessage(out, "BID_FAIL");
                    }

                } else if ("REGISTER".equals(command)) {
                    String[] parts = request.split(",");
                    String username = parts[1];
                    String password = parts[2];
                    String email = parts[3];
                    String role = parts[4];

                    boolean isSuccess = userService.register(username, password, email, role);
                    sendMessage(out, isSuccess ? "REGISTER_SUCCESS" : "REGISTER_FAIL");

                } else if ("UPDATE_PROFILE".equals(command)) {
                    String[] parts = request.split(",");
                    int userId = Integer.parseInt(parts[1]);
                    String username = parts[2];
                    String email = parts[3];

                    boolean isSuccess = userService.updateProfile(userId, username, email);
                    sendMessage(out, isSuccess ? "PROFILE_UPDATE_SUCCESS" : "PROFILE_UPDATE_FAIL");

                } else if ("CHANGE_PASSWORD".equals(command)) {
                    String[] parts = request.split(",");
                    int userId = Integer.parseInt(parts[1]);
                    String currentPassword = parts[2];
                    String newPassword = parts[3];

                    boolean isSuccess = userService.changePassword(userId, currentPassword, newPassword);
                    sendMessage(out, isSuccess ? "PASSWORD_CHANGE_SUCCESS" : "PASSWORD_CHANGE_FAIL");

                } else if ("GET_ACTIVE_AUCTIONS".equals(command)) {
                    List<Auction> activeAuctions = auctionService.getActiveAuctions();
                    StringBuilder responseBuilder = new StringBuilder("ACTIVE_AUCTIONS,");

                    for (int i = 0; i < activeAuctions.size(); i++) {
                        Auction auction = activeAuctions.get(i);

                        String base64Img = "NO_IMAGE";
                        String imgPath = auction.getItem().getImagePath();
                        if (imgPath != null && !imgPath.isEmpty()) {
                            File imgFile = new File(imgPath);
                            if (imgFile.exists()) {
                                try {
                                    byte[] fileContent = java.nio.file.Files.readAllBytes(imgFile.toPath());
                                    base64Img = java.util.Base64.getEncoder().encodeToString(fileContent);
                                } catch (Exception e) {
                                    System.err.println("[Server] Không thể chuyển đổi ảnh sang Base64: " + e.getMessage());
                                }
                            }
                        }

                        responseBuilder.append(auction.getId()).append("|")
                                .append(auction.getItem().getName()).append("|")
                                .append(String.format(Locale.US, "%.2f", auction.getCurrentPrice())).append("|")
                                .append(base64Img);

                        if (i < activeAuctions.size() - 1) {
                            responseBuilder.append(";");
                        }
                    }
                    sendMessage(out, responseBuilder.toString());

                } else if ("GET_ENDED_AUCTIONS".equals(command)) {
                    List<Auction> ended = auctionService.getEndedAuctions();
                    StringBuilder sb = new StringBuilder("ENDED_AUCTIONS,");
                    for (int i = 0; i < ended.size(); i++) {
                        Auction a = ended.get(i);
                        sb.append(a.getId()).append("|")
                                .append(a.getItem().getName()).append("|")
                                .append(String.format(Locale.US, "%.2f", a.getCurrentPrice())).append("|")
                                .append(a.getStatus().toString());
                        if (i < ended.size() - 1) sb.append(";");
                    }
                    sendMessage(out, sb.toString());

                } else if ("CREATE_AUCTION".equals(command)) {
                    String[] parts = request.split(",", 5);

                    String itemName = parts[1];
                    double startPrice = Double.parseDouble(parts[2]);
                    int sellerId = Integer.parseInt(parts[3]);
                    String base64Image = parts[4];

                    System.out.println("[Server] Đang tiến hành giải mã dữ liệu ảnh của vật phẩm: " + itemName);

                    String imagePath = null;
                    if (!"NO_IMAGE".equals(base64Image)) {
                        imagePath = saveImageToFile(base64Image, itemName);
                    }

                    Item item = new Item(0, itemName) {};
                    item.setImagePath(imagePath);

                    Auction newAuction = new Auction(item, startPrice);
                    newAuction.setStatus(AuctionStatus.ONQUEUE);
                    newAuction.setSellerId(sellerId);
                    newAuction.setStartTime(LocalDateTime.now());
                    newAuction.setEndTime(LocalDateTime.now().plusDays(7));

                    ItemRepository itemRepo = new ItemRepository();
                    AuctionRepository auctionRepo = new AuctionRepository();

                    boolean isItemSaved = itemRepo.addItemToRepo(item);

                    if (isItemSaved) {
                        boolean isAuctionSaved = auctionRepo.addAuctionToRepo(newAuction);
                        sendMessage(out, isAuctionSaved ? "CREATE_AUCTION_SUCCESS" : "CREATE_AUCTION_FAIL");
                    } else {
                        sendMessage(out, "CREATE_AUCTION_FAIL");
                    }

                } else if ("GET_PENDING_AUCTIONS".equals(command)) {
                    List<Auction> pending = auctionService.getPendingAuctions();
                    StringBuilder sb = new StringBuilder("PENDING_AUCTIONS,");

                    for (int i = 0; i < pending.size(); i++) {
                        Auction a = pending.get(i);
                        sb.append(a.getId()).append("|")
                                .append(a.getItem().getName()).append("|")
                                .append(String.format(Locale.US, "%.2f", a.getCurrentPrice())).append("|")
                                .append(a.getSellerId());
                        if (i < pending.size() - 1) sb.append(";");
                    }
                    sendMessage(out, sb.toString());

                } else if ("GET_MY_AUCTIONS".equals(command)) {
                    String[] parts = request.split(",");
                    int sellerId = Integer.parseInt(parts[1]);
                    List<Auction> myAuctions = auctionService.getAuctionsBySellerId(sellerId);

                    StringBuilder sb = new StringBuilder("MY_AUCTIONS,");
                    for (int i = 0; i < myAuctions.size(); i++) {
                        Auction a = myAuctions.get(i);
                        sb.append(a.getId()).append("|")
                                .append(a.getItem().getName()).append("|")
                                .append(String.format(Locale.US, "%.2f", a.getCurrentPrice())).append("|")
                                .append(a.getStatus().toString());
                        if (i < myAuctions.size() - 1) sb.append(";");
                    }
                    sendMessage(out, sb.toString());

                } else if ("APPROVE_AUCTION".equals(command)) {
                    String[] parts = request.split(",");
                    int auctionId = Integer.parseInt(parts[1]);
                    boolean ok = auctionService.approveAuction(auctionId);
                    sendMessage(out, ok ? "APPROVE_AUCTION_SUCCESS" : "APPROVE_AUCTION_FAIL");

                } else if ("CANCEL_AUCTION".equals(command)) {
                    String[] parts = request.split(",");
                    int auctionId = Integer.parseInt(parts[1]);
                    boolean ok = auctionService.cancelAuction(auctionId);
                    sendMessage(out, ok ? "CANCEL_AUCTION_SUCCESS" : "CANCEL_AUCTION_FAIL");

                } else if ("LOGOUT".equals(command)) {
                    sendMessage(out, "GOODBYE");
                    isRunning = false;
                }
            }
            in.close();
            out.close();
            clientSocket.close();

        } catch (Exception e) {
            System.out.println("[Server] Thao tác kết thúc luồng hoặc Client đã ngắt kết nối đột ngột.");
        }
    }

    private String saveImageToFile(String base64, String itemName) {
        try {
            File dir = new File("auction_images");
            if (!dir.exists()) dir.mkdir();

            String fileName = itemName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(dir, fileName);

            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64);
            java.nio.file.Files.write(imageFile.toPath(), imageBytes);

            return imageFile.getPath();
        } catch (Exception e) {
            System.err.println("[Server] Lỗi giải mã và lưu tập tin ảnh: " + e.getMessage());
            return null;
        }
    }
}