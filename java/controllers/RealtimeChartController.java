package controllers;

import javafx.application.Platform;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RealtimeChartController {

    private LineChart<String, Number> lineChart;
    private XYChart.Series<String, Number> priceSeries;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public LineChart<String, Number> createChart() {
        // Trục X: Thời gian (String)
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Thời gian");

        // Trục Y: Giá (Number)
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Giá đấu (VNĐ)");

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Biểu đồ lịch sử giá Realtime");
        lineChart.setAnimated(false); // Tắt animation mặc định để tránh giật lag khi update liên tục

        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Giá cao nhất");
        lineChart.getData().add(priceSeries);

        return lineChart;
    }

    // Phương thức này sẽ được gọi khi có Event/Socket báo có Bid mới
    public void onNewBidReceived(double newPrice) {
        String currentTime = timeFormat.format(new Date());

        // Đẩy việc update UI vào JavaFX Application Thread
        Platform.runLater(() -> {
            priceSeries.getData().add(new XYChart.Data<>(currentTime, newPrice));

            // Tùy chọn: Xóa bớt data cũ nếu biểu đồ quá dài (giữ lại 20 điểm gần nhất)
            if (priceSeries.getData().size() > 20) {
                priceSeries.getData().remove(0);
            }
        });
    }
}