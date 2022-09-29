package com.marlan.weatherupdate;

import com.marlan.weatherupdate.controller.WeatherUpdateController;
import com.marlan.weatherupdate.utilities.DirHandler;
import com.marlan.weatherupdate.utilities.Logger;

import java.io.IOException;
import java.net.URISyntaxException;

public class WeatherUpdate {
    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        final String WORKING_DIR = DirHandler.getWorkingDir(args);
        Logger.open(WORKING_DIR);
        Logger.info("Working Directory: " + WORKING_DIR);

        WeatherUpdateController.run(WORKING_DIR);

        Logger.close();
    }
}
