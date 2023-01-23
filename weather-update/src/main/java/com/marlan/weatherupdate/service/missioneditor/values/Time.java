package com.marlan.weatherupdate.service.missioneditor.values;

import lombok.Data;

@Data
public class Time {
    private final float hour;
    private final int day;
    private final int month;

    public Time(float hour, int day, int month) {
        this.hour = hour;
        this.day = day;
        this.month = month;
    }

}
