package com.marlan.weatherupdate.service;

import lombok.AllArgsConstructor;

import static java.lang.System.getenv;
import static java.lang.System.out;

@AllArgsConstructor
public class MizHandler {
    final String sevenZipPath = getenv("ProgramFiles") + "\\7-Zip\\7z.exe";
    final String dir;

    public void extractMission(String miz) throws Exception {
        out.println("Extracting mission");
        ProcessBuilder pb = new ProcessBuilder(
                sevenZipPath,
                "e",
                "-tzip",
                miz,
                "mission",
                "-y"
        );
        SevenZip sevenZip = new SevenZip(dir);
        sevenZip.runProcess(pb);
    }

    public void updateMiz(String miz, String missionFile) throws Exception {
        out.println("Updating .miz");
        ProcessBuilder pb = new ProcessBuilder(
                sevenZipPath,
                "a",
                "-tzip",
                miz,
                missionFile
        );
        SevenZip sevenZip = new SevenZip(dir);
        sevenZip.runProcess(pb);
    }
}
