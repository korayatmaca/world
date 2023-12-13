package com.task.world;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class CameraControl {
    private KafkaConsumer<String, String> consumer;
    private KafkaProducer<String, String> producer;

    public CameraControl() {
        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", "localhost:9092");
        consumerProps.put("group.id", "camera");
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singleton("TargetBearingPosition"));

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
                    String[] bearing = record.value().split(",");
                    double angle = Double.parseDouble(bearing[0].split(":")[1]);
                    double distance = Double.parseDouble(bearing[1].split(":")[1]);

                    // Calculate the angle of the target from the camera
                    // This is a placeholder calculation, replace with actual calculation
                    String cameraAngle = "angle:" + (angle + distance);

                    producer.send(new ProducerRecord<>("CameraLosStatus", cameraAngle));
                }
            } catch (Exception e) {
                System.err.println("An error occurred while polling for records:");
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new CameraControl().startControl();
    }
}