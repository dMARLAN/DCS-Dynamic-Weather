package com.marlan.weatherupdate.model.station;

import lombok.Data;

/**
 * GSON Deserialization Class
 */
@Data
public class AVWXStation {
    private double elevationFt;
    private double elevationM;
    private double longitude;
    private double latitude;
    private String country;
    private String icao;
}
