package com.task.world;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class WorldGuiController {
    public Button startButton;
    public Button stopButton;
    public Label cameraViewpointLabel;
    private WorldSimulator worldSimulator;

    public WorldGuiController() {
        worldSimulator = new WorldSimulator();
    }

    @FXML
    protected void startSimulation(ActionEvent event) {
        worldSimulator.startSimulation();
    }

    @FXML
    protected void stopSimulation(ActionEvent event) {
        worldSimulator.stopSimulation();
    }

    public void updateCameraViewpoint(String cameraViewpoint) {
        cameraViewpointLabel.setText(cameraViewpoint);
    }
}