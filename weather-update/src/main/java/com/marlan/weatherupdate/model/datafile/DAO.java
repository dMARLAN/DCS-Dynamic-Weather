package com.marlan.weatherupdate.model.datafile;

import lombok.Data;

@Data
public class DAO {
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
