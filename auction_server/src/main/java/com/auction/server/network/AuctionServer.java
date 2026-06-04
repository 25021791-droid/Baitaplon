package com.auction.server.network;

import com.auction.common.model.Auction;
import com.auction.server.service.AuctionService;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class AuctionServer {
    public void start(int port) {
        // Khởi chạy luồng chạy nền tự động quét phiên đấu giá hết hạn
        Thread scannerThread = new Thread(() -> {
            AuctionService service = new AuctionService();
            System.out.println("[Server SCANNED] Bắt đầu luồng kiểm tra thời gian phiên đấu giá...");
            while (true) {
                try {
                    Thread.sleep(5000); // kiểm tra mỗi 5 giây
                    List<Auction> activeAuctions = service.getActiveAuctions();
                    for (Auction a : activeAuctions) {
                        if (a.getEndTime() != null && java.time.LocalDateTime.now().isAfter(a.getEndTime())) {
                            System.out.println("[Server SCANNED] Phát hiện phiên đấu giá ID " + a.getId() + " (" + a.getItem().getName() + ") hết hạn.");
                            service.endAuction(a);
                        }
                    }
                } catch (InterruptedException ex) {
                    System.err.println("[Server SCANNED] Luồng quét kết thúc đấu giá bị dừng.");
                    break;
                } catch (Exception ex) {
                    System.err.println("[Server SCANNED] Lỗi tiến trình quét: " + ex.getMessage());
                }
            }
        });
        scannerThread.setDaemon(true);
        scannerThread.start();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server đang chạy tại cổng: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Có thiết bị mới kết nối!");

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}