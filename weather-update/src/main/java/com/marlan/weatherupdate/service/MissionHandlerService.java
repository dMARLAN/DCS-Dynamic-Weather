package com.marlan.weatherupdate.service;

import com.marlan.weatherupdate.model.dto.DTO;
import com.marlan.weatherupdate.model.metar.AVWXMetar;
import com.marlan.weatherupdate.model.metar.fields.Temperature;
import com.marlan.weatherupdate.model.metar.fields.WindDirection;
import com.marlan.weatherupdate.model.metar.fields.WindSpeed;
import com.marlan.weatherupdate.model.station.AVWXStation;
import com.marlan.weatherupdate.utilities.AltimeterUtility;
import com.marlan.weatherupdate.utilities.Logger;
import com.marlan.weatherupdate.utilities.StationInfoUtility;

import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Random;

public class MissionHandlerService {
    private static final double KNOTS_TO_METERS = 0.51444444444;
    private static final double INHG_TO_MMHG = 25.4;
    private static final double ISA_TEMP_C = 15;
    private static final double ISA_PRESSURE_INHG = 29.92;
    private static final double TEMP_LAPSE_RATE_C = 1.98;
    private static final double INHG_TO_HPA = 33.86389;

    private final int hour;
    private final double windSpeed;
    private final double windDirection;
    private final AVWXStation stationAVWX;
    private final double stationTempC;
    private final double stationQnh;
    private final int day;
    private final int month;
    private final String metar;

    public MissionHandlerService(DTO dto, AVWXStation stationAVWX, AVWXMetar metarAVWX) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(StationInfoUtility.getZoneId(stationAVWX.getCountry())));

        if (metarAVWX.getMeta().getWarning() != null) {
            Logger.warning(metarAVWX.getMeta().getWarning());
        }
        Logger.info("METAR: " + metarAVWX.getSanitized());

        switch (dto.getWeatherType()) {
            case "real" -> {
                if (zonedDateTime.getHour() + dto.getTimeOffset() < 0) {
                    this.hour = 24 + zonedDateTime.getHour() + dto.getTimeOffset();
                } else {
                    this.hour = zonedDateTime.getHour() + dto.getTimeOffset();
                }
                this.windSpeed = metarAVWX.getWindSpeed().flatMap(WindSpeed::getValue).orElse(0.0);
                this.windDirection = metarAVWX.getWindDirection().flatMap(WindDirection::getValue).orElse(0.0);
                this.stationTempC = metarAVWX.getTemperature().flatMap(Temperature::getValue).orElse(ISA_TEMP_C);
                if (metarAVWX.getUnits().getAltimeter().equals("hPa")) {
                    this.stationQnh = metarAVWX.getAltimeter().getValue() / INHG_TO_HPA;
                } else {
                    this.stationQnh = metarAVWX.getAltimeter().getValue();
                }
                this.metar = metarAVWX.getSanitized();
            }
            case "clearDay" -> {
                this.hour = 12;
                this.windSpeed = 0.0;
                this.windDirection = 0.0;
                this.stationTempC = metarAVWX.getTemperature().flatMap(Temperature::getValue).orElse(ISA_TEMP_C);
                this.stationQnh = ISA_PRESSURE_INHG;
                this.metar = "";
            }
            case "clearNight" -> {
                this.hour = 0;
                this.windSpeed = 0.0;
                this.windDirection = 0.0;
                this.stationTempC = metarAVWX.getTemperature().flatMap(Temperature::getValue).orElse(ISA_TEMP_C);
                this.stationQnh = ISA_PRESSURE_INHG;
                this.metar = "";
            }
            default -> {
                this.hour = 12;
                this.windSpeed = 0.0;
                this.windDirection = 0.0;
                this.stationTempC = ISA_TEMP_C;
                this.stationQnh = ISA_PRESSURE_INHG;
                this.metar = "";
            }
        }
        this.day = zonedDateTime.getDayOfMonth();
        this.month = zonedDateTime.getMonthValue();
        this.stationAVWX = stationAVWX;
    }

    public String editMission(String mission) {

        double windSpeedGround = getCorrectedGroundWindSpeed(windSpeed, stationAVWX.getElevationM()); // "Ground" Wind is 10m/33ft but also sets ~500m/1660ft
        double windSpeed2000 = getModifiedWindSpeed(2000, windSpeed); // "2000" Wind is 2000m/6600ft
        double windSpeed8000 = getModifiedWindSpeed(8000, windSpeed); // "8000" Wind is 8000m/26000ft

        double seaLevelTempC = stationTempC + TEMP_LAPSE_RATE_C * (stationAVWX.getElevationFt() / 1000);
        double correctedQffInHg = AltimeterUtility.getCorrectedQff(stationQnh, stationTempC, stationAVWX);
        double qffMmHg = correctedQffInHg * INHG_TO_MMHG;

        double windDirectionGround = invertWindDirection(windDirection); // Wind Direction is backwards in DCS.
        double windDirection2000 = randomizeWindDirection(windDirection);
        double windDirection8000 = randomizeWindDirection(windDirection2000);

        String cloudsPreset = buildCloudsPreset(selectCloudsPresetSuffix(metar));

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
        Logger.info("Clouds Preset: " + cloudsPreset);

        mission = mission.replaceAll("\\[\"at8000\"]\\s+=\\s+\\{([^}]*)",
                "[\"at8000\"] =\n            {\n                [\"speed\"] = $wind8000Speed,\n                [\"dir\"] = $wind8000Dir,\n            "
                        .replace("$wind8000Speed", Double.toString(windSpeed8000))
                        .replace("$wind8000Dir", Double.toString(windDirection8000)));
        Logger.info("Wind at 8000 set to: " + Math.round(windSpeed8000) + " m/s (" + Math.round(windSpeed8000 / KNOTS_TO_METERS) + " kts)" + " at " + Math.floor(invertWindDirection(windDirection8000)) + "°");

        mission = mission.replaceAll("\\[\"at2000\"]\\s+=\\s+\\{([^}]*)",
                "[\"at2000\"] =\n            {\n                [\"speed\"] = $wind2000Speed,\n                [\"dir\"] = $wind2000Dir,\n            "
                        .replace("$wind2000Speed", Double.toString(windSpeed2000))
                        .replace("$wind2000Dir", Double.toString(windDirection2000)));
        Logger.info("Wind at 2000 set to: " + Math.round(windSpeed2000) + " m/s (" + Math.round(windSpeed2000 / KNOTS_TO_METERS) + " kts)" + " at " + Math.floor(invertWindDirection(windDirection2000)) + "°");

        mission = mission.replaceAll("\\[\"atGround\"]\\s+=\\s+\\{([^}]*)",
                "[\"atGround\"] =\n            {\n                [\"speed\"] = $windGroundSpeed,\n                [\"dir\"] = $windGroundDir,\n            "
                        .replace("$windGroundSpeed", Double.toString(windSpeedGround))
                        .replace("$windGroundDir", Double.toString(windDirectionGround)));
        Logger.info("Wind at Ground set to: " + Math.round(windSpeedGround) + " m/s (" + Math.round(windSpeedGround / KNOTS_TO_METERS) + " kts)" + " at " + Math.floor(invertWindDirection(windDirectionGround)) + "°");

        mission = mission.replaceAll("(?<=\\[\"currentKey\"]\\s{1,5}=\\s{1,5}.{1,100}\n)(.*)", "    [\"start_time\"] = $startTime,".replace("$startTime", Integer.toString(hour * 3600)));
        Logger.info("Start Time set to: " + hour * 3600 + "s (" + hour + "h)");

        mission = mission.replaceAll("(\\[\"Day\"].*)\n", "[\"Day\"] = \\$day,\n".replace("$day", Integer.toString(day)));
        Logger.info("Day set to: " + day);

        mission = mission.replaceAll("(\\[\"Month\"].*)\n", "[\"Month\"] = \\$month,\n".replace("$month", Integer.toString(month)));
        Logger.info("Month set to: " + month);

        mission = mission.replaceAll("(\\[\"temperature\"].*)\n", "[\"temperature\"] = \\$stationTempC,\n".replace("$stationTempC", Double.toString(stationTempC)));
        Logger.info("Station Temperature set to: " + stationTempC + " C" + " / Sea Level Temperature set to: " + Math.round(seaLevelTempC) + " C");

        mission = mission.replaceAll("(\\[\"qnh\"].*)\n", "[\"qnh\"] = \\$qnh,\n".replace("$qnh", Double.toString(qffMmHg))); // DCS actually uses QFF not QNH!
        Logger.info("QFF set to: " + qffMmHg + " mmHg (" + qffMmHg / INHG_TO_MMHG + " inHg)");

        return mission;
    }

    private double getCorrectedGroundWindSpeed(double windSpeedKnots, double stationAltitude) {
        double windSpeedMultiplier;
        windSpeedMultiplier = Math.abs((0.5 / 500) * stationAltitude - 1);
        return windSpeedKnots * windSpeedMultiplier * KNOTS_TO_METERS;
    }

    private double getModifiedWindSpeed(double altitudeMeters, double windSpeedKnots) {
        Random random = new SecureRandom();
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
