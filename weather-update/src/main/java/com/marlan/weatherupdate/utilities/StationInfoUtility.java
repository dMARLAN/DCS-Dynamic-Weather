package com.marlan.weatherupdate.utilities;

import us.dustinj.timezonemap.TimeZoneMap;

public class StationInfoUtility {
    private StationInfoUtility() {
    }

    public static String getZoneId(String country, double latitude, double longitude) {
        TimeZoneMap map = TimeZoneMap.forRegion(latitude - 1, longitude - 1, latitude + 1, longitude + 1);
        String zoneId = map.getOverlappingTimeZone(latitude, longitude).getZoneId();
        Logger.info("ZoneId for " + country + " is " + zoneId);
        if (!zoneId.isEmpty()) {
            return zoneId;
        } else {
            Logger.warning("Unknown Country: " + country + " - using default zoneId: UTC");
            return "UTC";
        }
    }
}
