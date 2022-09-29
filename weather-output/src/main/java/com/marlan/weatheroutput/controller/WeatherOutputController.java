package com.marlan.weatheroutput.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatheroutput.model.Config;
import com.marlan.weatheroutput.model.DTO;
import com.marlan.weatheroutput.service.discord.DiscordClient;
import com.marlan.weatheroutput.service.sheets.SheetsClient;
import com.marlan.weatheroutput.utilities.FileHandler;
import com.marlan.weatheroutput.utilities.Log;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

/**
 * Controller for Weather Output module
 */
public class WeatherOutputController {
    private WeatherOutputController() {
    }

    public static void run(final String WORKING_DIR) throws IOException, URISyntaxException, InterruptedException, GeneralSecurityException {

        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

        final String DTO_PATH = "data\\dto.json";
        final String CONFIG_PATH = "config.json";

        String dtoFileContent = FileHandler.readFile(WORKING_DIR, DTO_PATH);
        String configFileContent = FileHandler.readFile(WORKING_DIR, CONFIG_PATH);

        DTO dto = gson.fromJson(dtoFileContent, DTO.class);
        Config config = gson.fromJson(configFileContent, Config.class);

        String jsonInput = """
                {
                  "embeds": [
                   {
                    "description": "$METAR",
                    "color": 0
                   }
                  ]
                }
                """.replace("$METAR", dto.getMetar());

        if (config.isOutputToDiscord()) {
            DiscordClient.post(WORKING_DIR, jsonInput);
        } else {
            Log.info("Discord Webhook output is disabled, skipping...");
        }
        if (config.isOutputToSheets()) {
            SheetsClient.setSheetValue(config.getSpreadsheetId(), config.getSpreadsheetRange(), dto.getMetar(), WORKING_DIR);
        } else {
            Log.info("Google Sheets output is disabled, skipping...");
        }
    }
}
