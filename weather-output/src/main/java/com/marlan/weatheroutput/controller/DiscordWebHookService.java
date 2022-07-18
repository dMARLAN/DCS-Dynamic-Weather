package com.marlan.weatheroutput.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatheroutput.model.WeatherOutputData;
import com.marlan.weatheroutput.service.DirHandler;
import com.marlan.weatheroutput.service.DiscordClient;
import com.marlan.weatheroutput.service.FileHandler;

import java.io.IOException;
import java.net.URISyntaxException;

import static java.lang.System.out;

public class DiscordWebHookService {
    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        DirHandler dirHandler = new DirHandler();
        FileHandler fileHandler = new FileHandler();
        DiscordClient discordClient = new DiscordClient();

        String dataFileContent = fileHandler.readFile(dirHandler.getWorkingDir(args), "Data.txt");

        WeatherOutputData weatherOutputData = gson.fromJson(dataFileContent, WeatherOutputData.class);

        String jsonInput = """
                {
                  "embeds": [
                   {
                    "description": "$METAR",
                    "color": 0
                   }
                  ]
                }
                """.replace("$METAR", weatherOutputData.getMetar());

        out.println("Response Code: " + discordClient.postChannel(weatherOutputData, jsonInput).statusCode());
    }
}
