package com.task.world;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Future;

public class WorldSimulator {
    private KafkaProducer<String, String> producer;
    private Random random;
    private volatile boolean running;

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
                try {
                    // X0-20 and Y0-20 are the coordinates of the target point
                    String targetPosition = random.nextInt(20) + "," + random.nextInt(20);
                    System.out.println("Target position" +  targetPosition);
                    producer.send(new ProducerRecord<>("TargetPointPosition", targetPosition));

                    setTowerPosition();

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void setTowerPosition() {

        String RadarTowerPosition = "1,1";
        producer.send(new ProducerRecord<>("RadarTowerPosition", RadarTowerPosition));

        String CameraTowerPosition = "19,1";
        producer.send(new ProducerRecord<>("CameraTowerPosition", CameraTowerPosition));

        System.out.println("Radar Tower " +  RadarTowerPosition);
        System.out.println("Camera Tower " +  CameraTowerPosition);
        }

    public Future<?> stopSimulation() {

        running = false;
        return null;
    }

    public static void main(String[] args) {
        //new WorldSimulator().setTowerPosition();
        new WorldSimulator().startSimulation();
    }
}