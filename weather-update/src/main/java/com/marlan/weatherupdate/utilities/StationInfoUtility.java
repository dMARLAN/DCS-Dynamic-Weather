package com.marlan.weatherupdate.utilities;

import us.dustinj.timezonemap.TimeZoneMap;

/**
 * Returns Time Zone Id based on latitude & longitude using TimeZoneMap otherwise returns UTC if TimeZoneMap fails.
 */
public class StationInfoUtility {
    private StationInfoUtility() {
    }

    public static String getZoneId(double latitude, double longitude) {
        TimeZoneMap map = TimeZoneMap.forRegion(latitude - 1, longitude - 1, latitude + 1, longitude + 1);
        String zoneId = map.getOverlappingTimeZone(latitude, longitude).getZoneId();
        if (!zoneId.isEmpty()) {
            Log.info("ZoneId: " + zoneId);
            return zoneId;
        } else {
            Log.warning("Unable to get ZoneId, using default ZoneId: UTC");
            return "UTC";
        }
    }
}
