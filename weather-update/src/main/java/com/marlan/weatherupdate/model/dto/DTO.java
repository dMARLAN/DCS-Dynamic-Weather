package com.marlan.weatherupdate.model.dto;

import lombok.Data;

@Data
public class DTO {
    private String avwxApiKey;
    private String discordApiKey;
    private String spreadsheetId;
    private String spreadsheetRange;
    private String mission;
    private String icao;
    private String metar;
    private String stationLongitude;
    private String stationLatitude;
    private String weatherType;
}
