package com.marlan.weatherupdate.utilities;

import com.marlan.shared.utilities.Log;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StationInfoUtilityTest {
    static Log log = Log.getInstance();

    @BeforeAll
    static void setup() {
        log.disable();
    }

    @Test
    @DisplayName("Invalid latitude and longitude should return UTC")
    void invalidLatLongShouldReturnUTC() {
        assertEquals("UTC", StationInfoUtility.getZoneId(-9999, -9999));
    }

    @Test
    @DisplayName("Valid latitude and longitude should return ZoneId")
    void validLatLongShouldReturnZoneId() {
        assertAll(
                () -> assertEquals("America/New_York", StationInfoUtility.getZoneId(40.7128, -74.0060)),
                () -> assertEquals("America/Los_Angeles", StationInfoUtility.getZoneId(34.0522, -118.2437)),
                () -> assertEquals("America/Chicago", StationInfoUtility.getZoneId(41.8781, -87.6298)),
                () -> assertEquals("America/Denver", StationInfoUtility.getZoneId(39.7392, -104.9903))
        );
    }

}