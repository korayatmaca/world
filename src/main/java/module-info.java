module com.task.world {
    requires javafx.controls;
    requires javafx.fxml;
    requires kafka.clients;


    opens com.task.world to javafx.fxml;
    exports com.task.world;
}