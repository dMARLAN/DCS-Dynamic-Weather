package com.marlan.weatherupdate.model.config;

import lombok.Data;

/**
 * GSON Deserialization Class
 */
@Data
public class Config {
    private String customSevenZipPath;
    private int timeOffset;
}
