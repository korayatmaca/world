package com.task.world;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Future;

public class CameraControl {
    private KafkaConsumer<String, String> consumer;
    private KafkaProducer<String, String> producer;
    private volatile boolean isRunning;
    public CameraControl() {
        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", "localhost:9092");
        consumerProps.put("group.id", "camera");
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Arrays.asList(/*"TargetBearing",*/ "CameraTowerPosition", "TargetPointPosition"));
        System.out.println("Subscribed to topics: " + consumer.subscription());


        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", "localhost:9092");
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(producerProps);
    }

    public void startControl() {
        isRunning = true;
        new Thread(() -> {
            while (isRunning) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    int cameraX = 0;
                    int cameraY = 0;
                    int targetX = 0;
                    int targetY = 0;

                    for (ConsumerRecord<String, String> record : records) {
                        if (record.topic().equals("TargetBearing")) {
                            double targetBearing = Double.parseDouble(record.value());
                            System.out.println("Target bearing: " + targetBearing);
                            producer.send(new ProducerRecord<>("TargetBearingPosition", String.valueOf(targetBearing)));
                        } else if (record.topic().equals("CameraTowerPosition")) {
                            String[] cameraTowerPosition = record.value().split(",");
                            cameraX = Integer.parseInt(cameraTowerPosition[0]); // X coordinate of the camera tower
                            cameraY = Integer.parseInt(cameraTowerPosition[1]); // Y coordinate of the camera tower
                            System.out.println("Camera position: " + cameraX + "," + cameraY);

                        } else if (record.topic().equals("TargetPointPosition")) {
                            String[] targetPointPosition = record.value().split(",");
                            targetX = Integer.parseInt(targetPointPosition[0]); // X coordinate of the target point
                            targetY = Integer.parseInt(targetPointPosition[1]); //Y coordinate of the target point
                            System.out.println("Target position: " + targetX + "," + targetY);
                        }
                    }
                    double los = calculateLOS(cameraX, cameraY, targetX, targetY);
                    System.out.println("LOS:  " + los);
                    producer.send(new ProducerRecord<>("CameraLosStatus", String.valueOf(los)));
                    Thread.sleep(1000);

                } catch (Exception e) {
                    System.err.println("An error occurred while polling for records:");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    protected static double calculateLOS(double lat1, double lon1, double lat2, double lon2){
        double longitude1 = lon1;
        double longitude2 = lon2;
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        System.out.println("Camera: " + lat1 + "," + lon1);
        System.out.println("Target: " + lat2 + "," + lon2);
        double longDiff= Math.toRadians(longitude2-longitude1);
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x))+360)%360;
    }

    public Future<?> stopControl() {
        isRunning = false;
        if (consumer != null) {
            consumer.close();
        }
        return null;
    }
    public static void main(String[] args) {
        new CameraControl().startControl();
    }
}