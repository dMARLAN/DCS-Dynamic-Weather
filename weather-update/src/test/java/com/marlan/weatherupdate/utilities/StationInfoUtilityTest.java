package com.marlan.weatherupdate.utilities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StationInfoUtilityTest {

    @Test
    @DisplayName("Invalid latititude and longitude should return UTC")
    void invalidLatLongShouldReturnUTC() {
        assertEquals("UTC", StationInfoUtility.getZoneId(-1, -1));
    }

}