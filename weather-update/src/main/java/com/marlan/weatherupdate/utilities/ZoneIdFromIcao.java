package com.marlan.weatherupdate.utilities;

import lombok.experimental.UtilityClass;

import static java.lang.System.out;

@UtilityClass
public class ZoneIdFromIcao {
    public static String getZoneId(String icao) {
        if (icao.equals("KLSV")) return "America/Los_Angeles"; // Nellis AFB
        if (icao.equals("OMAM")) return "Asia/Dubai"; // Al Dhafra AFB
        if (icao.equals("PGUA")) return "Pacific/Guam"; // Andersen AFB
        if (icao.equals("UGSB")) return "Asia/Tbilisi"; // Batumi AFB
        if (icao.equals("LTAG")) return "Asia/Damascus"; // Incirlik AFB
        if (icao.equals("EGYP")) return "Atlantic/Stanley"; // Mount Pleasant AFB
        out.println("ICAO Unknown, time conversion set to UTC");
        return "UTC";
    }
}
