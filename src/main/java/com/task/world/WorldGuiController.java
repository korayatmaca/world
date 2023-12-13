package com.task.world;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
    public Button startButton;
    public Button stopButton;
    public Label cameraViewpointLabel;
    public Canvas worldCanvas;
    public XYChart<Number, Number> worldChart; // Declare worldChart
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
    }

    @FXML
    protected void startSimulation(ActionEvent event) {
        worldSimulator.startSimulation();
        new Thread(this::startListening).start();
    }

    private void startListening() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, String> record : records) {
                String cameraViewpoint = record.value();

                // Update the display on the JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    updateCameraViewpoint(cameraViewpoint);
                });
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
    }

    private void drawTarget(String cameraViewpoint) {
        GraphicsContext gc = worldCanvas.getGraphicsContext2D();

        // Clear the canvas
        gc.clearRect(0, 0, worldCanvas.getWidth(), worldCanvas.getHeight());

        // Parse the target position from the camera viewpoint
        String[] position = cameraViewpoint.split(",");
        double x = Double.parseDouble(position[0]);
        double y = Double.parseDouble(position[1]);

        // Draw the target
        gc.setFill(Color.RED);
        gc.fillOval(x, y, 10, 10);
    }

    public void drawRadar() {
        GraphicsContext gc = worldCanvas.getGraphicsContext2D();
        gc.setFill(Color.BLUE);
        gc.fillOval(10, 0, 10, 10);
    }

    public void drawCamera() {
        GraphicsContext gc = worldCanvas.getGraphicsContext2D();
        gc.setFill(Color.GREEN);
        gc.fillOval(1, 0, 10, 10);
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