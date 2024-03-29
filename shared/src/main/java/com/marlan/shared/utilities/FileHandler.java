package com.marlan.shared.utilities;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles files other than the logger, primarily for reading/writing JSON
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

    public static void overwriteFile(String dir, String fileName, String newContent) {
        File file = new File(dir + fileName);
        try (FileWriter fileWriter = new FileWriter(file, false)) {
            fileWriter.write(newContent);
        } catch (IOException ioe) {
            log.error("Could not overwrite file: " + ioe.getMessage());
        }
    }

    public static void writeJSON(String dir, String fileName, Object jsonObject) {
        File file = new File(dir + fileName);
        try (FileWriter fileWriter = new FileWriter(file, false)) {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();
            gson.toJson(jsonObject, fileWriter);
            log.info("Wrote JSON to " + fileName);
        } catch (IOException ioe) {
            log.error("Could not write JSON: " + ioe.getMessage());
        }
    }

    public static void deleteFile(String dir, String fileName) {
        Path filePath = Path.of(dir + fileName);
        try {
            Files.delete(filePath);
        } catch (IOException ioe) {
            log.error("Could not delete file: " + ioe.getMessage());
        }
    }

}
