package com.marlan.weatherupdate.service;

import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.getProperty;
import static java.lang.System.out;

public class SevenZipService {
    public void runProcess(ProcessBuilder pb) throws Exception {
        File processOutput = new File(getProperty("user.dir") + "\\Logs\\SevenZipService.txt");
        try {
            if (processOutput.createNewFile()) {
                out.println("File created: " + processOutput.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        pb.redirectOutput(processOutput);
        Process p = pb.start();
        new Thread(new InputConsumer(p.getInputStream())).start();
        out.println("MizHandler finished with exit code: " + p.waitFor());
    }

    @AllArgsConstructor
    public static class InputConsumer implements Runnable {
        private final InputStream is;

        @Override
        public void run() {
            try {
                int value;
                while ((value = is.read()) != -1) {
                    out.print((char) value);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
