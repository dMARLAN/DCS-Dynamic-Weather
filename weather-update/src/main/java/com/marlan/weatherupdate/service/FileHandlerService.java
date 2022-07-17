package com.marlan.weatherupdate.service;

import lombok.Data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class FileHandlerService {

    public String readFile(String dir, String fileName) throws IOException {
        Path dataFilePath = Path.of(dir + fileName);
        return Files.readString(dataFilePath);
    }

    public void overwriteFile(String dir, String fileName, String newContent) {
        File missionFile = new File(dir + fileName);
        try (FileWriter fileWriter = new FileWriter(missionFile, false)) {
            fileWriter.write(newContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(String dir, String fileName){
        File file = new File(dir + fileName);
        file.deleteOnExit();
    }
}
