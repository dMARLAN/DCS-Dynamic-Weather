package com.marlan.weatherupdate.utilities;

import com.marlan.weatherupdate.model.config.Config;

import java.io.IOException;

import static java.lang.System.getenv;

/**
 * Handles extracting mission file from DCS *.miz archive file and updating with the new
 * mission file. Uses 7zip to handle extraction/rearchiving.
 */
public class MizUtility {
    private final String sevenZipPath;

    public MizUtility(Config config) {
        if (config.getCustomSevenZipPath().isEmpty()) {
            this.sevenZipPath = getenv("ProgramFiles") + "\\7-Zip\\7z.exe";
        } else {
            this.sevenZipPath = config.getCustomSevenZipPath();
        }
    }

    /**
     * Extracts mission file from .miz using 7zip
     * @param workingDir Working Directory
     * @param mizName Name of miz file
     */
    public void extractMission(String workingDir, String mizName) {
        Log.info("Extracting: " + workingDir + mizName);
        ProcessBuilder pb = new ProcessBuilder(
                this.sevenZipPath,
                "x",
                "-tzip",
                workingDir + mizName,
                "-o" + workingDir,
                "mission",
                "-y"
        );
        try {
            runProcess(pb);
        } catch (IOException e) {
            Log.error("Error extracting .miz: " + e.getMessage());
            System.exit(1);
        }

    }

    /**
     * Updates .miz with new mission file using 7zip
     * @param workingDir Working Directory
     * @param mizName Name of miz file
     * @param missionFile Name of new mission file
     */
    public void updateMiz(String workingDir, String mizName, String missionFile) {
        Log.info("Updating: " + workingDir + mizName);
        ProcessBuilder pb = new ProcessBuilder(
                this.sevenZipPath,
                "a",
                "-tzip",
                workingDir + mizName,
                workingDir + missionFile
        );
        try {
            runProcess(pb);
        } catch (IOException e) {
            Log.error("Error updating .miz: " + e.getMessage());
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
            Log.error("Error running process: " + ie.getMessage());
            Thread.currentThread().interrupt();
            System.exit(1);
        }
    }
}
