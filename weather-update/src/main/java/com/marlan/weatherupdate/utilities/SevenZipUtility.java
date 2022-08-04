package com.marlan.weatherupdate.utilities;

import java.io.File;
import java.io.IOException;

import static java.lang.System.out;

public class SevenZipUtility {
    private SevenZipUtility() {
    }

    public static void runProcess(ProcessBuilder pb, String dir) throws IOException, InterruptedException {
        File processOutput = new File(dir + "logs\\SevenZip.log");
        try {
            if (processOutput.createNewFile()) {
                out.println("File created: " + processOutput.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        pb.redirectOutput(processOutput);
        Process p = pb.start();
        if (p.waitFor() != 0) {
            out.println("ERROR: SevenZip process failed with exit code:" + p.waitFor());
        } else {
            out.println("INFO: SevenZip process finished successfully");
        }
    }
}
