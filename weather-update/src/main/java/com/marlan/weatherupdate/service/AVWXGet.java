package com.marlan.weatherupdate.service;

import com.marlan.weatherupdate.model.WeatherUpdateData;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@NoArgsConstructor
public class AVWXGet {
    public HttpResponse<String> getRequest(WeatherUpdateData weatherUpdateData) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("https://avwx.rest/api/metar/" + weatherUpdateData.getIcao()))
                .header("Authorization", weatherUpdateData.getAvwxApiKey())
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        return httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
    }
}
