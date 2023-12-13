package com.task.world;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class WorldGui extends Application {
    private KafkaConsumer<String, String> consumer;
    private FXMLLoader loader;

    public WorldGui() {
        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", "localhost:9092");
        consumerProps.put("group.id", "gui");
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singleton("CameraLosStatus"));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        loader = new FXMLLoader(getClass().getResource("/com/task/world/world_gui.fxml"));
        BorderPane borderPane = loader.load();

        Scene scene = new Scene(borderPane, 200, 100);

        primaryStage.setScene(scene);

        primaryStage.setMinWidth(800); // Adjust the window size as needed
        primaryStage.setMinHeight(600); // Adjust the window size as needed
        primaryStage.setMaxWidth(800); // Adjust the window size as needed
        primaryStage.setMaxHeight(600); // Adjust the window size as needed
        primaryStage.setTitle("World GUI");

        primaryStage.show();

        // Get the controller and draw the initial positions of the radar and camera
        WorldGuiController controller = loader.getController();
        controller.drawRadar();
        controller.drawCamera();

        // Start listening for updates from Kafka on a new thread
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
                        WorldGuiController controller = loader.getController();
                        controller.updateCameraViewpoint(cameraViewpoint);
                    });
                }
            } catch (Exception e) {
                System.err.println("An error occurred while listening for updates from Kafka:");
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}