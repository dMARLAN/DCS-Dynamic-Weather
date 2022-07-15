package com.marlan.weatherupdate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WeatherUpdateData {
    private String avwxApiKey;
    private String icao;
    private String mission;
}
