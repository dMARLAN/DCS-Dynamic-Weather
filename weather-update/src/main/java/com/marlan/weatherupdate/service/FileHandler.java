package com.marlan.weatherupdate.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

@NoArgsConstructor
public class FileHandler {

    public String readFile(String dir, String fileName) {
        try {
            Path filePath = Path.of(dir + fileName);
            return Files.readString(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void overwriteFile(String dir, String fileName, String newContent) {
        File file = new File(dir + fileName);
        try (FileWriter fileWriter = new FileWriter(file, false)) {
            fileWriter.write(newContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeJSON(String dir, String fileName, String jsonObject) {
        File file = new File(dir + fileName);
        try (FileWriter fileWriter = new FileWriter(file, false)) {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            gson.toJson(jsonObject, fileWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(String dir, String fileName) {
        try {
            Path filePath = Path.of(dir + fileName);
            Files.delete(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
