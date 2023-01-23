package com.marlan.shared.model;

import lombok.Data;

/**
 * GSON Deserialization Class
 */
@Data
public class Config {
    private String spreadsheetId;
    private String spreadsheetRange;
    private boolean outputToSheets;
    private boolean outputToDiscord;
    private String customSevenZipPath;
    private int timeOffset;
    private int currentTime;
    private int firstCyclicTimeInSecs;
    private int cyclicWindows;
    private int cyclicLength;
    private int preEventTime;
}
