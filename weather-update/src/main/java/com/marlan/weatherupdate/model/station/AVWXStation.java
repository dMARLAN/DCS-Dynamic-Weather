package com.marlan.weatherupdate.model.station;

import lombok.Data;

@Data
public class AVWXStation {
    private double elevationFt;
    private double elevationM;
    private double latitude;
    private double longitude;
    private String country;
}
