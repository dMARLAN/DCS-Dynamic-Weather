package com.marlan.weatherupdate.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.System.getProperty;

public class DirHandler {
    private DirHandler() {
    }

    public static String getWorkingDir(String[] args) throws IOException {
        if (args.length != 0 && Files.exists(Path.of(args[0]))) {
            System.setProperty("user.dir", args[0]);
        }
        String workingDir = getProperty("user.dir") + "\\";
        if (!Files.exists(Path.of(workingDir))) {
            throw new IOException("Working directory inaccessible");
        }
        return workingDir;
    }

}
