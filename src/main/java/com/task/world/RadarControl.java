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

public class RadarControl {
    private KafkaConsumer<String, String> consumer;
    private KafkaProducer<String, String> producer;
    private volatile boolean isRunning;


    public RadarControl() {
        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", "localhost:9092");
        consumerProps.put("group.id", "radar");
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Arrays.asList("TargetPointPosition", "RadarTowerPosition"));

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
                    int radarX = 0;
                    int radarY = 0;
                    int targetX = 0;
                    int targetY = 0;

                    for (ConsumerRecord<String, String> record : records) {
                        if (record.topic().equals("RadarTowerPosition")) {
                            String[] radarTowerPosition = record.value().split(",");
                            radarX = Integer.parseInt(radarTowerPosition[0]); // X coordinate of the radar tower
                            radarY = Integer.parseInt(radarTowerPosition[1]); // Y coordinate of the radar tower
                        } else if (record.topic().equals("TargetPointPosition")) {
                            String[] targetPointPosition = record.value().split(",");
                            targetX = Integer.parseInt(targetPointPosition[0]); // X coordinate of the target point
                            targetY = Integer.parseInt(targetPointPosition[1]); //Y coordinate of the target point
                        }
                    }
                    double targetBearing = bearing(radarX, radarY, targetX, targetY);
                    System.out.println("Target bearing: " + targetBearing);
                    producer.send(new ProducerRecord<>("TargetBearing", String.valueOf(targetBearing)));

                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("An error occurred while polling for records:");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    protected static double bearing(double lat1, double lon1, double lat2, double lon2){
        double longitude1 = lon1; // X coordinate of the radar tower
        double longitude2 = lon2; // X coordinate of the target point
        double latitude1 = Math.toRadians(lat1); // Y coordinate of the radar tower
        double latitude2 = Math.toRadians(lat2); // Y coordinate of the target point
        System.out.println("Radar: " + lon1 + "," + lat1);
        System.out.println("Target: " + lon2 + "," + lat2);

        double longDiff= Math.toRadians(longitude2-longitude1); //difference in longitude coordinates
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x))+360)%360; //convert from radians to degrees
    }

    public Future<?> stopControl() {
        isRunning = false;
        if (consumer != null) {
            consumer.close();
        }
        return null;
    }
    public static void main(String[] args) {
        new RadarControl().startControl();
    }
}