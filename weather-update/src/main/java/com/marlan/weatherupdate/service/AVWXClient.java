package com.marlan.weatherupdate.service;

import com.marlan.weatherupdate.model.metar.AVWXMetar;
import com.marlan.weatherupdate.model.metar.fields.WeatherUpdateData;
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

    public HttpResponse<String> getMetar(WeatherUpdateData weatherUpdateData) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("https://avwx.rest/api/metar/" + weatherUpdateData.getIcao() + "?onfail=nearest"))
                .header("Authorization", weatherUpdateData.getAvwxApiKey())
                .build();

        return this.httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> getStation(WeatherUpdateData weatherUpdateData, AVWXMetar weatherAVWX) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("https://avwx.rest/api/station/" + weatherAVWX.getStation()))
                .header("Authorization", weatherUpdateData.getAvwxApiKey())
                .build();

        return this.httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
    }
}
