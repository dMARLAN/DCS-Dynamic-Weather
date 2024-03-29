package com.marlan.shared.utilities;

import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

/**
 * Logger for DCS Dynamic Weather, prints to logs folder.
 */
public class Log {
    private static Log instance;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private FileWriter loggerFw;
    private boolean isOpen = false;

    private Log() {
    }

    public static Log getInstance() {
        if (instance == null) {
            instance = new Log();
        }
        return instance;
    }

    public void info(String message) {
        final String INFO = "INFO    ";
        log(INFO, message);
    }

    public void warning(String message) {
        final String WARNING = "WARNING ";
        log(WARNING, message);
    }

    public void error(String message) {
        final String ERROR = "ERROR   ";
        log(ERROR, message);
    }

    private void log(String type, String message) {
        if (isOpen) {
            System.out.println(getDateTime() + " " + type + message);
            try {
                loggerFw.write(getDateTime() + " " + type + message + "\n");
            } catch (IOException ioe) {
                System.out.println(getDateTime() + " ERROR   " + "Error writing to log file: " + ioe.getMessage());
            }
        }
    }

    public void open(String dir, String fileName) {
        String timeStamp = DateTimeFormatter.ofPattern("-yyMMdd").format(java.time.LocalDateTime.now());
        String logPath = dir + "logs\\" + fileName + timeStamp + ".log";
        try {
            Files.createDirectories(Path.of(dir + "logs"));
            loggerFw = new FileWriter(logPath, true);
            isOpen = true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void close() {
        try {
            loggerFw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @NotNull
    private String getDateTime() {
        return dtf.format(java.time.LocalDateTime.now());
    }

}
