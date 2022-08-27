package com.marlan.weatherupdate.utilities;

public class StationInfoUtility {
    private StationInfoUtility() {
    }

    public static String getZoneId(String country) {
        if (country.equals("US")) return "America/Los_Angeles"; // Nellis AFB
        if (country.equals("AE")) return "Asia/Dubai"; // Al Dhafra AFB
        if (country.equals("GU")) return "Pacific/Guam"; // Andersen AFB
        if (country.equals("GE")) return "Asia/Tbilisi"; // Batumi AFB
        if (country.equals("TR")) return "Asia/Damascus"; // Incirlik AFB
        if (country.equals("FK")) return "Atlantic/Stanley"; // Mount Pleasant AFB
        Logger.warning("Unknown Country: " + country + " - using default zoneId: UTC");
        return "UTC";
    }

}
