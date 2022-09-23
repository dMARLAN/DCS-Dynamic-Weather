package com.marlan.weatheroutput.utilities;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.System.getProperty;

public class DirHandler {
    private DirHandler() {
    }

    public static String getWorkingDir(String[] args) {
        if (args.length != 0 && Files.exists(Path.of(args[0]))) {
            System.setProperty("user.dir", args[0]);
        }
        String workingDir = getProperty("user.dir") + "\\";
        Logger.setDir(workingDir);
        if (!Files.exists(Path.of(workingDir))) {
            Logger.error("Working Directory inaccessible.");
            System.exit(1); // Unrecoverable Error
        }
        Logger.info("Working Directory: " + workingDir);
        return workingDir;
    }

}
