package com.marlan.weatheroutput;

import com.marlan.shared.utilities.DirHandler;
import com.marlan.shared.utilities.Log;
import com.marlan.weatheroutput.controller.WeatherOutputController;

import java.io.IOException;

/**
 * Entry Point for the Weather Output module of DCS Dynamic Weather
 * Outputs METAR from DTO to Discord Webhook and/or Google Sheets if config file is set to true for either.
 *
 * @author Chad Penarsky
 */
public class WeatherOutput {
    private static final Log log = Log.getInstance();

    public static void main(String[] args) {
        String workingDirectory;
        try {
            workingDirectory = DirHandler.getWorkingDir(args);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            workingDirectory = "";
        }

        log.open(workingDirectory, "DCSDynamicWeather-WeatherOutput.log");
        WeatherOutputController.run(workingDirectory); // Start of the Weather Output program
        log.close();
    }
}
