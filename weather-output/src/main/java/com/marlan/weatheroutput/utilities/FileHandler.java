package com.marlan.weatheroutput.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles reading JSON
 */
public class FileHandler {
    private FileHandler() {
    }

    public static String readFile(String dir, String fileName) throws IOException {
        Path filePath = Path.of(dir + fileName);
        try {
            return Files.readString(filePath);
        } catch (IOException ioe) {
            Log.error(ioe.getMessage());
            throw new IOException("Error: " + ioe.getMessage(), ioe);
        }
    }

}
