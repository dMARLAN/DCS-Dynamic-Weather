package com.marlan.weatheroutput.model;

import lombok.Data;

@Data
public class DTO {
    private String discordApiKey;
    private String metar;
    private String spreadsheetId;
    private String spreadsheetRange;
}
