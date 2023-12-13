package com.task.world;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class WorldGuiController {
    @FXML
    public Button startButton;
    @FXML
    public Button stopButton;
    @FXML
    public Label cameraViewpointLabel;
    @FXML
    public Canvas worldCanvas;
    @FXML
    public LineChart<Number, Number> worldChart;
    private WorldSimulator worldSimulator;
    private KafkaConsumer<String, String> consumer;

    public WorldGuiController() {
        worldSimulator = new WorldSimulator();

        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", "localhost:9092");
        consumerProps.put("group.id", "gui");
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singleton("CameraLosStatus"));

        System.out.println("Subscribed to topics: " + consumer.subscription());
    }

    @FXML
    protected void startSimulation(ActionEvent event) {
        worldSimulator.startSimulation();
        new Thread(this::startListening).start();
    }

    private void startListening() {
        while (true) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    String cameraViewpoint = record.value();

                    // Update the display on the JavaFX Application Thread
                    javafx.application.Platform.runLater(() -> {
                        updateCameraViewpoint(cameraViewpoint);
                    });
                }
            } catch (Exception e) {
                System.err.println("An error occurred while polling for records:");
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void stopSimulation(ActionEvent event) {
        worldSimulator.stopSimulation();
    }

    public void updateCameraViewpoint(String cameraViewpoint) {
        cameraViewpointLabel.setText(cameraViewpoint);
        drawTarget(cameraViewpoint);
        drawRadar();
        drawCamera();
        updateChart(cameraViewpoint);
    }

    private void drawTarget(String cameraViewpoint) {
        String[] position = cameraViewpoint.split(",");
        double x = Double.parseDouble(position[0]);
        double y = Double.parseDouble(position[1]);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>(x, y));

        worldChart.getData().add(series);
    }

    public void drawRadar() {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>(1, 0)); // Adjust the position of the radar as needed

        worldChart.getData().add(series);
    }

    public void drawCamera() {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>(0, 0)); // Adjust the position of the camera as needed

        worldChart.getData().add(series);
    }

    public void updateChart(String cameraViewpoint) {
        String[] position = cameraViewpoint.split(",");
        double x = Double.parseDouble(position[0]);
        double y = Double.parseDouble(position[1]);

        XYChart.Series<Number, Number> series;
        if (worldChart.getData().isEmpty()) {
            series = new XYChart.Series<>();
            worldChart.getData().add(series);
        } else {
            series = worldChart.getData().get(0);
        }

        series.getData().add(new XYChart.Data<>(x, y));
    }
}