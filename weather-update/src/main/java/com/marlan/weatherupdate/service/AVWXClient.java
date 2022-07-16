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
public class AVWXClient {
    HttpClient httpClient = HttpClient.newHttpClient();

    public HttpResponse<String> getWeather(WeatherUpdateData weatherUpdateData) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("https://avwx.rest/api/metar/" + weatherUpdateData.getIcao()))
                .header("Authorization", weatherUpdateData.getAvwxApiKey())
                .build();

        return this.httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
    }
}
