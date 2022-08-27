package com.marlan.weatherupdate.utilities;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtility {
    private FileUtility() {
    }

    public static String readFile(String dir, String fileName) {
        Path filePath = Path.of(dir + fileName);
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1); // Program can't continue without the file, Lua Script can read error code.
        }
        return ""; // Unreachable
    }

    public static void overwriteFile(String dir, String fileName, String newContent) {
        File file = new File(dir + fileName);
        try (FileWriter fileWriter = new FileWriter(file, false)) {
            fileWriter.write(newContent);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1); // Program is useless without writing to file, Lua Script can read error code.
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendFile(String dir, String fileName, String content) {
        try (FileWriter fw = new FileWriter(dir + fileName, true)) {
            fw.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(String dir, String fileName) {
        Path filePath = Path.of(dir + fileName);
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
