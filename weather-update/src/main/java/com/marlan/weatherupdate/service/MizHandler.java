package com.marlan.weatherupdate.service;

import com.marlan.weatherupdate.processor.Processor;
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
        Processor processor = new Processor();
        processor.runProcess(pb);
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
        Processor processor = new Processor();
        processor.runProcess(pb);
    }
}
