package com.marlan.weatherupdate;

import com.marlan.shared.utilities.DirHandler;
import com.marlan.shared.utilities.Log;
import com.marlan.weatherupdate.controller.WeatherUpdateController;

/**
 * Entry Point for the Weather Update module of DCS Dynamic Weather
 * Extracts mission file from DCS *.miz and replaces values based on AVWX API's METAR return
 * or based on customized user input from DCS.
 *
 * @author Chad Penarsky
 */
public class WeatherUpdate {
    private static final Log log = Log.getInstance();

    public static void main(String[] args) {
        String workingDirectory = DirHandler.getWorkingDir(args);

        log.open(workingDirectory, "DCSDynamicWeather-WeatherUpdate");
        WeatherUpdateController.run(workingDirectory); // Start of the Weather Update program
        log.close();
    }
}
