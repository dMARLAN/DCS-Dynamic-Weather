package com.marlan.shared.utilities;

import org.junit.jupiter.api.*;

class LogTest {
    @Test
    @DisplayName("Logger should only have one instance")
    void loggerShouldBeSingleton(){
        Log logger1 = Log.getInstance();
        Log logger2 = Log.getInstance();
        Assertions.assertEquals(logger1, logger2);
    }
}