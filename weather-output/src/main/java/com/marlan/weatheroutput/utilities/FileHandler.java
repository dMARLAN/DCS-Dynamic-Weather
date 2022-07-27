package com.marlan.weatheroutput.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHandler {
    private FileHandler() {
    }

    public static String readFile(String dir, String fileName) throws IOException {
        Path dataFilePath = Path.of(dir + fileName);
        return Files.readString(dataFilePath);
    }

}
