package com.marlan.weatheroutput.service;

import com.marlan.weatheroutput.model.WeatherOutputData;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@NoArgsConstructor
public class DiscordClient {
    HttpClient httpClient = HttpClient.newHttpClient();

    public HttpResponse<String> postChannel(WeatherOutputData weatherOutputData, String message) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(weatherOutputData.getDiscordApiKey()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build();

        return httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
    }
}
