package com.marlan.weatherupdate.utilities;

import com.marlan.weatherupdate.model.station.AVWXStation;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class AltimeterUtilityTest {
    static double qnhDelta = 0.02;

    @Test
    @DisplayName("QFF should equal QNH at sea level")
    void qnhShouldEqualQffAtISA() {
        double stationQnhInHg = 29.92; // Standard ISA Pressure
        double stationTempC = 15; // Standard ISA Temperature
        AVWXStation station = new AVWXStation();
        station.setElevationFt(0); // Sea Level
        station.setLatitude(0); // Irrelevant for this test, but required.
        double qff = AltimeterUtility.getCorrectedQff(stationQnhInHg, stationTempC, station);
        assertEquals(29.92, qff, qnhDelta);
    }

    @Test
    @DisplayName("Nellis AFB: 30.05 InHg QNH & 35.0C == 29.94 InHg QFF")
    void nellisAFBTestOne() {
        double stationQnhInHg = 30.05;
        double stationTempC = 35;
        AVWXStation station = new AVWXStation();
        station.setElevationFt(1843); // Nellis AFB Elevation
        station.setLatitude(36.2355); // Nellis AFB Latitude
        double qff = AltimeterUtility.getCorrectedQff(stationQnhInHg, stationTempC, station);
        qff = Math.round(qff * 100.0) / 100.0;
        assertEquals(29.94, qff, qnhDelta);
    }

    @Test
    @DisplayName("Nellis AFB: 29.85 InHg QNH & 0.0C == 29.98 InHg QFF")
    void nellisAFBTestTwo() {
        double stationQnhInHg = 29.85;
        double stationTempC = 0;
        AVWXStation station = new AVWXStation();
        station.setElevationFt(1843); // Nellis AFB Elevation
        station.setLatitude(36.2355); // Nellis AFB Latitude
        double qff = AltimeterUtility.getCorrectedQff(stationQnhInHg, stationTempC, station);
        qff = Math.round(qff * 100.0) / 100.0;
        assertEquals(29.98, qff, qnhDelta);
    }

    @Test
    @DisplayName("Kutaisi AFB: 29.92 InHg QNH & -10.0C == 29.94 InHg QFF")
    void kutaisiAFBTestOne() {
        double stationQnhInHg = 29.92;
        double stationTempC = -10;
        AVWXStation station = new AVWXStation();
        station.setElevationFt(148); // Kutaisi AFB Elevation
        station.setLatitude(42.17765); // Kutaisi AFB Latitude
        double qff = AltimeterUtility.getCorrectedQff(stationQnhInHg, stationTempC, station);
        qff = Math.round(qff * 100.0) / 100.0;
        assertEquals(29.94, qff, qnhDelta);

    }

    @Test
    @DisplayName("Kutaisi AFB: 30.50 InHg QNH & -10.0C == 30.53 InHg QFF")
    void kutaisiAFBTestTwo() {
        double stationQnhInHg = 30.50;
        double stationTempC = -10;
        AVWXStation station = new AVWXStation();
        station.setElevationFt(148); // Kutaisi AFB Elevation
        station.setLatitude(42.17765); // Kutaisi AFB Latitude
        double qff = AltimeterUtility.getCorrectedQff(stationQnhInHg, stationTempC, station);
        qff = Math.round(qff * 100.0) / 100.0;
        assertEquals(30.53, qff, qnhDelta);
    }

    @Test
    @DisplayName("Beslan AFB: 29.75 InHg QNH & 10.0C == 29.80 InHg QFF")
    void beslanAFBTestOne() {
        double stationQnhInHg = 29.75;
        double stationTempC = 10;
        AVWXStation station = new AVWXStation();
        station.setElevationFt(1722); // Kutaisi AFB Elevation
        station.setLatitude(43.205683); // Kutaisi AFB Latitude
        double qff = AltimeterUtility.getCorrectedQff(stationQnhInHg, stationTempC, station);
        qff = Math.round(qff * 100.0) / 100.0;
        assertEquals(29.80, qff, qnhDelta);
    }

    @Test
    @DisplayName("Mineralnye Vody AFB: 30.05 InHg QNH & 0.0C == 30.53 InHg QFF")
    void mineralnyeVodyAFBTestOne() {
        double stationQnhInHg = 30.05;
        double stationTempC = 0;
        AVWXStation station = new AVWXStation();
        station.setElevationFt(1050); // Kutaisi AFB Elevation
        station.setLatitude(44.227816); // Kutaisi AFB Latitude
        double qff = AltimeterUtility.getCorrectedQff(stationQnhInHg, stationTempC, station);
        qff = Math.round(qff * 100.0) / 100.0;
        System.out.println(qff);
        assertEquals(30.09, qff, qnhDelta);
    }

}