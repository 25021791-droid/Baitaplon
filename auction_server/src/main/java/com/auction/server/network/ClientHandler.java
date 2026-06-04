package com.auction.server.network;

import com.auction.common.model.Item;
import com.auction.common.model.AuctionStatus;
import java.util.List;
import java.util.Locale;
import java.io.File;
import com.auction.common.model.Auction;
import com.auction.common.model.Bid;
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

    
    private void sendMessage(DataOutputStream out, String message) throws IOException {
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }

    @Override
    public void run() {
        String clientIp = clientSocket.getRemoteSocketAddress().toString();
        System.out.println("[Server] Bắt đầu luồng xử lý kết nối cho client: " + clientIp);
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            boolean isRunning = true;
            while (isRunning) {
                int length = in.readInt();

                if (length > 20 * 1024 * 1024) {
                    System.err.println("[Server Cảnh báo] Client " + clientIp + " gửi gói tin quá lớn (" + length + " bytes). Đóng kết nối!");
                    break;
                }

                byte[] payload = new byte[length];
                in.readFully(payload);
                String request = new String(payload, StandardCharsets.UTF_8);

                System.out.println("[Server] Nhận yêu cầu từ client " + clientIp + ": " + request.substring(0, Math.min(request.length(), 100)) + (request.length() > 100 ? "..." : ""));

                String[] temporaryParts = request.split(",");
                String command = temporaryParts[0];

                if ("LOGIN".equals(command)) {
                    String[] parts = request.split(",");
                    String username = parts[1];
                    String password = parts[2];

                    System.out.println("[Server LOGIN] Xử lý đăng nhập cho: " + username);
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
                        System.out.println(String.format("[Server LOGIN] Đăng nhập THÀNH CÔNG: ID=%d, Tên=%s, Email=%s, Số dư=%.2f, Vai trò=%s", id, name, email, balance, role));
                        sendMessage(out, response);
                    } else {
                        System.out.println("[Server LOGIN] Đăng nhập THẤT BẠI cho user: " + username + " (Sai tên đăng nhập hoặc mật khẩu)");
                        sendMessage(out, "LOGIN_FAIL");
                    }

                } else if ("BID".equals(command)) {
                    String[] parts = request.split(",");
                    try {
                        long auctionId = Long.parseLong(parts[1]);
                        int bidderId = Integer.parseInt(parts[2]);
                        double bidAmount = Double.parseDouble(parts[3]);

                        System.out.println(String.format("[Server BID] Đặt giá: Auction ID=%d, Bidder ID=%d, Số tiền=%.2f", auctionId, bidderId, bidAmount));
                        boolean isBidSuccess = auctionService.placeBid(auctionId, bidderId, bidAmount);
                        if (isBidSuccess) {
                            User updatedUser = userService.getUserById(bidderId);
                            double newBalance = 0.0;
                            if (updatedUser instanceof com.auction.common.model.Bidder) {
                                newBalance = ((com.auction.common.model.Bidder) updatedUser).getBalance();
                            }
                            System.out.println(String.format("[Server BID] Đặt giá THÀNH CÔNG: Auction ID=%d, Bidder ID=%d, Số dư còn lại=%.2f", auctionId, bidderId, newBalance));
                            sendMessage(out, String.format(Locale.US, "BID_SUCCESS,%.2f", newBalance));
                        } else {
                            System.out.println(String.format("[Server BID] Đặt giá THẤT BẠI: Auction ID=%d, Bidder ID=%d, Số tiền=%.2f", auctionId, bidderId, bidAmount));
                            sendMessage(out, "BID_FAIL");
                        }
                    } catch (Exception e) {
                        System.err.println("[Server BID] Lỗi xử lý đặt giá: " + e.getMessage());
                        sendMessage(out, "BID_FAIL");
                    }

                } else if ("REGISTER".equals(command)) {
                    String[] parts = request.split(",");
                    String username = parts[1];
                    String password = parts[2];
                    String email = parts[3];
                    String role = parts[4];

                    System.out.println(String.format("[Server REGISTER] Yêu cầu đăng ký mới: Username=%s, Email=%s, Vai trò=%s", username, email, role));
                    boolean isSuccess = userService.register(username, password, email, role);
                    System.out.println("[Server REGISTER] Kết quả đăng ký cho " + username + ": " + (isSuccess ? "THÀNH CÔNG" : "THẤT BẠI"));
                    sendMessage(out, isSuccess ? "REGISTER_SUCCESS" : "REGISTER_FAIL");

                } else if ("UPDATE_PROFILE".equals(command)) {
                    String[] parts = request.split(",");
                    int userId = Integer.parseInt(parts[1]);
                    String username = parts[2];
                    String email = parts[3];

                    System.out.println(String.format("[Server UPDATE_PROFILE] Yêu cầu cập nhật thông tin: User ID=%d, Username=%s, Email=%s", userId, username, email));
                    boolean isSuccess = userService.updateProfile(userId, username, email);
                    System.out.println("[Server UPDATE_PROFILE] Kết quả cập nhật cho User ID=" + userId + ": " + (isSuccess ? "THÀNH CÔNG" : "THẤT BẠI"));
                    sendMessage(out, isSuccess ? "PROFILE_UPDATE_SUCCESS" : "PROFILE_UPDATE_FAIL");

                } else if ("CHANGE_PASSWORD".equals(command)) {
                    String[] parts = request.split(",");
                    int userId = Integer.parseInt(parts[1]);
                    String currentPassword = parts[2];
                    String newPassword = parts[3];

                    System.out.println("[Server CHANGE_PASSWORD] Yêu cầu đổi mật khẩu cho User ID: " + userId);
                    boolean isSuccess = userService.changePassword(userId, currentPassword, newPassword);
                    System.out.println("[Server CHANGE_PASSWORD] Kết quả đổi mật khẩu cho User ID=" + userId + ": " + (isSuccess ? "THÀNH CÔNG" : "THẤT BẠI"));
                    sendMessage(out, isSuccess ? "PASSWORD_CHANGE_SUCCESS" : "PASSWORD_CHANGE_FAIL");

                } else if ("GET_ACTIVE_AUCTIONS".equals(command)) {
                    System.out.println("[Server GET_ACTIVE_AUCTIONS] Lấy danh sách các phiên đang đấu giá hoạt động...");
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
                                    System.err.println("[Server GET_ACTIVE_AUCTIONS] Không thể chuyển đổi ảnh sang Base64 cho vật phẩm " + auction.getItem().getName() + ": " + e.getMessage());
                                }
                            }
                        }

                        // Load bids from database
                        List<Bid> bids = null;
                        Auction loadedAuction = auctionService.getAuctionById(auction.getId().intValue());
                        if (loadedAuction != null) {
                            bids = loadedAuction.getBids();
                        }
                        StringBuilder bidsSb = new StringBuilder();
                        if (bids != null) {
                            for (int j = 0; j < bids.size(); j++) {
                                Bid bid = bids.get(j);
                                bidsSb.append(bid.getUsername()).append("#")
                                      .append(bid.getAmount()).append("#")
                                      .append(bid.getTime().toString());
                                if (j < bids.size() - 1) {
                                    bidsSb.append("_");
                                }
                            }
                        }

                        String endTimeStr = (auction.getEndTime() != null) ? auction.getEndTime().toString() : "";

                        responseBuilder.append(auction.getId()).append("|")
                                .append(auction.getItem().getName()).append("|")
                                .append(String.format(Locale.US, "%.2f", auction.getCurrentPrice())).append("|")
                                .append(base64Img).append("|")
                                .append(endTimeStr).append("|")
                                .append(bidsSb.toString());

                        if (i < activeAuctions.size() - 1) {
                            responseBuilder.append(";");
                        }
                    }
                    System.out.println("[Server GET_ACTIVE_AUCTIONS] Gửi danh sách " + activeAuctions.size() + " phiên hoạt động.");
                    sendMessage(out, responseBuilder.toString());

                } else if ("GET_ENDED_AUCTIONS".equals(command)) {
                    System.out.println("[Server GET_ENDED_AUCTIONS] Lấy danh sách các phiên đấu giá đã kết thúc...");
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
                    System.out.println("[Server GET_ENDED_AUCTIONS] Gửi danh sách " + ended.size() + " phiên đã kết thúc.");
                    sendMessage(out, sb.toString());

                } else if ("CREATE_AUCTION".equals(command)) {
                    String[] parts = request.split(",", 5);

                    String itemName = parts[1];
                    double startPrice = Double.parseDouble(parts[2]);
                    int sellerId = Integer.parseInt(parts[3]);
                    String base64Image = parts[4];

                    System.out.println(String.format("[Server CREATE_AUCTION] Yêu cầu tạo phiên đấu giá: Tên=%s, Giá khởi điểm=%.2f, Seller ID=%d", itemName, startPrice, sellerId));
                    System.out.println("[Server CREATE_AUCTION] Giải mã dữ liệu ảnh...");

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
                    newAuction.setEndTime(LocalDateTime.now().plusSeconds(300));

                    ItemRepository itemRepo = new ItemRepository();
                    AuctionRepository auctionRepo = new AuctionRepository();

                    boolean isItemSaved = itemRepo.addItemToRepo(item);

                    if (isItemSaved) {
                        boolean isAuctionSaved = auctionRepo.addAuctionToRepo(newAuction);
                        System.out.println("[Server CREATE_AUCTION] Kết quả tạo phiên đấu giá: " + (isAuctionSaved ? "THÀNH CÔNG" : "THẤT BẠI"));
                        sendMessage(out, isAuctionSaved ? "CREATE_AUCTION_SUCCESS" : "CREATE_AUCTION_FAIL");
                    } else {
                        System.err.println("[Server CREATE_AUCTION] Lưu vật phẩm thất bại!");
                        sendMessage(out, "CREATE_AUCTION_FAIL");
                    }

                } else if ("GET_PENDING_AUCTIONS".equals(command)) {
                    System.out.println("[Server GET_PENDING_AUCTIONS] Lấy danh sách phiên chờ duyệt...");
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
                    System.out.println("[Server GET_PENDING_AUCTIONS] Gửi danh sách " + pending.size() + " phiên chờ duyệt.");
                    sendMessage(out, sb.toString());

                } else if ("GET_MY_AUCTIONS".equals(command)) {
                    String[] parts = request.split(",");
                    int sellerId = Integer.parseInt(parts[1]);
                    System.out.println("[Server GET_MY_AUCTIONS] Lấy danh sách phiên của Seller ID: " + sellerId);
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
                    System.out.println("[Server GET_MY_AUCTIONS] Gửi danh sách " + myAuctions.size() + " phiên của Seller ID: " + sellerId);
                    sendMessage(out, sb.toString());

                } else if ("APPROVE_AUCTION".equals(command)) {
                    String[] parts = request.split(",");
                    long auctionId = Long.parseLong(parts[1]);
                    System.out.println("[Server APPROVE_AUCTION] Duyệt phiên ID: " + auctionId);
                    boolean ok = auctionService.approveAuction((int) auctionId);
                    System.out.println("[Server APPROVE_AUCTION] Kết quả duyệt phiên ID=" + auctionId + ": " + (ok ? "THÀNH CÔNG" : "THẤT BẠI"));
                    sendMessage(out, ok ? "APPROVE_AUCTION_SUCCESS" : "APPROVE_AUCTION_FAIL");

                } else if ("CANCEL_AUCTION".equals(command)) {
                    String[] parts = request.split(",");
                    long auctionId = Long.parseLong(parts[1]);
                    System.out.println("[Server CANCEL_AUCTION] Hủy phiên ID: " + auctionId);
                    boolean ok = auctionService.cancelAuction((int) auctionId);
                    System.out.println("[Server CANCEL_AUCTION] Kết quả hủy phiên ID=" + auctionId + ": " + (ok ? "THÀNH CÔNG" : "THẤT BẠI"));
                    sendMessage(out, ok ? "CANCEL_AUCTION_SUCCESS" : "CANCEL_AUCTION_FAIL");

                } else if ("LOGOUT".equals(command)) {
                    System.out.println("[Server LOGOUT] Client " + clientIp + " yêu cầu đăng xuất.");
                    sendMessage(out, "GOODBYE");
                    isRunning = false;
                }
            }
            in.close();
            out.close();
            clientSocket.close();
            System.out.println("[Server] Đóng kết nối bình thường với client: " + clientIp);

        } catch (Exception e) {
            System.out.println("[Server] Kết nối bị ngắt đột ngột hoặc lỗi xảy ra với client: " + clientIp + ". Chi tiết: " + e.getMessage());
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