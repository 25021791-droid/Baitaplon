package com.auction.server.network;

import java.util.List;
import java.util.Locale;

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
                }
                else if ("BID".equals(command)) {
                    out.writeUTF("BID_OK");
                    out.flush();
                }
                else if ("REGISTER".equals(command)) {
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
                }
                else if ("GET_ACTIVE_AUCTIONS".equals(command)) {
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
}
