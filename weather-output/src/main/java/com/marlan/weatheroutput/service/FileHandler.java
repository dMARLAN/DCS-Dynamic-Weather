package com.marlan.weatheroutput.service;

import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@NoArgsConstructor
public class FileHandler {

    public String readFile(String dir, String fileName) throws IOException {
        Path dataFilePath = Path.of(dir + fileName);
        return Files.readString(dataFilePath);
    }

}
