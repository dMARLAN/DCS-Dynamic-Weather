package com.marlan.weatherupdate;

import com.marlan.weatherupdate.controller.WeatherUpdateController;
import com.marlan.weatherupdate.utilities.DirHandler;
import com.marlan.weatherupdate.utilities.Log;

import java.io.IOException;

/**
 * Entry Point for the Weather Update module of DCS Dynamic Weather
 * Extracts mission file from DCS *.miz and replaces values based on AVWX API's METAR return
 * or based on customized user input from DCS.
 *
 * @author Chad Penarsky
 */
public class WeatherUpdate {

    public static void main(String[] args) {
        final String WORKING_DIR;
        try {
            WORKING_DIR = DirHandler.getWorkingDir(args);
            Log.open(WORKING_DIR);
            WeatherUpdateController.run(WORKING_DIR); // Start of the Weather Update program
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }
    }
}
