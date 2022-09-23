package com.marlan.weatherupdate.utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String INFO = "INFO    ";
    private static final String WARNING = "WARNING ";
    private static final String ERROR = "ERROR   ";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static FileWriter loggerFw;

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
            loggerFw.write(getDateTime() + " " + type + message + "\n");
        } catch (IOException ioe) {
            System.out.println(getDateTime() + " " + ERROR + "Error writing to log file: " + ioe.getMessage());
        }
    }

    public static void open(String workingDir) throws IOException {
        try {
            loggerFw = new FileWriter(workingDir + "logs\\DCSDynamicWeather-Output.log", true);
        } catch (IOException ioe) {
            throw new IOException("Error opening log file");
        }
    }

    public static void close() throws IOException {
        try {
            loggerFw.close();
        } catch (IOException ioe) {
            throw new IOException("Error closing logger");
        }
    }

    private static String getDateTime() {
        return dtf.format(java.time.LocalDateTime.now());
    }

}
