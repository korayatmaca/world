package com.task.world;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RadarControlTest {
    private RadarControl radarControl;

    @BeforeEach
    void setUp() {
        radarControl = new RadarControl();
    }

    @AfterEach
    void tearDown() {
        radarControl = null;
    }

    @Test
    void startControl() {
        assertDoesNotThrow(() -> radarControl.startControl());
    }

    @Test
    void stopControl() {
        radarControl.startControl();
        assertDoesNotThrow(() -> radarControl.stopControl());
    }
}