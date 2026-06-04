package com.auction.client.controller;

import javafx.application.Platform;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class RealtimeChartController {

    private static final int MAX_DATA_POINTS = 20;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private LineChart<String, Number> lineChart;
    private XYChart.Series<String, Number> priceSeries;

    public LineChart<String, Number> createChart() {

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Thời gian");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Giá đấu (VNĐ)");

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Biểu đồ lịch sử giá Realtime");
        lineChart.setAnimated(false);

        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Giá cao nhất");
        lineChart.getData().add(priceSeries);

        return lineChart;
    }

    public void onNewBidReceived(double newPrice) {

        String currentTime = LocalTime.now().format(TIME_FORMATTER);

        Platform.runLater(() -> {
            priceSeries.getData().add(new XYChart.Data<>(currentTime, newPrice));

            if (priceSeries.getData().size() > MAX_DATA_POINTS) {
                priceSeries.getData().remove(0);
            }
        });
    }
}