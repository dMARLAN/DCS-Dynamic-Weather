package com.marlan.weatherupdate.model.datafile;

import lombok.Data;

@Data
public class WeatherUpdateData {
    private String avwxApiKey;
    private String icao;
    private String mission;
    private String weatherType;
    private String stationLongitude;
    private String stationLatitude;
}
