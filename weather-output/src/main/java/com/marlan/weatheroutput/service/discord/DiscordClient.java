package com.marlan.weatheroutput.service.discord;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatheroutput.model.DiscordWebhookAPI;
import com.marlan.weatheroutput.utilities.FileHandler;
import com.marlan.weatheroutput.utilities.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DiscordClient {
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private DiscordClient() {
    }

    public static void post(String workingDir, String message) throws URISyntaxException, IOException, InterruptedException {
        final String DISCORD_KEY_PATH = "secrets\\discord_api_key.json";
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

        String discordKeyFile = FileHandler.readFile(workingDir, DISCORD_KEY_PATH);
        DiscordWebhookAPI discordApiKey = gson.fromJson(discordKeyFile, DiscordWebhookAPI.class);
        String discordKey = discordApiKey.getDiscordApiKey();

        if (discordKey.isEmpty()) {
            Logger.warning("Discord API key is empty");
        } else {
            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(new URI(discordKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(message))
                    .build();
            Logger.info("Discord API Reponse: " + httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString()).toString());
        }
    }

}
