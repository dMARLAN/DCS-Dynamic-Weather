package com.marlan.weatherupdate.service;

import lombok.AllArgsConstructor;

import static java.lang.System.getenv;
import static java.lang.System.out;

@AllArgsConstructor
public class MizHandler {
    final String sevenZipPath = getenv("ProgramFiles") + "\\7-Zip\\7z.exe";

    public void extractMission(String dir, String mizName) throws Exception {
        out.println("INFO: Extracting mission from " + dir + mizName);
        ProcessBuilder pb = new ProcessBuilder(
                sevenZipPath,
                "x",
                "-tzip",
                dir + mizName,
                "-o" + dir,
                "mission",
                "-y"
        );
        SevenZip sevenZip = new SevenZip(dir);
        sevenZip.runProcess(pb);
    }

    public void updateMiz(String dir, String mizName, String missionFile) throws Exception {
        out.println("INFO: Updating " + dir + mizName);
        ProcessBuilder pb = new ProcessBuilder(
                sevenZipPath,
                "a",
                "-tzip",
                dir + mizName,
                dir + missionFile
        );
        SevenZip sevenZip = new SevenZip(dir);
        sevenZip.runProcess(pb);
    }
}
