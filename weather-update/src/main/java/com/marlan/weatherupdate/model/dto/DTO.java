package com.marlan.weatherupdate.model.dto;

import lombok.Data;

/**
 * GSON Deserialization Class
 */
@Data
public class DTO {
    private String mission;
    private String icao;
    private String metar;
    private String stationLongitude;
    private String stationLatitude;
    private String weatherType;
}
