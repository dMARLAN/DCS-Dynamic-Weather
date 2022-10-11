package com.marlan.weatheroutput;

import com.marlan.shared.utilities.DirHandler;
import com.marlan.shared.utilities.Log;
import com.marlan.weatheroutput.controller.WeatherOutputController;

/**
 * Entry Point for the Weather Output module of DCS Dynamic Weather
 * Outputs METAR from DTO to Discord Webhook and/or Google Sheets if config file is set to true for either.
 *
 * @author Chad Penarsky
 */
public class WeatherOutput {
    private static final Log log = Log.getInstance();

    public static void main(String[] args) {
        String workingDirectory = DirHandler.getWorkingDir(args);

        log.open(workingDirectory, "DCSDynamicWeather-WeatherOutput");
        WeatherOutputController.run(workingDirectory); // Start of the Weather Output program
        log.close();
    }
}
