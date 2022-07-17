package com.marlan.weatherupdate.service;

import static java.lang.System.getProperty;

public class DirHandler {
    public String getWorkingDir(String[] args){
        if (args.length == 0){
            return getProperty("user.dir") + "\\";
        } else {
            return args[0] + "\\";
        }
    }
}
