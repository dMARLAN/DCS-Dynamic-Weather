package com.marlan.weatherupdate.utilities;

import java.io.File;
import java.io.IOException;

import static java.lang.System.out;

public class SevenZipUtility {
    private SevenZipUtility() {
    }

    public static void runProcess(ProcessBuilder pb, String dir) {
        File processOutput = new File(dir + "logs\\SevenZip.log");
        try {
            if (processOutput.createNewFile()) {
                out.println("File created: " + processOutput.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        pb.redirectOutput(processOutput);
        try {
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1); // Program is useless without writing to file, Lua Script can read error code.
        }
    }
}
