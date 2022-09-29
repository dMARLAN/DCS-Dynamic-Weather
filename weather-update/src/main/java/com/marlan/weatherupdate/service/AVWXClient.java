package com.marlan.weatherupdate.service;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatherupdate.model.dto.DTO;
import com.marlan.weatherupdate.model.metar.AVWXMetar;
import com.marlan.weatherupdate.model.secrets.AVWX;
import com.marlan.weatherupdate.utilities.FileHandler;
import com.marlan.weatherupdate.utilities.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AVWXClient {
    private final String dir;
    HttpClient httpClient = HttpClient.newHttpClient();

    public AVWXClient(final String dir) {
        this.dir = dir;
    }

    public HttpResponse<String> getMetar(DTO dto) throws URISyntaxException, IOException, InterruptedException, IllegalArgumentException {

        String avwxKey = getAVWXApiKey();
        if (avwxKey.isEmpty() || dto.getStationLatitude().isEmpty() || dto.getStationLongitude().isEmpty()) {
            String errorMessage = "AVWX API Key, Station Latitude and Station Longitude are required";
            Logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        } else {
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://avwx.rest/api/metar/" + dto.getStationLatitude() + "," + dto.getStationLongitude() + "?onfail=nearest"))
                    .header("Authorization", avwxKey)
                    .build();

            return this.httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        }
    }

    public HttpResponse<String> getStation(AVWXMetar weatherAVWX) throws URISyntaxException, IOException, InterruptedException, IllegalArgumentException {
        String avwxKey = getAVWXApiKey();
        if (avwxKey.isEmpty() || weatherAVWX.getStation().isEmpty()) {
            String errorMessage = "AVWX API Key and Station are required";
            Logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        } else {
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://avwx.rest/api/station/" + weatherAVWX.getStation()))
                    .header("Authorization", avwxKey)
                    .build();

            return this.httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        }
    }

    private String getAVWXApiKey() {
        final String AVWX_API_KEY_PATH = "secrets\\avwx_api_key.json";
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

        String avwxKeyFile = FileHandler.readFile(dir, AVWX_API_KEY_PATH);
        AVWX avwx = gson.fromJson(avwxKeyFile, AVWX.class);
        return avwx.getAvwxApiKey();
    }
}
