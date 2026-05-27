package com.auction.server.network;
import com.auction.common.model.Item;
import com.auction.common.model.Electronics;
import com.auction.common.model.AuctionStatus;
import java.util.List;
import java.util.Locale;
import java.io.File;
import com.auction.common.model.Auction;
import com.auction.server.service.AuctionService;
import com.auction.server.service.UserService;
import com.auction.common.model.User;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    private final UserService userService = new UserService();

    private final AuctionService auctionService = new AuctionService();

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            boolean isRunning = true;
            while (isRunning) {
                String request = in.readUTF();
                System.out.println("[Server] Nhận được: " + request);

                String[] parts = request.split(",");
                String command = parts[0];

                if ("LOGIN".equals(command)) {
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
                        out.writeUTF(response);
                        out.flush();
                    } else {
                        out.writeUTF("LOGIN_FAIL");
                        out.flush();
                    }
                } else if ("BID".equals(command)) {
                    out.writeUTF("BID_OK");
                    out.flush();
                } else if ("REGISTER".equals(command)) {
                    String username = parts[1];
                    String password = parts[2];
                    String email = parts[3];
                    String role = parts[4];

                    System.out.println("[Server] Register start: " + username + " / " + role);
                    boolean isSuccess = userService.register(username, password, email, role);
                    System.out.println("[Server] Register result: " + isSuccess);

                    if (isSuccess) {
                        out.writeUTF("REGISTER_SUCCESS");
                    } else {
                        out.writeUTF("REGISTER_FAIL");
                    }
                    out.flush();
                } else if ("UPDATE_PROFILE".equals(command)) {
                    int userId = Integer.parseInt(parts[1]);
                    String username = parts[2];
                    String email = parts[3];

                    boolean isSuccess = userService.updateProfile(userId, username, email);
                    out.writeUTF(isSuccess ? "PROFILE_UPDATE_SUCCESS" : "PROFILE_UPDATE_FAIL");
                    out.flush();
                } else if ("CHANGE_PASSWORD".equals(command)) {
                    int userId = Integer.parseInt(parts[1]);
                    String currentPassword = parts[2];
                    String newPassword = parts[3];

                    boolean isSuccess = userService.changePassword(userId, currentPassword, newPassword);
                    out.writeUTF(isSuccess ? "PASSWORD_CHANGE_SUCCESS" : "PASSWORD_CHANGE_FAIL");
                    out.flush();
                } else if ("GET_ACTIVE_AUCTIONS".equals(command)) {
                    List<Auction> activeAuctions = auctionService.getActiveAuctions();

                    StringBuilder responseBuilder = new StringBuilder("ACTIVE_AUCTIONS,");

                    for (int i = 0; i < activeAuctions.size(); i++) {
                        Auction auction = activeAuctions.get(i);

                        responseBuilder.append(auction.getId()).append("|")
                                .append(auction.getItem().getName()).append("|")
                                .append(String.format(Locale.US, "%.2f", auction.getCurrentPrice()));

                        if (i < activeAuctions.size() - 1) {
                            responseBuilder.append(";");
                        }
                    }
                    out.writeUTF(responseBuilder.toString());
                    out.flush();
                }
                else if ("GET_ENDED_AUCTIONS".equals(command)) {
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
                    out.writeUTF(sb.toString());
                    out.flush();
                }else if ("CREATE_AUCTION".equals(command)) {
                    String itemName = parts[1];
                    double startPrice = Double.parseDouble(parts[2]);
                    int sellerId = Integer.parseInt(parts[3]);

                    System.out.println("[Server] Nhận yêu cầu tạo auction: " + itemName);

                    // Bước 2: Đọc ảnh từ socket
                    String imagePath = null;
                    int imageSize = in.readInt();  // Đọc độ dài ảnh

                    if (imageSize > 0) {
                        byte[] imageBytes = new byte[imageSize];
                        in.readFully(imageBytes);  // Đọc toàn bộ bytes

                        // Lưu ảnh
                        imagePath = saveImageBytes(imageBytes, itemName);
                        System.out.println("[Server] Đã nhận ảnh: " + imageSize + " bytes");
                    }

                    // Tạo Item
                    Item item = new Electronics(0, itemName);
                    item.setImagePath(imagePath);

                    Auction newAuction = new Auction(item, startPrice);
                    newAuction.setStatus(AuctionStatus.ONQUEUE);
                    newAuction.setSellerId(sellerId);

                    auctionService.addAuction(newAuction);

                    out.writeUTF("CREATE_AUCTION_SUCCESS");
                    out.flush();
                    System.out.println("[Server] Đã tạo auction thành công, chờ duyệt!");
                } else if ("GET_PENDING_AUCTIONS".equals(command)) {
                    System.out.println("[Server] ===== DEBUG GET_PENDING_AUCTIONS =====");

                    List<Auction> pending = auctionService.getPendingAuctions();

                    System.out.println("[Server] Số lượng pending: " + pending.size());
                    for (Auction a : pending) {
                        System.out.println("[Server] Pending: ID=" + a.getId() + " Name=" + a.getItem().getName());
                    }

                    StringBuilder sb = new StringBuilder("PENDING_AUCTIONS,");
                    for (int i = 0; i < pending.size(); i++) {
                        Auction a = pending.get(i);
                        sb.append(a.getId()).append("|")
                                .append(a.getItem().getName()).append("|")
                                .append(String.format(Locale.US, "%.2f", a.getCurrentPrice())).append("|")
                                .append(a.getSellerId());
                        if (i < pending.size() - 1) sb.append(";");
                    }

                    System.out.println("[Server] Gửi response: " + sb.toString());
                    out.writeUTF(sb.toString());
                    out.flush();
                }else if ("GET_MY_AUCTIONS".equals(command)) {
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
                    out.writeUTF(sb.toString());
                    out.flush();
                }
                else if ("APPROVE_AUCTION".equals(command)) {
                    long auctionId = Long.parseLong(parts[1]);
                    System.out.println("[Server] Admin duyệt auction ID: " + auctionId);

                    boolean ok = auctionService.approveAuction(auctionId);
                    out.writeUTF(ok ? "APPROVE_AUCTION_SUCCESS" : "APPROVE_AUCTION_FAIL");  // ← SỬA
                    out.flush();
                    System.out.println("[Server] Kết quả duyệt: " + ok);
                }
                else if ("CANCEL_AUCTION".equals(command)) {   // ← PHẢI CÓ ELSE IF
                    long auctionId = Long.parseLong(parts[1]);
                    System.out.println("[Server] ===== NHẬN CANCEL_AUCTION: " + auctionId + " =====");
                    boolean ok = auctionService.cancelAuction(auctionId);
                    System.out.println("[Server] Kết quả hủy: " + ok);
                    out.writeUTF(ok ? "CANCEL_AUCTION_SUCCESS" : "CANCEL_AUCTION_FAIL");
                    out.flush();
                }
                else if ("LOGOUT".equals(command)) {
                    out.writeUTF("GOODBYE");
                    out.flush();
                    isRunning = false;
                }
            }
            in.close();
            out.close();
            clientSocket.close();

        } catch (Exception e) {
            System.out.println("[Server] Client connection ended or failed.");
            e.printStackTrace();
        }
    }
    private String saveImageToFile(String base64, String itemName) {
        try {
            // Tạo thư mục nếu chưa có
            File dir = new File("auction_images");
            if (!dir.exists()) dir.mkdir();

            // Tên file: itemName_timestamp.jpg
            String fileName = itemName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(dir, fileName);

            // Giải mã Base64 và ghi ra file
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64);
            java.nio.file.Files.write(imageFile.toPath(), imageBytes);

            return imageFile.getPath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private String saveImageBytes(byte[] imageBytes, String itemName) {
        try {
            java.io.File dir = new java.io.File("auction_images");
            if (!dir.exists()) dir.mkdir();

            String safeName = itemName.replaceAll("[^a-zA-Z0-9]", "_");
            String fileName = safeName + "_" + System.currentTimeMillis() + ".jpg";
            java.io.File imageFile = new java.io.File(dir, fileName);

            java.nio.file.Files.write(imageFile.toPath(), imageBytes);

            System.out.println("[Server] Đã lưu ảnh: " + imageFile.getPath());
            return imageFile.getPath();

        } catch (Exception e) {
            System.err.println("[Server] Lỗi lưu ảnh: " + e.getMessage());
            return null;
        }
    }

}
