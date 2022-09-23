package com.marlan.weatherupdate.utilities;

import com.marlan.weatherupdate.model.config.Config;

import java.io.IOException;

import static java.lang.System.getenv;

public class MizUtility {
    private final String sevenZipPath;

    public MizUtility(Config config) {
        if (config.getCustomSevenZipPath().isEmpty()) {
            this.sevenZipPath = getenv("ProgramFiles") + "\\7-Zip\\7z.exe";
        } else {
            this.sevenZipPath = config.getCustomSevenZipPath();
        }
    }

    public void extractMission(String dir, String mizName) {
        Logger.info("Extracting: " + dir + mizName);
        ProcessBuilder pb = new ProcessBuilder(
                this.sevenZipPath,
                "x",
                "-tzip",
                dir + mizName,
                "-o" + dir,
                "mission",
                "-y"
        );
        try {
            runProcess(pb);
        } catch (IOException e) {
            Logger.error("Error extracting .miz: " + e.getMessage());
            System.exit(1);
        }

    }

    public void updateMiz(String dir, String mizName, String missionFile) {
        Logger.info("Updating: " + dir + mizName);
        ProcessBuilder pb = new ProcessBuilder(
                this.sevenZipPath,
                "a",
                "-tzip",
                dir + mizName,
                dir + missionFile
        );
        try {
            runProcess(pb);
        } catch (IOException e) {
            Logger.error("Error updating .miz: " + e.getMessage());
            System.exit(1);
        }
    }

    private void runProcess(ProcessBuilder pb) throws IOException {
        try {
            Process p = pb.start();
            p.waitFor();
        } catch (IOException ioe) {
            throw new IOException(ioe);
        } catch (InterruptedException ie) {
            Logger.error("Error running process: " + ie.getMessage());
            Thread.currentThread().interrupt();
            System.exit(1);
        }
    }
}
