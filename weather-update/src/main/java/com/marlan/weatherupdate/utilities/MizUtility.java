package com.marlan.weatherupdate.utilities;

import static java.lang.System.getenv;

public class MizUtility {
    private static final String SEVEN_ZIP_PATH = getenv("ProgramFiles") + "\\7-Zip\\7z.exe";

    private MizUtility() {
    }

    public static void extractMission(String dir, String mizName) {
        Logger.info("Extracting mission: " + dir + mizName);
        ProcessBuilder pb = new ProcessBuilder(
                SEVEN_ZIP_PATH,
                "x",
                "-tzip",
                dir + mizName,
                "-o" + dir,
                "mission",
                "-y"
        );
        SevenZipUtility.runProcess(pb, dir);
    }

    public static void updateMiz(String dir, String mizName, String missionFile) {
        Logger.info("Updating mission: " + dir + mizName);
        ProcessBuilder pb = new ProcessBuilder(
                SEVEN_ZIP_PATH,
                "a",
                "-tzip",
                dir + mizName,
                dir + missionFile
        );
        SevenZipUtility.runProcess(pb, dir);
    }
}
