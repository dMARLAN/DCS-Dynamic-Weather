package com.marlan.weatheroutput.service;

import static java.lang.System.getProperty;

public class DirHandler {
    public String getWorkingDir(String[] args){
        if (args.length != 0) {
            System.setProperty("user.dir", args[0]);
        }
        return getProperty("user.dir") + "\\";
    }
}
