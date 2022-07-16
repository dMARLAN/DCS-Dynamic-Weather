package com.marlan.weatherupdate.utilities;

import lombok.NoArgsConstructor;

import static java.lang.System.out;

@NoArgsConstructor
public class ZuluLocalConverter {
    public int zuluToLocalConversion(String icao) {
        if (icao.equals("KLSV")) return -6; // TODO: Expand on this!
        out.println("ZuluLocalConverter: ICAO Unknown, time conversion set to 0");
        return 0;
    }
}
