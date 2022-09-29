package com.marlan.weatherupdate.utilities;

import us.dustinj.timezonemap.TimeZoneMap;

import java.util.Objects;

/**
 * Returns Time Zone Id based on latitude & longitude using TimeZoneMap otherwise returns UTC if TimeZoneMap fails.
 */
public class StationInfoUtility {
    private StationInfoUtility() {
    }

    /**
     * Uses TimeZoneMap and returns Java ZoneId based on latitude/longitude for the station within 1 degree of latitude or longititude.
     * @param latitude Latitude for station
     * @param longitude Longitude for station
     * @return Java ZoneId or UTC if null
     */
    public static String getZoneId(double latitude, double longitude) {
        TimeZoneMap map = TimeZoneMap.forRegion(latitude - 1, longitude - 1, latitude + 1, longitude + 1);
        try {
            String zoneId = Objects.requireNonNull(map.getOverlappingTimeZone(latitude, longitude)).getZoneId();
            Log.info("ZoneId: " + zoneId);
            return zoneId;
        } catch (NullPointerException npe) {
            Log.warning("Unable to get ZoneId, using UTC");
            return "UTC";
        }
    }
}
