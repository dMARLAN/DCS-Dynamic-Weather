package com.marlan.weatherupdate.utilities;

import lombok.experimental.UtilityClass;

import static java.lang.System.out;

@UtilityClass
public class ZoneIdFromIcao {
    public static String getZoneId(String icao) {
        if (icao.equals("KLSV")) return "America/Los_Angeles"; // Nellis AFB
        if (icao.equals("OMAM")) return ""; // Al Dhafra AFB
        if (icao.equals("PGUA")) return ""; // Andersen AFB
        if (icao.equals("UGSB")) return ""; // Batumi AFB
        if (icao.equals("LTAG")) return ""; // Incirlik AFB
        if (icao.equals("EGYP")) return ""; // Mount Pleasant AFB
        out.println("ZuluLocalConverter: ICAO Unknown, time conversion set to 0");
        return "UTC";
    }
}
