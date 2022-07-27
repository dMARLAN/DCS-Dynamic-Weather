package com.marlan.weatherupdate.service;

import com.marlan.weatherupdate.model.datafile.DAO;
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

    public HttpResponse<String> getMetar(DAO DAO) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("https://avwx.rest/api/metar/" + DAO.getStationLatitude() + "," + DAO.getStationLongitude() + "?onfail=nearest"))
                .header("Authorization", DAO.getAvwxApiKey())
                .build();

        return this.httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> getStation(DAO DAO, AVWXMetar weatherAVWX) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("https://avwx.rest/api/station/" + weatherAVWX.getStation()))
                .header("Authorization", DAO.getAvwxApiKey())
                .build();

        return this.httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
    }
}
