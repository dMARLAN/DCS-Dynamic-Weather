package com.marlan.weatherupdate.utilities;

import lombok.NoArgsConstructor;

import static java.lang.System.out;

@NoArgsConstructor
public class ZuluLocalConverter {
    public int zuluToLocalConversion(String icao) {
        if (icao.equals("KLSV")) return -7; // Nellis AFB
        if (icao.equals("OMAM")) return 4; // Al Dhafra AFB
        if (icao.equals("PGUA")) return -2; // Andersen AFB
        if (icao.equals("UGSB")) return 4; // Batumi AFB
        if (icao.equals("LTAG")) return 3; // Incirlik AFB
        if (icao.equals("EGYP")) return 3; // Mount Pleasant AFB
        out.println("ZuluLocalConverter: ICAO Unknown, time conversion set to 0");
        return 0;
    }
}
