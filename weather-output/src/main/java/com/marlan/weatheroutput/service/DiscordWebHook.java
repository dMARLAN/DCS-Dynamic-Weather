package com.marlan.weatheroutput.service;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatheroutput.model.WeatherOutputData;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.System.*;

public class DiscordWebHook {
    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        final String DISCORD_API_KEY;
        final String METAR;
        final String DIR;

        if (args.length == 0) {
            DIR = getProperty("user.dir") + "\\";
        } else {
            DIR = args[0] + "\\";
        }

        Path dataFilePath = Path.of(DIR + "Data.txt");
        String dataFileContent = Files.readString(dataFilePath);
        WeatherOutputData weatherOutputData = gson.fromJson(dataFileContent, WeatherOutputData.class);
        DISCORD_API_KEY = weatherOutputData.getDiscordApiKey();
        METAR = weatherOutputData.getMetar();

        String jsonInput = """
                {
                  "embeds": [
                   {
                    "description": "$METAR",
                    "color": 0
                   }
                  ]
                }
                """.replace("$METAR", METAR);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(DISCORD_API_KEY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        out.println("Response Code: " + postResponse.statusCode());
    }
}
