package com.marlan.weatherupdate.service;

import lombok.NoArgsConstructor;

import static java.lang.System.getenv;
import static java.lang.System.out;

@NoArgsConstructor
public class MizHandlerService {
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
        SevenZipService sevenZipService = new SevenZipService();
        sevenZipService.runProcess(pb);
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
        SevenZipService sevenZipService = new SevenZipService();
        sevenZipService.runProcess(pb);
    }
}
