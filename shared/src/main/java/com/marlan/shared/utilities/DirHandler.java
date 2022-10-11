package com.marlan.shared.utilities;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.System.getProperty;

/**
 * Returns working directory using either entry points's main String[] args passed through
 * from weather-scripts/utilities/JAR or if args are empty, then use user.dir
 * If args directory does not exist or cannot be accessed, program exits because if a custom directory
 * is specified, then it is required to be accessible.
 */
public class DirHandler {
    private DirHandler() {
    }

    @NotNull
    public static String getWorkingDir(String[] args) {
        if (args != null && args.length != 0) {
            if (!Files.exists(Path.of(args[0]))) {
                System.out.println("ERROR: Directory does not exist: " + args[0]);
                System.exit(1); // Unrecoverable error
            }
            System.setProperty("user.dir", args[0]);
        }
        String workingDir = getProperty("user.dir") + "\\";
        if (!Files.exists(Path.of(workingDir))) {
            System.out.println("ERROR: Directory inaccessible " + workingDir);
            System.exit(1); // Unrecoverable error
        }
        return workingDir;
    }

}
