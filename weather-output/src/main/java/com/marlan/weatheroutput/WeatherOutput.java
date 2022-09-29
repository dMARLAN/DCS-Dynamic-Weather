package com.marlan.weatheroutput;

import com.marlan.weatheroutput.controller.WeatherOutputController;
import com.marlan.weatheroutput.utilities.DirHandler;
import com.marlan.weatheroutput.utilities.Log;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

/**
 * Entry Point for the Weather Output module of DCS Dynamic Weather
 * Outputs METAR from DTO to Discord Webhook and/or Google Sheets if config file is set to true for either.
 * @author Chad Penarsky
 */
public class WeatherOutput {
    public static void main(String[] args) throws IOException, GeneralSecurityException, URISyntaxException, InterruptedException {
        final String WORKING_DIR = DirHandler.getWorkingDir(args);
        Log.open(WORKING_DIR);
        Log.info("Working Directory: " + WORKING_DIR);

        WeatherOutputController.run(WORKING_DIR);

        Log.close();
    }
}
