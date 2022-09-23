package com.marlan.weatheroutput.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHandler {
    private FileHandler() {
    }

    public static String readFile(String workingDir, String fileName) throws IOException {
        Path dataFilePath = Path.of(workingDir + fileName);
        try {
            return Files.readString(dataFilePath);
        } catch (IOException ioe) {
            Logger.error("Error reading file: " + fileName);
            throw ioe;
        }
    }

}
