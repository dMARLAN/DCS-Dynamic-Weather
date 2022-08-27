package com.marlan.weatheroutput.utilities;

import lombok.Setter;

import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String INFO = "INFO";
    private static final String WARNING = "WARNING";
    private static final String ERROR = "ERROR";
    @Setter
    private static String dir;

    private Logger() {
    }

    @SuppressWarnings("unused")
    public static void info(String message) {
        log(INFO, message);
    }

    @SuppressWarnings("unused")
    public static void warning(String message) {
        log(WARNING, message);
    }

    @SuppressWarnings("unused")
    public static void error(String message) {
        log(ERROR, message);
    }

    private static void log(String type, String message) {
        getDateTime();
        FileHandler.appendFile(dir + "logs\\", "DCSDynamicWeather-Weather-Output.log", getDateTime() + " " + type + "    " + message + "\n");
    }

    private static String getDateTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dtf.format(java.time.LocalDateTime.now());
    }

}
