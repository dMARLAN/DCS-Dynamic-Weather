package com.marlan.weatherupdate.model.metar.fields;

import lombok.Data;

/**
 * GSON Deserialization Class
 */
@Data
public class Time {
    private String repr; // var name from AVWX API; e.g.: "232221Z"
    private String dt; // var name from AVWX API; e.g.: "2022-09-23T22:21:00Z"
}
