package com.marlan.weatherupdate.utilities;

import static java.lang.System.getProperty;
import static java.lang.System.out;

public class DirUtility {
    private static final String USER_DIR = "user.dir";

    private DirUtility() {
    }

    public static String getWorkingDir(String[] args) {
        if (args.length != 0) {
            System.setProperty(USER_DIR, args[0]);
        }
        out.println("INFO: Working Directory: " + getProperty(USER_DIR) + "\\");
        return getProperty(USER_DIR) + "\\";
    }

}
