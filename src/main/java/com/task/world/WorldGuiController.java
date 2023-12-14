package com.task.world;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.concurrent.Task;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
public class WorldGuiController {

    @FXML
    private LineChart<Number, Number> chart;

    @FXML
    private Button playButton;

    @FXML
    private Button stopButton;

    private WorldSimulator worldSimulator;
    private RadarControl radarControl;
    private CameraControl cameraControl;
    private volatile boolean isRunning = true;

    private KafkaConsumer<String, String> consumer;

    private XYChart.Series<Number, Number> targetSeries;
    private XYChart.Series<Number, Number> radarSeries;
    private XYChart.Series<Number, Number> cameraSeries;

    @FXML
    public void initialize() {
        // Initialize WorldSimulator, RadarControl and CameraControl
        worldSimulator = new WorldSimulator();
        radarControl = new RadarControl();
        cameraControl = new CameraControl();

        // Initialize Kafka consumer
        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", "localhost:9092");
        consumerProps.put("group.id", "gui");
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Arrays.asList("TargetPointPosition", "RadarTowerPosition", "CameraTowerPosition"));

        // Initialize series for each position
        targetSeries = new XYChart.Series<>();
        targetSeries.setName("TargetPointPosition");
        radarSeries = new XYChart.Series<>();
        radarSeries.setName("RadarTowerPosition");
        cameraSeries = new XYChart.Series<>();
        cameraSeries.setName("CameraTowerPosition");

        // Add series to chart
        chart.getData().addAll(targetSeries, radarSeries, cameraSeries);

        // Start listening for updates from Kafka on a new thread
        new Thread(this::startListening).start();
    }

    private void startListening() {
        while (isRunning) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

            for (ConsumerRecord<String, String> record : records) {
                Platform.runLater(() -> updateChart(record.topic(), record.value()));
            }
        }
    }

    private void stopListening() {
        isRunning = false;
        consumer.close();
    }

    public void updateChart(String topic, String data) {
        String[] coordinates = data.split(",");
        int x = Integer.parseInt(coordinates[0]);
        int y = Integer.parseInt(coordinates[1]);

        XYChart.Data<Number, Number> point = new XYChart.Data<>(x, y);

        switch (topic) {
            case "TargetPointPosition":
                targetSeries.getData().clear(); // Clear old data
                targetSeries.getData().add(point); // Add new data
                break;
            case "RadarTowerPosition":
                radarSeries.getData().add(point);
                break;
            case "CameraTowerPosition":
                cameraSeries.getData().add(point);
                break;
        }
    }

    @FXML
    public void play() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Start WorldSimulator, RadarControl and CameraControl
                worldSimulator.startSimulation();
                radarControl.startControl();
                cameraControl.startControl();
                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    public void stop() {
        // Stop WorldSimulator, RadarControl and CameraControl
        Future<?> simFuture = worldSimulator.stopSimulation();
        Future<?> radarFuture = radarControl.stopControl();
        Future<?> cameraFuture = cameraControl.stopControl();

        // Wait for all tasks to complete
        try {
            simFuture.get();
            radarFuture.get();
            cameraFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Stop listening for updates from Kafka
        stopListening();
    }}