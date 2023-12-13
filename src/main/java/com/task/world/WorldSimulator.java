package com.task.world;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Random;

public class WorldSimulator {
    private KafkaProducer<String, String> producer;
    private Random random;
    private volatile boolean running;
    private String cameraViewpoint;

    public WorldSimulator() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(props);
        random = new Random();
    }

    public void startSimulation() {
        running = true;
        new Thread(() -> {
            while (running) {
                String targetPosition = random.nextInt(10) + "," + random.nextInt(10);
                producer.send(new ProducerRecord<>("TargetPointPosition", targetPosition));

                String towerPosition = "5,5";
                producer.send(new ProducerRecord<>("TowerPosition", towerPosition));

                // Update the camera viewpoint
                cameraViewpoint = targetPosition;

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stopSimulation() {
        running = false;
    }

    public String getCameraViewpoint() {
        return cameraViewpoint;
    }

    public static void main(String[] args) {
        new WorldSimulator().startSimulation();
    }
}