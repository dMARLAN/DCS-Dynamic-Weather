package com.marlan.weatherupdate.utilities;

import com.marlan.weatherupdate.model.station.AVWXStation;

/**
 * DCS does not actually use QNH but instead uses QFF when set inside the mission file.
 * This will convert QNH to QFF so that it can be used in DCS.
 */
public class AltimeterUtility {
    static Log log = Log.getInstance();
    private static final double FEET_TO_METERS = 0.3048;
    private static final double PA_TO_INHG = 0.000295299830714;
    private static final double ISA_PRESSURE_MB = 1013.25;

    private AltimeterUtility() {
    }

    public static double getCorrectedQff(double stationQnhInHg, double stationTempC, AVWXStation station) {
        double stationElevFeet = station.getElevationFt();
        double stationLatitude = station.getLatitude();
        double pressureAltitude = getPressureAltitude(stationQnhInHg) + stationElevFeet;
        log.info("Pressure Altitude: " + pressureAltitude + " ft");
        double stationQfeInHg = getQfe(pressureAltitude);
        log.info("Station QFE: " + stationQfeInHg + " inHg");
        return getQff(stationQfeInHg, stationTempC, stationLatitude, stationElevFeet + 50);
    }

    private static double getPressureAltitude(Double stationQnhInHg) {
        // https://en.wikipedia.org/wiki/Pressure_altitude
        final double INHG_TO_MB = 33.864;
        double stationQnhMb = stationQnhInHg * INHG_TO_MB;
        return 145366.45 * (1 - Math.pow((stationQnhMb / ISA_PRESSURE_MB), 0.190284));
    }

    private static double getQfe(double pressureAltitude) {
        // https://en.wikipedia.org/wiki/Atmospheric_pressure
        double h = pressureAltitude * FEET_TO_METERS;
        double p0 = 101325; // Sea Level Standard Atmospheric Pressure (Pa)
        double t0 = 288.16; // Sea Level Standard Temperature (K)
        double g = 9.80665; // Acceleration of Gravity (m/s^2)
        double m = 0.02896968; // Molar Mass of Earth's Air (kg/mol)
        double r0 = 8.314462618; // Universal Gas Constant (J/mol/K)
        double cp = 1004.68506; // Specific Heat Capacity of Air (J/kg/K)

        return p0 * Math.pow(1 - ((g * h) / (cp * t0)), (cp * m) / (r0)) * PA_TO_INHG;
    }

    private static double getQff(double qfeInHg, double temperatureInCelsius, double stationLatitude, double stationElevFeet) {
        // https://www.metpod.co.uk/calculators/pressure/ -- Swedish Meteorological and Hydrological Institute Method
        double h = stationElevFeet * FEET_TO_METERS;
        double t1 = getWinterInversionT1(temperatureInCelsius);
        return qfeInHg * Math.pow(Math.E, ((h * 0.034163 * (1 - (0.0026373 * Math.cos(stationLatitude)))) / t1));
    }

    private static double getWinterInversionT1(double temperatureInCelsius) {
        // https://www.metpod.co.uk/calculators/pressure/ -- Swedish Meteorological and Hydrological Institute Method
        if (temperatureInCelsius <= -7) {
            return 0.5 * temperatureInCelsius + 275;
        } else if (temperatureInCelsius <= 2) {
            return 0.535 * temperatureInCelsius + 275.6;
        } else {
            return 1.07 * temperatureInCelsius + 274.5;
        }
    }
}
