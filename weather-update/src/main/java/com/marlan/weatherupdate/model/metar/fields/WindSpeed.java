package com.marlan.weatherupdate.model.metar.fields;

import lombok.Data;

import java.util.Optional;

/**
 * GSON Deserialization Class
 */
@Data
public class WindSpeed {
    private Double value;

    public Optional<Double> getValue() {
        return Optional.ofNullable(this.value);
    }
}