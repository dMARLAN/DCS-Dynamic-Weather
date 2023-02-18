package com.marlan.weatherupdate.service.missioneditor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.marlan.shared.utilities.Log;
import com.marlan.weatherupdate.model.station.AVWXStation;
import com.marlan.weatherupdate.utilities.AltimeterUtility;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Handles replacing strings inside the mission file
 */
public class MissionEditor {
    private static final Log log = Log.getInstance();
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

    public void editMission(JsonNode mission) {
        double correctedQffInHg = AltimeterUtility.getCorrectedQff(missionValues.getStation().getQnh(), missionValues.getStation().getTempC(), stationAVWX);
        double qffMmHg = correctedQffInHg * INHG_TO_MMHG;

        double windSpeedGround = getCorrectedGroundWindSpeed(missionValues.getWind().getSpeed(), stationAVWX.getElevationM()); // "Ground" Wind is 10m/33ft but also sets ~500m/1660ft
        double windSpeed2000 = getModifiedWindSpeed(2000, missionValues.getWind().getSpeed()); // "2000" Wind is 2000m/6600ft
        double windSpeed8000 = getModifiedWindSpeed(8000, missionValues.getWind().getSpeed()); // "8000" Wind is 8000m/26000ft

        double windDirectionGround = invertWindDirection(missionValues.getWind().getDirection()); // Wind Direction is backwards in DCS.
        double windDirection2000 = randomizeWindDirection(missionValues.getWind().getDirection());
        double windDirection8000 = randomizeWindDirection(windDirection2000);

        String cloudsPreset = buildCloudsPreset(selectCloudsPresetSuffix(missionValues.getStation().getMetar()));

        replaceCloudsPreset(mission, cloudsPreset);
        replaceWind8000(mission, windSpeed8000, windDirection8000);
        replaceWind2000(mission, windSpeed2000, windDirection2000);
        replaceWindGround(mission, windSpeedGround, windDirectionGround);
        replaceHour(mission, missionValues.getTime().getHour());
        replaceDay(mission, missionValues.getTime().getDay());
        replaceMonth(mission, missionValues.getTime().getMonth());
        replaceTemperature(mission, missionValues.getStation().getTempC());
        replaceQnh(mission, qffMmHg, missionValues.getStation().getQnh());
    }

    private void replaceQnh(@NotNull JsonNode mission, double qffMmHg, double qnhInHg) {
        ((ObjectNode) mission.get("weather")).set("qnh", new DoubleNode(qffMmHg)); // DCS actually uses QFF not QNH!
        double qnhMmHg = qnhInHg * INHG_TO_MMHG;
        log.info("QNH set to: " + qnhInHg + " inHg (" + qnhMmHg + " mmHg)");
        log.info("QFF set to: " + qffMmHg / INHG_TO_MMHG + " inHg (" + qffMmHg + " mmHg)");
    }

    private void replaceTemperature(@NotNull JsonNode mission, double stationTempC) {
        ((ObjectNode) mission.get("weather").get("season")).set("temperature", new DoubleNode(stationTempC));
        log.info("Station Temperature set to: "
                 + stationTempC + " C"
                 + " / Sea Level Temperature set to: "
                 + Math.round(stationTempC + TEMP_LAPSE_RATE_C * (stationAVWX.getElevationFt() / 1000)) + " C");
    }

    private void replaceMonth(@NotNull JsonNode mission, int month) {
        ((ObjectNode) mission.get("date")).set("Month", new IntNode(month));
        log.info("Month set to: " + month);
    }

    private void replaceDay(@NotNull JsonNode mission, int day) {
        ((ObjectNode) mission.get("date")).set("Day", new IntNode(day));
        log.info("Day set to: " + day);
    }

    private void replaceHour(@NotNull JsonNode mission, float hour) {
        ((ObjectNode) mission).set("start_time", new FloatNode(hour * 3600));
        log.info("Start Time set to: " + hour * 3600 + "s (" + hour + "h)");
    }

    private void replaceWindGround(@NotNull JsonNode mission, double windSpeedGround, double windDirectionGround) {
        ((ObjectNode) mission.get("weather").get("wind").get("atGround")).set("speed", new DoubleNode(windSpeedGround));
        ((ObjectNode) mission.get("weather").get("wind").get("atGround")).set("dir", new DoubleNode(windDirectionGround));
        log.info("Wind at Ground set to: " + Math.round(windSpeedGround) + " m/s ("
                 + Math.round(windSpeedGround / KNOTS_TO_METERS) + " kts) "
                 + Math.floor(invertWindDirection(windDirectionGround)) + "°");
    }

    private void replaceWind2000(@NotNull JsonNode mission, double windSpeed2000, double windDirection2000) {
        ((ObjectNode) mission.get("weather").get("wind").get("at2000")).set("speed", new DoubleNode(windSpeed2000));
        ((ObjectNode) mission.get("weather").get("wind").get("at2000")).set("dir", new DoubleNode(windDirection2000));
        log.info("Wind at 2000 set to: " + Math.round(windSpeed2000) + " m/s (" + Math.round(windSpeed2000 / KNOTS_TO_METERS) + " kts) " + Math.floor(invertWindDirection(windDirection2000)) + "°");
    }

    private void replaceWind8000(@NotNull JsonNode mission, double windSpeed8000, double windDirection8000) {
        ((ObjectNode) mission.get("weather").get("wind").get("at8000")).set("speed", new DoubleNode(windSpeed8000));
        ((ObjectNode) mission.get("weather").get("wind").get("at8000")).set("dir", new DoubleNode(windDirection8000));
        log.info("Wind at 8000 set to: " + Math.round(windSpeed8000) + " m/s (" + Math.round(windSpeed8000 / KNOTS_TO_METERS) + " kts) " + Math.floor(invertWindDirection(windDirection8000)) + "°");
    }

    private void replaceCloudsPreset(@NotNull JsonNode mission, String cloudsPreset) {
        if (mission.get("weather").get("clouds").has("preset")) {
            ((ObjectNode) mission.get("weather").get("clouds"))
                    .set("preset", new TextNode(cloudsPreset));
        }
        log.info("Clouds Preset: " + cloudsPreset);
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
