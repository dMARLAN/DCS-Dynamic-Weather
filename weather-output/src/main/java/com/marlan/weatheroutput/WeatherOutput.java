package com.marlan.weatheroutput;

import com.marlan.weatheroutput.controller.DiscordWebHookService;
import com.marlan.weatheroutput.utilities.DirHandler;
import com.marlan.weatheroutput.utilities.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

public class WeatherOutput {
    public static void main(String[] args) throws IOException, GeneralSecurityException, URISyntaxException, InterruptedException {
        final String WORKING_DIR = DirHandler.getWorkingDir(args);
        Logger.open(WORKING_DIR);
        Logger.info("Working Directory: " + WORKING_DIR);

        DiscordWebHookService.run(WORKING_DIR);

        Logger.close();
    }
}
