package com.marlan.weatherupdate.model.datafile;

import lombok.Data;

@Data
public class WeatherUpdateData {
    private String avwxApiKey;
    private String icao;
    private String mission;
    private String discordApiKey;
    private String metar;
    private String stationLongitude;
    private String stationLatitude;
    private String weatherType;
}
