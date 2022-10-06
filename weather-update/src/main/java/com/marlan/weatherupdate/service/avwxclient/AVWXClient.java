package com.marlan.weatherupdate.service.avwxclient;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatherupdate.model.dto.DTO;
import com.marlan.weatherupdate.model.metar.AVWXMetar;
import com.marlan.weatherupdate.model.secrets.AVWX;
import com.marlan.weatherupdate.utilities.FileHandler;
import com.marlan.weatherupdate.utilities.Log;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Gets Station & METAR data from <a href="https://avwx.rest/">AVWX API</a>
 */
public class AVWXClient {
    private static final Log log = Log.getInstance();
    private final String dir;
    HttpClient httpClient = HttpClient.newHttpClient();

    public AVWXClient(final String dir) {
        this.dir = dir;
    }

    public HttpResponse<String> getMetar(DTO dto) {

        String avwxKey = getAVWXApiKey();
        if (avwxKey.isEmpty() || dto.getStationLatitude().isEmpty() || dto.getStationLongitude().isEmpty()) {
            String errorMessage = "AVWX API Key, Station Latitude and Station Longitude are required";
            log.error(errorMessage);
        } else {
            try {
                HttpRequest getRequest = HttpRequest.newBuilder()
                        .uri(new URI("https://avwx.rest/api/metar/" + dto.getStationLatitude() + "," + dto.getStationLongitude() + "?onfail=nearest"))
                        .header("Authorization", avwxKey)
                        .build();
                return sendRequest(getRequest);

            } catch (URISyntaxException use) {
                log.error(use.getMessage());
            }
        }
        return null;
    }

    public HttpResponse<String> getStation(AVWXMetar weatherAVWX) {
        String avwxKey = getAVWXApiKey();
        if (avwxKey.isEmpty() || weatherAVWX.getStation().isEmpty()) {
            String errorMessage = "AVWX API Key and Station are required";
            log.error(errorMessage);
        } else {
            try {
                HttpRequest getRequest = HttpRequest.newBuilder()
                        .uri(new URI("https://avwx.rest/api/station/" + weatherAVWX.getStation()))
                        .header("Authorization", avwxKey)
                        .build();
                return sendRequest(getRequest);

            } catch (URISyntaxException use) {
                log.error(use.getMessage());
            }
        }
        return null;
    }

    private HttpResponse<String> sendRequest(HttpRequest getRequest) {
        try {
            return this.httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage());
        }
        return null;
    }

    private String getAVWXApiKey() {
        final String AVWX_API_KEY_PATH = "secrets\\avwx_api_key.json";
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        String avwxKeyFile = FileHandler.readFile(dir, AVWX_API_KEY_PATH);
        AVWX avwx = gson.fromJson(avwxKeyFile, AVWX.class);
        return avwx.getAvwxApiKey();
    }
}
