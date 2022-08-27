package com.marlan.weatheroutput.utilities;

import java.io.FileWriter;
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

    public static void appendFile(String dir, String fileName, String content) {
        try (FileWriter fw = new FileWriter(dir + fileName, true)) {
            fw.write(content);
        } catch (IOException e) {
            // TODO: handle exception
        }
    }

}
