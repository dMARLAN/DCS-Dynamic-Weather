package com.marlan.weatherupdate.service;

import lombok.NoArgsConstructor;

import static java.lang.System.getenv;
import static java.lang.System.out;

@NoArgsConstructor
public class MizHandler {
    final String sevenZipPath = getenv("ProgramFiles") + "\\7-Zip\\7z.exe";

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
        SevenZip sevenZip = new SevenZip();
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
        SevenZip sevenZip = new SevenZip();
        sevenZip.runProcess(pb);
    }
}
