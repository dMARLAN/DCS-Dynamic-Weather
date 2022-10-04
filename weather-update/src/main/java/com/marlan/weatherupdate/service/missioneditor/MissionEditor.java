package com.marlan.weatherupdate.service.missioneditor;

import com.marlan.weatherupdate.model.station.AVWXStation;
import com.marlan.weatherupdate.utilities.AltimeterUtility;
import com.marlan.weatherupdate.utilities.Log;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Handles replacing strings inside the mission file
 */
public class MissionEditor {
    private static final double KNOTS_TO_METERS = 0.51444444444;
    private static final double INHG_TO_MMHG = 25.4;
    private static final double TEMP_LAPSE_RATE_C = 1.98;
    private static final Random random = new Random();

    private final AVWXStation stationAVWX;
    private final MissionValues missionValues;

    public MissionEditor(AVWXStation stationAVWX, MissionValues missionValues) {
        this.stationAVWX = stationAVWX;
        this.missionValues = missionValues;
    }

    public String editMission(String mission) {
        double correctedQffInHg = AltimeterUtility.getCorrectedQff(missionValues.getStationQnh(), missionValues.getStationTempC(), stationAVWX);
        double qffMmHg = correctedQffInHg * INHG_TO_MMHG;

        // TODO Move this shit into MissionValues
        double windSpeedGround = getCorrectedGroundWindSpeed(missionValues.getWindSpeed(), stationAVWX.getElevationM()); // "Ground" Wind is 10m/33ft but also sets ~500m/1660ft
        double windSpeed2000 = getModifiedWindSpeed(2000, missionValues.getWindSpeed()); // "2000" Wind is 2000m/6600ft
        double windSpeed8000 = getModifiedWindSpeed(8000, missionValues.getWindSpeed()); // "8000" Wind is 8000m/26000ft

        double windDirectionGround = invertWindDirection(missionValues.getWindDirection()); // Wind Direction is backwards in DCS.
        double windDirection2000 = randomizeWindDirection(missionValues.getWindDirection());
        double windDirection8000 = randomizeWindDirection(windDirection2000);

        String cloudsPreset = buildCloudsPreset(selectCloudsPresetSuffix(missionValues.getMetar()));

        mission = replaceCloudsPreset(mission, cloudsPreset);
        mission = replaceWind8000(mission, windSpeed8000, windDirection8000);
        mission = replaceWind2000(mission, windSpeed2000, windDirection2000);
        mission = replaceWindGround(mission, windSpeedGround, windDirectionGround);
        mission = replaceHour(mission, missionValues.getHour());
        mission = replaceDay(mission, missionValues.getDay());
        mission = replaceMonth(mission, missionValues.getMonth());
        mission = replaceTemperature(mission, missionValues.getStationTempC());
        mission = replaceQnh(mission, qffMmHg, missionValues.getStationQnh());

        return mission;
    }

    @NotNull
    private String replaceQnh(String mission, double qffMmHg, double qnhInHg) {
        mission = mission.replaceAll("(\\[\"qnh\"].*)\n", "[\"qnh\"] = \\$qnh,\n".replace("$qnh", Double.toString(qffMmHg))); // DCS actually uses QFF not QNH!
        double qnhMmHg = qnhInHg * INHG_TO_MMHG;
        Log.info("QNH set to: " + qnhInHg + " inHg (" + qnhMmHg + " mmHg)");
        Log.info("QFF set to: " + qffMmHg / INHG_TO_MMHG + " inHg (" + qffMmHg + " mmHg)");
        return mission;
    }

    @NotNull
    private String replaceTemperature(String mission, double stationTempC) {
        mission = mission.replaceAll("(\\[\"temperature\"].*)\n", "[\"temperature\"] = \\$stationTempC,\n".replace("$stationTempC", Double.toString(stationTempC)));
        Log.info("Station Temperature set to: " + stationTempC + " C" + " / Sea Level Temperature set to: " + Math.round(stationTempC + TEMP_LAPSE_RATE_C * (stationAVWX.getElevationFt() / 1000)) + " C");
        return mission;
    }

    @NotNull
    private String replaceMonth(String mission, int month) {
        mission = mission.replaceAll("(\\[\"Month\"].*)\n", "[\"Month\"] = \\$month,\n".replace("$month", Integer.toString(month)));
        Log.info("Month set to: " + month);
        return mission;
    }

    @NotNull
    private String replaceDay(String mission, int day) {
        mission = mission.replaceAll("(\\[\"Day\"].*)\n", "[\"Day\"] = \\$day,\n".replace("$day", Integer.toString(day)));
        Log.info("Day set to: " + day);
        return mission;
    }

    @NotNull
    private String replaceHour(String mission, int hour) {
        mission = mission.replaceAll("(?<=\\[\"currentKey\"]\\s{1,5}=\\s{1,5}.{1,100}\n)(.*)", "    [\"start_time\"] = $startTime,".replace("$startTime", Integer.toString(hour * 3600)));
        Log.info("Start Time set to: " + hour * 3600 + "s (" + hour + "h)");
        return mission;
    }

    @NotNull
    private String replaceWindGround(String mission, double windSpeedGround, double windDirectionGround) {
        mission = mission.replaceAll("\\[\"atGround\"]\\s+=\\s+\\{([^}]*)",
                "[\"atGround\"] =\n            {\n                [\"speed\"] = $windGroundSpeed,\n                [\"dir\"] = $windGroundDir,\n            "
                        .replace("$windGroundSpeed", Double.toString(windSpeedGround))
                        .replace("$windGroundDir", Double.toString(windDirectionGround)));
        Log.info("Wind at Ground set to: " + Math.round(windSpeedGround) + " m/s (" + Math.round(windSpeedGround / KNOTS_TO_METERS) + " kts) " + Math.floor(invertWindDirection(windDirectionGround)) + "°");
        return mission;
    }

    @NotNull
    private String replaceWind2000(String mission, double windSpeed2000, double windDirection2000) {
        mission = mission.replaceAll("\\[\"at2000\"]\\s+=\\s+\\{([^}]*)",
                "[\"at2000\"] =\n            {\n                [\"speed\"] = $wind2000Speed,\n                [\"dir\"] = $wind2000Dir,\n            "
                        .replace("$wind2000Speed", Double.toString(windSpeed2000))
                        .replace("$wind2000Dir", Double.toString(windDirection2000)));
        Log.info("Wind at 2000 set to: " + Math.round(windSpeed2000) + " m/s (" + Math.round(windSpeed2000 / KNOTS_TO_METERS) + " kts) " + Math.floor(invertWindDirection(windDirection2000)) + "°");
        return mission;
    }

    @NotNull
    private String replaceWind8000(String mission, double windSpeed8000, double windDirection8000) {
        mission = mission.replaceAll("\\[\"at8000\"]\\s+=\\s+\\{([^}]*)",
                "[\"at8000\"] =\n            {\n                [\"speed\"] = $wind8000Speed,\n                [\"dir\"] = $wind8000Dir,\n            "
                        .replace("$wind8000Speed", Double.toString(windSpeed8000))
                        .replace("$wind8000Dir", Double.toString(windDirection8000)));
        Log.info("Wind at 8000 set to: " + Math.round(windSpeed8000) + " m/s (" + Math.round(windSpeed8000 / KNOTS_TO_METERS) + " kts) " + Math.floor(invertWindDirection(windDirection8000)) + "°");
        return mission;
    }

    private String replaceCloudsPreset(@NotNull String mission, String cloudsPreset) {
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
        Log.info("Clouds Preset: " + cloudsPreset);
        return mission;
    }

    private double getCorrectedGroundWindSpeed(double windSpeedKnots, double stationAltitude) {
        double windSpeedMultiplier;
        windSpeedMultiplier = Math.abs((0.5 / 500) * stationAltitude - 1);
        return windSpeedKnots * windSpeedMultiplier * KNOTS_TO_METERS;
    }

    private double getModifiedWindSpeed(double altitudeMeters, double windSpeedKnots) {
        double windSpeedMultiplier;
        double windSpeedAddition;
        if (altitudeMeters == 2000) {
            windSpeedAddition = Math.min(random.nextGaussian(10, 10), 30);
            windSpeedMultiplier = Math.min(random.nextGaussian(0.5, 0.5) + 1, 2.5);
            return Math.abs((windSpeedKnots * windSpeedMultiplier) + windSpeedAddition) * KNOTS_TO_METERS;
        } else if (altitudeMeters == 8000) {
            windSpeedAddition = Math.min(random.nextGaussian(40, 20), 60);
            windSpeedMultiplier = Math.min(random.nextGaussian(1, 1) + 1, 3);
            return Math.abs((windSpeedKnots * windSpeedMultiplier) + windSpeedAddition) * KNOTS_TO_METERS;
        } else {
            return 0.0;
        }
    }

    private double randomizeWindDirection(double windDirection) {
        double randomizedWindDirection = windDirection + random.nextGaussian(0, 60);
        if (randomizedWindDirection < 0) {
            randomizedWindDirection += 360;
        } else if (randomizedWindDirection > 360) {
            randomizedWindDirection -= 360;
        }
        return randomizedWindDirection;
    }

    @NotNull
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

    private int selectCloudsPresetSuffix(@NotNull String metar) {
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
