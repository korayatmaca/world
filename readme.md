# World Simulator Project

This project simulates a world with a moving target and two static towers, a Radar Tower and a Camera Tower. The positions of these entities are published to Kafka topics and consumed by different components of the system. The GUI displays the positions of these entities on a chart.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- Java 8 or higher
- Maven
- Kafka running on localhost:9092

### Installing

Clone the repository to your local machine:

```
git clone https://github.com/korayatmaca/world.git
```

Navigate to the project directory and build the project:

```
cd world
mvn clean install
```

### Running the Application

Run the `WorldGui` class to start the application:

```
mvn exec:java -Dexec.mainClass="com.task.world.WorldGui"
```

## Project Structure

The project consists of several classes:

- `WorldSimulator`: This class simulates the world by randomly generating a target position and static positions for the Radar and Camera Towers. These positions are published to Kafka topics.
- `WorldGuiController`: This class controls the GUI of the application. It consumes the positions from Kafka topics and updates the chart accordingly.
- `RadarControl`: This class controls the Radar Tower. It consumes the positions of the Radar Tower and the target from Kafka topics, calculates the bearing of the target from the Radar Tower, and publishes this bearing to a Kafka topic.
- `CameraControl`: This class controls the Camera Tower. It consumes the positions of the Camera Tower and the target from Kafka topics, calculates LoS (Line of Sight) between the Camera Tower and the target, and publishes this LoS to a Kafka topic.
- `WorldGui`: This class is the entry point of the application. It starts the JavaFX application.

## Built With

- [Java](https://www.java.com/) - The programming language used
- [Maven](https://maven.apache.org/) - Dependency Management
- [Kafka](https://kafka.apache.org/) - Used for inter-component communication
- [JavaFX](https://openjfx.io/) - Used to create the GUI

## Authors

- Koray Atmaca

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.