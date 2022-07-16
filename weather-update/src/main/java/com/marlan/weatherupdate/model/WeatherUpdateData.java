package com.marlan.weatherupdate.model;

import lombok.Data;

@Data
public class WeatherUpdateData {
    private String avwxApiKey;
    private String icao;
    private String mission;
}
