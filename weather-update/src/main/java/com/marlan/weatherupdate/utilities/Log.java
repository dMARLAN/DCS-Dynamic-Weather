package com.marlan.weatherupdate.utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

/**
 * Logger for DCS Dynamic Weather, prints to logs folder.
 */
public class Log {
    private static final String INFO = "INFO    ";
    private static final String WARNING = "WARNING ";
    private static final String ERROR = "ERROR   ";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static FileWriter loggerFw;

    private Log() {
    }

    /**
     * Prints to log file with "INFO" prefix.
     */
    public static void info(String message) {
        log(INFO, message);
    }

    /**
     * Prints to log file with "WARNING" prefix.
     */
    public static void warning(String message) {
        log(WARNING, message);
    }

    /**
     * Prints to log file with "ERROR" prefix.
     */
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

    /**
     * Opens logger's file writer and writes to logs\\DCSDynamicWeather-Update.log
     * @throws IOException If log file cannot be opened
     */
    public static void open(String dir) throws IOException {
        String logPath = dir + "logs\\DCSDynamicWeather-Update.log";
        try {
            Files.createDirectories(Path.of(dir + "logs"));
            loggerFw = new FileWriter(logPath, true);
        } catch (IOException ioe) {
            throw new IOException("Error opening log file");
        }
    }

    /**
     * Closes & flushes logger's file writer.
     * @throws IOException If log file cannot be closed
     */
    public static void close() throws IOException {
        try {
            loggerFw.flush();
            loggerFw.close();
        } catch (IOException ioe) {
            throw new IOException("Error closing logger");
        }
    }

    private static String getDateTime() {
        return dtf.format(java.time.LocalDateTime.now());
    }

}
