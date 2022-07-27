package com.marlan.weatherupdate.utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.out;

public class SevenZipUtility {
    private SevenZipUtility() {
    }

    public static void runProcess(ProcessBuilder pb, String dir) throws IOException, InterruptedException {
        File processOutput = new File(dir + "logs\\SevenZip.txt");
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
        if (p.waitFor() != 0) {
            out.println("ERROR: SevenZip process failed with exit code:" + p.waitFor());
        } else {
            out.println("INFO: SevenZip process finished successfully");
        }
    }

    public record InputConsumer(InputStream is) implements Runnable {
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
