package com.marlan.weatherupdate.service;

import com.marlan.weatherupdate.model.metar.AVWXMetar;
import com.marlan.weatherupdate.model.metar.fields.Temperature;
import com.marlan.weatherupdate.model.metar.fields.WindDirection;
import com.marlan.weatherupdate.model.metar.fields.WindSpeed;
import com.marlan.weatherupdate.model.station.AVWXStation;
import com.marlan.weatherupdate.utilities.AltimeterHandler;
import com.marlan.weatherupdate.utilities.Constants;
import com.marlan.weatherupdate.utilities.StationInfo;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;
import java.util.Random;

import static java.lang.System.out;

@NoArgsConstructor
public class MissionHandler {

    public String editMission(String mission, AVWXMetar metarAVWX, AVWXStation stationAVWX) {
        java.time.ZonedDateTime zonedDateTime = java.time.ZonedDateTime.now(java.time.ZoneId.of(StationInfo.getZoneId(stationAVWX.getCountry())));

        double windSpeed = metarAVWX.getWindSpeed().flatMap(WindSpeed::getValue).orElse(0.0);
        double windDir = metarAVWX.getWindDirection().flatMap(WindDirection::getValue).orElse(0.0);
        double windSpeedGround = getModifiedWindSpeed(500, windSpeed, stationAVWX); // "Ground" Wind is 10m/33ft but also sets ~500m/1660ft
        double windSpeed2000 = getModifiedWindSpeed(2000, windSpeed, stationAVWX); // "2000" Wind is 2000m/6600ft
        double windSpeed8000 = getModifiedWindSpeed(8000, windSpeed, stationAVWX); // "8000" Wind is 8000m/26000ft

        double stationTempC = metarAVWX.getTemperature().flatMap(Temperature::getValue).orElse(Constants.ISA_TEMP_C);
        double seaLevelTempC = stationTempC + Constants.TEMP_LAPSE_RATE_C * (stationAVWX.getElevationFt() / 1000);
        double correctedQffInHg = AltimeterHandler.getCorrectedQff(metarAVWX.getAltimeter().getValue(), stationTempC, stationAVWX);
        double qffMmHg = correctedQffInHg * Constants.INHG_TO_MMHG;

        double windDirectionGround = invertWindDirection(windDir); // Wind Direction is backwards in DCS.
        double windDirection2000 = randomizeWindDirection(windDir);
        double windDirection8000 = randomizeWindDirection(windDirection2000);

        String cloudsPreset = buildCloudsPreset(selectCloudsPresetSuffix(metarAVWX.getSanitized()));

        if (!mission.contains("[\"preset\"]")) {
            mission = mission.replaceAll(
                            "(\\[\"iprecptns\"].*)\n",
                            "[\"iprecptns\"] = 0,\n            [\"preset\"] = \"\\$cloudsPreset\",\n")
                    .replace("$cloudsPreset", cloudsPreset);
        } else {
            mission = mission.replaceAll(
                    "(\\[\"preset\"].*)\n",
                    "[\"preset\"] = \"\\$cloudsPreset\",\n"
                            .replace("$cloudsPreset", cloudsPreset));
        }
        out.println("INFO: Clouds Preset set to: " + cloudsPreset);

        mission = mission.replaceAll("\\[\"at8000\"]\\s+=\\s+\\{([^}]*)",
                "[\"at8000\"] =\n            {\n                [\"speed\"] = $wind8000Speed,\n                [\"dir\"] = $wind8000Dir,\n            "
                        .replace("$wind8000Speed", Double.toString(windSpeed8000))
                        .replace("$wind8000Dir", Double.toString(windDirection8000)));
        out.println("INFO: Wind at 8000 set to: " + Math.round(windSpeed8000) + " m/s (" + Math.round(windSpeed8000 * Constants.METERS_TO_KNOTS) + " kts)" + " at " + Math.floor(invertWindDirection(windDirection8000)) + "°");

        mission = mission.replaceAll("\\[\"at2000\"]\\s+=\\s+\\{([^}]*)",
                "[\"at2000\"] =\n            {\n                [\"speed\"] = $wind2000Speed,\n                [\"dir\"] = $wind2000Dir,\n            "
                        .replace("$wind2000Speed", Double.toString(windSpeed2000))
                        .replace("$wind2000Dir", Double.toString(windDirection2000)));
        out.println("INFO: Wind at 2000 set to: " + Math.round(windSpeed2000) + " m/s (" + Math.round(windSpeed2000 * Constants.METERS_TO_KNOTS) + " kts)" + " at " + Math.floor(invertWindDirection(windDirection2000)) + "°");

        mission = mission.replaceAll("\\[\"atGround\"]\\s+=\\s+\\{([^}]*)",
                "[\"atGround\"] =\n            {\n                [\"speed\"] = $windGroundSpeed,\n                [\"dir\"] = $windGroundDir,\n            "
                        .replace("$windGroundSpeed", Double.toString(windSpeedGround))
                        .replace("$windGroundDir", Double.toString(windDirectionGround)));
        out.println("INFO: Wind at Ground set to: " + Math.round(windSpeedGround) + " m/s (" + Math.round(windSpeedGround * Constants.METERS_TO_KNOTS) + " kts)" + " at " + Math.floor(invertWindDirection(windDirectionGround)) + "°");

        mission = mission.replaceAll("(?<=\\[\"currentKey\"]\\s{1,5}=\\s{1,5}.{1,100}\n)(.*)", "    [\"start_time\"] = $startTime,".replace("$startTime", Integer.toString(zonedDateTime.getHour() * 3600)));
        out.println("INFO: Start Time set to: " + zonedDateTime.getHour() * 3600 + "s (" + zonedDateTime.getHour() + "h)");

        mission = mission.replaceAll("(\\[\"Day\"].*)\n", "[\"Day\"] = \\$day,\n".replace("$day", Integer.toString(Integer.parseInt(metarAVWX.getTime().getRepr().substring(0, 2)))));
        out.println("INFO: Day set to: " + zonedDateTime.getDayOfMonth());

        mission = mission.replaceAll("(\\[\"Month\"].*)\n", "[\"Month\"] = \\$month,\n".replace("$month", Integer.toString(Integer.parseInt(metarAVWX.getTime().getDt().substring(5, 7)))));
        out.println("INFO: Month set to: " + zonedDateTime.getMonthValue());

        mission = mission.replaceAll("(\\[\"temperature\"].*)\n", "[\"temperature\"] = \\$stationTempC,\n".replace("$stationTempC", Double.toString(stationTempC)));
        out.println("INFO: Station Temperature set to: " + stationTempC + " C" + "/ Sea Level Temperature set to: " + Math.round(seaLevelTempC) + " C");

        mission = mission.replaceAll("(\\[\"qnh\"].*)\n", "[\"qnh\"] = \\$qnh,\n".replace("$qnh", Double.toString(qffMmHg))); // DCS actually uses QFF not QNH!
        out.println("INFO: QFF set to: " + qffMmHg + " mmHg (" + qffMmHg * Constants.MMHG_TO_INHG + " inHg)");

        return mission;
    }

    private double getModifiedWindSpeed(double altitudeMeters, double windSpeedKnots, AVWXStation stationAVWX) {
        Random random = new SecureRandom();
        double windSpeedMultiplier;
        double windSpeedAddition;
        if (altitudeMeters == 500) {
            double stationAltitude = stationAVWX.getElevationM();
            windSpeedMultiplier = Math.abs((0.5 / altitudeMeters) * stationAltitude - 1);
            return windSpeedKnots * windSpeedMultiplier * Constants.KNOTS_TO_METERS;
        } else if (altitudeMeters == 2000) {
            windSpeedAddition = Math.min(random.nextGaussian(10, 10), 30);
            windSpeedMultiplier = Math.min(random.nextGaussian(0.5, 0.5) + 1, 2.5);
            return Math.abs((windSpeedKnots * windSpeedMultiplier) + windSpeedAddition) * Constants.KNOTS_TO_METERS;
        } else if (altitudeMeters == 8000) {
            windSpeedAddition = Math.min(random.nextGaussian(40, 20), 60);
            windSpeedMultiplier = Math.min(random.nextGaussian(1, 1) + 1, 3);
            return Math.abs((windSpeedKnots * windSpeedMultiplier) + windSpeedAddition) * Constants.KNOTS_TO_METERS;
        } else {
            return 0.0;
        }
    }

    private double randomizeWindDirection(double windDirection) {
        Random random = new SecureRandom();
        double randomizedWindDirection = windDirection + random.nextGaussian(0, 60);
        if (randomizedWindDirection < 0) {
            randomizedWindDirection += 360;
        } else if (randomizedWindDirection > 360) {
            randomizedWindDirection -= 360;
        }
        return randomizedWindDirection;
    }

    private String buildCloudsPreset(int cloudsPresetSuffix) {
        if (cloudsPresetSuffix == 0) {
            return "nil";
        } else {
            if (cloudsPresetSuffix > 27) {
                return "RainyPreset" + cloudsPresetSuffix % 27; // Converts Presets28-30 to RainyPreset1-3
            } else {
                return "Preset" + cloudsPresetSuffix;
            }
        }
    }

    private int selectCloudsPresetSuffix(String metar) {
        Random random = new SecureRandom();
        if (metar.contains("SKC") || metar.contains("NCD")) return 0;
        if (metar.contains("CLR") || metar.contains("NSC") || metar.contains("CAVOK")) return random.nextInt(3);
        if (metar.contains("OVC")) return random.nextInt(10) + 21;
        if (metar.contains("BKN")) return random.nextInt(8) + 13;
        if (metar.contains("SCT")) return random.nextInt(10) + 3;
        if (metar.contains("FEW")) return random.nextInt(5) + 1;
        return 0;
    }

    private double invertWindDirection(double windDirection) {
        if (windDirection >= 0 && windDirection <= 180) {
            return windDirection + 180;
        } else {
            return windDirection - 180;
        }
    }
}
