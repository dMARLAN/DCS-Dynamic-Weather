package com.marlan.weatherupdate.model.metar;

import com.marlan.weatherupdate.model.metar.fields.*;
import lombok.Data;

import java.util.Optional;

@Data
public class AVWXMetar {
    private Altimeter altimeter;
    private WindDirection windDirection;
    private WindSpeed windSpeed;
    private Temperature temperature;
    private Time time;
    private String sanitized;
    private String station;
    private Visibility visibility;
    private Meta meta;
    private Units units;

    public Optional<Temperature> getTemperature() {
        return Optional.ofNullable(this.temperature);
    }

    public Optional<WindSpeed> getWindSpeed() {
        return Optional.ofNullable(this.windSpeed);
    }

    public Optional<WindDirection> getWindDirection() {
        return Optional.ofNullable(this.windDirection);
    }
}
