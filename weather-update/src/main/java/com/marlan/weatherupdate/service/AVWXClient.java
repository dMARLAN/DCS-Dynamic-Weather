package com.marlan.weatherupdate.service;

import com.marlan.weatherupdate.model.dto.DTO;
import com.marlan.weatherupdate.model.metar.AVWXMetar;
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

    public HttpResponse<String> getMetar(DTO dto) throws URISyntaxException, IOException, InterruptedException, IllegalArgumentException {
        if ((dto.getAvwxApiKey().isEmpty()) || (dto.getStationLatitude().isEmpty()) || (dto.getStationLongitude().isEmpty())) {
            throw new IllegalArgumentException("Missing required parameters");
        } else {
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://avwx.rest/api/metar/" + dto.getStationLatitude() + "," + dto.getStationLongitude() + "?onfail=nearest"))
                    .header("Authorization", dto.getAvwxApiKey())
                    .build();

            return this.httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        }
    }

    public HttpResponse<String> getStation(DTO dto, AVWXMetar weatherAVWX) throws URISyntaxException, IOException, InterruptedException, IllegalArgumentException {
        if ((dto.getAvwxApiKey().length() == 0) || (weatherAVWX.getStation().length() == 0)) {
            throw new IllegalArgumentException("Missing required parameters");
        } else {
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://avwx.rest/api/station/" + weatherAVWX.getStation()))
                    .header("Authorization", dto.getAvwxApiKey())
                    .build();

            return this.httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        }
    }
}
