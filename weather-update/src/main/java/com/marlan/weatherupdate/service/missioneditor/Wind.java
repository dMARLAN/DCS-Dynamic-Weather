package com.marlan.weatherupdate.service.missioneditor;

import lombok.Data;

@Data
public class Wind {
    private final double speed;
    private final double direction;

    public Wind(double speed, double direction){
        this.speed = speed;
        this.direction = direction;
    }

}
