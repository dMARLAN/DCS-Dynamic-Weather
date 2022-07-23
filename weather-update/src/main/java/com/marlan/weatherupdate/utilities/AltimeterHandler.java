package com.marlan.weatherupdate.utilities;

import com.marlan.weatherupdate.model.station.AVWXStation;

public class AltimeterHandler {

    private AltimeterHandler() {}

    public static double getCorrectedQff(double stationQnhInHg, double stationTempC, AVWXStation station) {
        double stationElevFeet = station.getElevationFt();
        double stationLatitude = station.getLatitude();
        double seaLevelTempC = stationTempC + Constants.TEMP_LAPSE_RATE_C * (stationElevFeet / 1000);

        double pressureAltitude = stationElevFeet - getPressureAltitude(stationQnhInHg);
        double correctedQfe = qnhToQfe(stationQnhInHg, pressureAltitude);

        return qfeToQff(correctedQfe, seaLevelTempC, stationLatitude, stationElevFeet);
    }

    private static double getPressureAltitude(Double stationQnhInHg) {
        double stationQnhMb = stationQnhInHg * Constants.INHG_TO_MB;
        return 145366.45 * (1 - Math.pow((stationQnhMb / Constants.ISA_PRESSURE_MB), 0.190284));
    }

    private static double qfeToQff(double qfeInHg, double temperatureInCelsius, double stationLatitude, double stationElevFeet) {
        // https://www.metpod.co.uk/calculators/pressure/ -- Swedish Meteorological and Hydrological Institute Method
        double h = stationElevFeet * Constants.FEET_TO_METERS;
        double t1 = getWinterInversionT1(temperatureInCelsius);
        double inner = 1 - (0.0026373 * Math.cos(stationLatitude));
        return qfeInHg * Math.pow(Math.E, ((h * 0.034163 * inner) / t1));
    }

    private static double getWinterInversionT1(double temperatureInCelsius) {
        if (temperatureInCelsius <= -7) {
            return 0.5 * temperatureInCelsius + 275;
        } else if (temperatureInCelsius <= 2) {
            return 0.535 * temperatureInCelsius + 275.6;
        } else {
            return 1.07 * temperatureInCelsius + 274.5;
        }
    }

    private static double qnhToQfe(double qnhInHg, double stationElevFeet) {
        double qnhMb = qnhInHg * Constants.INHG_TO_MB;
        double qfeMb = qnhMb - stationElevFeet / 27.4;
        return qfeMb * Constants.MB_TO_INHG;
    }

}
