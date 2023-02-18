package com.marlan.shared.model;

import lombok.Data;

/**
 * GSON Deserialization Class
 */
@Data
public class DTO {
    private String metar;
    private String mission;
    private String icao;
    private String stationLongitude;
    private String stationLatitude;
    private String weatherType;
    private String currentGameTime;
    private String updatePhase;
}
