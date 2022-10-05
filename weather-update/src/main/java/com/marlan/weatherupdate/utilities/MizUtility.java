package com.marlan.weatherupdate.utilities;

import com.marlan.weatherupdate.model.config.Config;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static java.lang.System.getenv;

/**
 * Handles extracting mission file from DCS *.miz archive file and updating with the new
 * mission file. Uses 7zip to handle extraction/rearchiving.
 */
public class MizUtility {
    private final String sevenZipPath;

    public MizUtility(@NotNull Config config) {
        if (config.getCustomSevenZipPath().isEmpty()) {
            this.sevenZipPath = getenv("ProgramFiles") + "\\7-Zip\\7z.exe";
        } else {
            this.sevenZipPath = config.getCustomSevenZipPath();
        }
    }

    /**
     * Extracts mission file from .miz using 7zip
     */
    public void extractMission(String dir, String mizName) {
        Log.info("Extracting: " + dir + mizName);
        ProcessBuilder pb = new ProcessBuilder(
                this.sevenZipPath,
                "x",
                "-tzip",
                dir + mizName,
                "-o" + dir,
                "mission",
                "-y"
        );
        runProcess(pb);
    }

    /**
     * Updates .miz with new mission file using 7zip
     */
    public void updateMiz(String dir, String mizName, String missionFile) {
        Log.info("Updating: " + dir + mizName);
        ProcessBuilder pb = new ProcessBuilder(
                this.sevenZipPath,
                "a",
                "-tzip",
                dir + mizName,
                dir + missionFile
        );
        runProcess(pb);
    }

    private void runProcess(@NotNull ProcessBuilder pb) {
        try {
            Process p = pb.start();
            p.waitFor();
        } catch (IOException ioe) {
            Log.error("Error running process: " + ioe.getMessage());
        } catch (InterruptedException ie) {
            Log.error("Error running process: " + ie.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
