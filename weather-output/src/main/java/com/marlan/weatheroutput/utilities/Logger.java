package com.marlan.weatheroutput.utilities;

import lombok.Setter;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String INFO = "INFO    ";
    private static final String WARNING = "WARNING ";
    private static final String ERROR = "ERROR   ";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    @Setter
    private static String dir;

    private Logger() {
    }

    public static void info(String message) {
        log(INFO, message);
    }

    public static void warning(String message) {
        log(WARNING, message);
    }

    public static void error(String message) {
        log(ERROR, message);
    }

    private static void log(String type, String message) {
        System.out.println(getDateTime() + " " + type + message);
        try {
            FileHandler.appendFile(dir + "logs\\", "DCSDynamicWeather-Output.log", getDateTime() + " " + type + message + "\n");
        } catch (IOException ioe) {
            System.out.println("Error writing to log file");
        }
    }

    private static String getDateTime() {
        return dtf.format(java.time.LocalDateTime.now());
    }

}
