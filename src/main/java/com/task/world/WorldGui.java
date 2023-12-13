package com.task.world;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class WorldGui extends Application {
    private KafkaConsumer<String, String> consumer;
    private FXMLLoader loader; // Declare the loader variable

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
        loader = new FXMLLoader(getClass().getResource("/com/task/world/world_gui.fxml")); // Initialize the loader variable
        VBox vbox = loader.load();

        Scene scene = new Scene(vbox, 200, 100);

        primaryStage.setScene(scene);
        primaryStage.show();
        new Thread(this::startListening).start();
    }

    private void startListening() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, String> record : records) {
                String cameraViewpoint = record.value();

                // Update the display on the JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    WorldGuiController controller = loader.getController();
                    controller.updateCameraViewpoint(cameraViewpoint);
                });
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}