package com.marlan.weatheroutput.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles reading JSON
 */
public class FileHandler {
    private static final Log log = Log.getInstance();

    private FileHandler() {
    }

    public static String readFile(String dir, String fileName) {
        Path filePath = Path.of(dir + fileName);
        try {
            return Files.readString(filePath);
        } catch (IOException ioe) {
            log.error("Could not open file: " + ioe.getMessage());
        }
        return "";
    }

}
