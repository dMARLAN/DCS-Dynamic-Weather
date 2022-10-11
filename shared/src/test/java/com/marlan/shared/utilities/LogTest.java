package com.marlan.shared.utilities;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class LogTest {
    @Test
    @DisplayName("Logger should only have one instance")
    void loggerShouldBeSingleton(){
        Log logger1 = Log.getInstance();
        Log logger2 = Log.getInstance();
        assertEquals(logger1, logger2);
    }
}