package com.marlan.weatherupdate.service.missioneditor;

import lombok.Data;

@Data
public class Time {
    private final int hour;
    private final int day;
    private final int month;

    public Time(int hour, int day, int month){
        this.hour = hour;
        this.day = day;
        this.month = month;
    }

}
