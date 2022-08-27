package com.marlan.weatherupdate.utilities;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class SevenZipUtility {
    private SevenZipUtility() {
    }

    public static void runProcess(ProcessBuilder pb, String dir) {
        File processOutput = new File(dir + "logs\\SevenZip.log");
        try {
            if (processOutput.createNewFile()) {
                Logger.info("Created new file: " + processOutput.getName());
            }
        } catch (IOException e) {
            Logger.error(Arrays.toString(e.getStackTrace()));
        }
        pb.redirectOutput(processOutput);
        try {
            Process p = pb.start();
            p.waitFor();
        } catch (IOException e) {
            Logger.error(Arrays.toString(e.getStackTrace()));
            System.exit(1); // Program is useless without writing to file, Lua Script can read error code.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.error(Arrays.toString(e.getStackTrace()));
            System.exit(1); // Program is useless without writing to file, Lua Script can read error code.
        }
    }
}
