package com.marlan.weatheroutput.service.discord;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatheroutput.model.DiscordWebhook;
import com.marlan.weatheroutput.utilities.FileHandler;
import com.marlan.weatheroutput.utilities.Log;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Handles posting METAR data to Discord Webhook
 */
public class DiscordClient {
    private static final Log log = Log.getInstance();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String workingDir;
    private final String message;

    public DiscordClient(String workingDir, String message) {
        this.workingDir = workingDir;
        this.message = message;
    }

    public void post() {
        final String DISCORD_KEY_PATH = "secrets\\discord_api_key.json";
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

        String discordKeyFile = FileHandler.readFile(workingDir, DISCORD_KEY_PATH);
        DiscordWebhook discordApiKey = gson.fromJson(discordKeyFile, DiscordWebhook.class);
        String discordKey = discordApiKey.getDiscordApiKey();

        if (discordKey.isEmpty()) {
            log.warning("Discord API key is empty");
        } else {
            try {
                HttpRequest postRequest = HttpRequest.newBuilder()
                        .uri(new URI(discordKey))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(message))
                        .build();
                log.info("Discord API Reponse: " + sendRequest(postRequest));
            } catch (URISyntaxException use) {
                log.error(use.getMessage());
            }
        }
    }

    private String sendRequest(HttpRequest postRequest) {
        try {
            return httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString()).toString();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage());
        }
        return null;
    }

}
