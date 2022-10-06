package com.marlan.weatherupdate.service.missioneditor.values;

import lombok.Data;

@Data
public class Station {
    private final String metar;
    private final double tempC;
    private final double qnh;

    public Station(String metar, double tempC, double qnh) {
        this.metar = metar;
        this.tempC = tempC;
        this.qnh = qnh;
    }

}
