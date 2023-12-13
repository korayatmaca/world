package com.task.world;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class RadarControl {
    private KafkaConsumer<String, String> consumer;
    private KafkaProducer<String, String> producer;

    public RadarControl() {
        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", "localhost:9092");
        consumerProps.put("group.id", "radar");
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Arrays.asList("TargetPointPosition", "TowerPosition"));

        System.out.println("Subscribed to topics: " + consumer.subscription());

        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", "localhost:9092");
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(producerProps);
    }

    public void startControl() {
        while (true) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, String> record : records) {
                    String[] position = record.value().split(",");
                    int x = Integer.parseInt(position[0]);
                    int y = Integer.parseInt(position[1]);

                    // Calculate the angle and distance of the target from the radar
                    // This is a placeholder calculation, replace with actual calculation
                    String angleAndDistance = "angle:" + (x + y) + ",distance:" + Math.hypot(x, y);

                    producer.send(new ProducerRecord<>("TargetBearingPosition", angleAndDistance));
                }
            } catch (Exception e) {
                System.err.println("An error occurred while polling for records:");
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new RadarControl().startControl();
    }
}