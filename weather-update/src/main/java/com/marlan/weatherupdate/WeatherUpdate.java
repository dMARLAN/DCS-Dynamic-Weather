package com.marlan.weatherupdate;

import com.marlan.weatherupdate.controller.WeatherUpdateController;
import com.marlan.weatherupdate.utilities.DirHandler;
import com.marlan.weatherupdate.utilities.Log;

import java.io.IOException;
import java.net.URISyntaxException;

public class WeatherUpdate {
    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        final String WORKING_DIR = DirHandler.getWorkingDir(args);
        Log.open(WORKING_DIR);
        Log.info("Working Directory: " + WORKING_DIR);

        WeatherUpdateController.run(WORKING_DIR);

        Log.close();
    }
}
