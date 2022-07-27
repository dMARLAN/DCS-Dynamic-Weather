package com.marlan.weatheroutput.service;

import com.marlan.weatheroutput.model.DAO;

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

    public static HttpResponse<String> postChannel(DAO dao, String message) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(dao.getDiscordApiKey()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build();

        return httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
    }

}
