package com.marlan.weatheroutput.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.shared.model.Config;
import com.marlan.shared.model.DTO;
import com.marlan.shared.utilities.FileHandler;
import com.marlan.shared.utilities.Log;
import com.marlan.weatheroutput.service.discord.DiscordClient;
import com.marlan.weatheroutput.service.sheets.SheetsClient;

/**
 * Controller for Weather Output module
 */
public class WeatherOutputController {
    private static final Log log = Log.getInstance();

    private WeatherOutputController() {
    }

    /**
     * @param WORKING_DIR Working directory of the program which is the location of this file (which should also include
     *                    the other folders and files needed for the program to run e.g. data, constants, secrets, etc.)
     */
    public static void run(final String WORKING_DIR) {


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
            DiscordClient discordClient = new DiscordClient(WORKING_DIR, jsonInput);
            discordClient.post();
        } else {
            log.info("Discord Webhook output is disabled, skipping...");
        }
        if (config.isOutputToSheets()) {
            SheetsClient.setSheetValue(config.getSpreadsheetId(), config.getSpreadsheetRange(), dto.getMetar(), WORKING_DIR);
        } else {
            log.info("Google Sheets output is disabled, skipping...");
        }
    }
}
