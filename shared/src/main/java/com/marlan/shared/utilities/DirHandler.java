package com.marlan.shared.utilities;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.System.getProperty;

/**
 * Returns working directory using either entry points's main String[] args passed through
 * from weather-scripts/utilities/JAR or if args are empty, then use user.dir
 */
public class DirHandler {
    private DirHandler() {
    }

    @NotNull
    public static String getWorkingDir(String[] args) throws IOException {
        if (args != null && args.length != 0) {
            if (!Files.exists(Path.of(args[0]))) {
                throw new IOException("Directory does not exist: " + args[0]);
            }
            System.setProperty("user.dir", args[0]);
        }
        String workingDir = getProperty("user.dir") + "\\";
        if (!Files.exists(Path.of(workingDir))) {
            throw new IOException("Working directory inaccessible");
        }
        return workingDir;
    }

}
