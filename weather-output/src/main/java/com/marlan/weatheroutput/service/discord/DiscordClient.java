package com.marlan.weatheroutput.service.discord;

import com.marlan.weatheroutput.model.DTO;
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

    public static void post(DTO dto, String message) throws URISyntaxException, IOException, InterruptedException {
        if (dto.getDiscordApiKey().length() == 0) {
            Logger.warning("Discord API key is empty");
        } else {
            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(new URI(dto.getDiscordApiKey()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(message))
                    .build();
            Logger.info("Discord API Reponse: " + httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString()).toString());
        }
    }

}
