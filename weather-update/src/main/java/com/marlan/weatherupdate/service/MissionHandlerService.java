package com.marlan.weatherupdate.service;

import com.marlan.weatherupdate.model.AVWXWeather;
import com.marlan.weatherupdate.utilities.ZuluLocalConverter;
import lombok.NoArgsConstructor;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import static java.lang.System.out;

@NoArgsConstructor
public class MissionHandlerService {
    public String editMission(String mission, AVWXWeather weatherAVWX) throws NoSuchAlgorithmException {
        ZuluLocalConverter zuluLocalConverter = new ZuluLocalConverter();
        double qnhInMg = weatherAVWX.getAltimeter().getValue() * 25.4;
        double tempC = weatherAVWX.getTemperature().getValue();
        double windSpeed = Math.min(weatherAVWX.getWindSpeed().getValue() / 1.944, 15);
        double windDir = weatherAVWX.getWindDirection().getValue();
        String metar = weatherAVWX.getSanitized();
        int timeInSeconds = ((Integer.parseInt(weatherAVWX.getTime().getRepr().substring(2, 4)) + zuluLocalConverter.zuluToLocalConversion(weatherAVWX.getStation())) % 24) * 3600;
        String cloudsPreset = buildCloudsPreset(selectCloudsPresetSuffix(metar));

        if (!mission.contains("[\"preset\"]")) {
            mission = mission.replaceAll("(\\[\"iprecptns\"].*)\n", "[\"iprecptns\"] = 0,\n            [\"preset\"] = \"\\$cloudsPreset\",\n").replace("$cloudsPreset", cloudsPreset);
        } else {
            mission = mission.replaceAll("(\\[\"preset\"].*)\n", "[\"preset\"] = \"\\$cloudsPreset\",\n".replace("$cloudsPreset", cloudsPreset));
        }
        out.println("Clouds Preset set to: " + cloudsPreset);

        // TODO: Match Ground only?
        mission = mission.replaceAll("(\\[\"speed\"].*)\n", "[\"speed\"] = \\$windSpeed,\n".replace("$windSpeed", Double.toString(windSpeed)));
        out.println("Wind Speed set to: " + windSpeed + " m/s");

        mission = mission.replaceAll("(\\[\"dir\"].*)\n", "[\"dir\"] = \\$windDir,\n".replace("$windDir", Double.toString(windDir)));
        out.println("Wind Direction set to: " + windDir);

        mission = mission.replaceAll("(\\[\"start_time\"].*)\n", "[\"start_time\"] = \\$startTime,\n".replace("$startTime", Integer.toString(timeInSeconds)));
        out.println("Start Time set to: " + timeInSeconds + "s");

        mission = mission.replaceAll("(\\[\"Day\"].*)\n", "[\"Day\"] = \\$day,\n".replace("$day", Integer.toString(Integer.parseInt(weatherAVWX.getTime().getRepr().substring(0, 2)))));
        out.println("Day set to: " + Integer.parseInt(weatherAVWX.getTime().getRepr().substring(0, 2)));

        mission = mission.replaceAll("(\\[\"Month\"].*)\n", "[\"Month\"] = \\$month,\n".replace("$month", Integer.toString(Integer.parseInt(weatherAVWX.getTime().getDt().substring(5, 7)))));
        out.println("Month set to: " + Integer.parseInt(weatherAVWX.getTime().getDt().substring(5, 7)));

        mission = mission.replaceAll("(\\[\"temperature\"].*)\n", "[\"temperature\"] = \\$tempC,\n".replace("$tempC", Double.toString(tempC)));
        out.println("Temperature set to: " + tempC + "C");

        mission = mission.replaceAll("(\\[\"qnh\"].*)\n", "[\"qnh\"] = \\$qnh,\n".replace("$qnh", Double.toString(qnhInMg)));
        out.println("QNH set to: " + qnhInMg + "inHg");

        return mission;
    }

    private String buildCloudsPreset(int cloudsPresetSuffix){
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

    private int selectCloudsPresetSuffix(String metar) throws NoSuchAlgorithmException {
        Random random = SecureRandom.getInstanceStrong();
        if (metar.contains("SKC") || metar.contains("NCD")) return 0;
        if (metar.contains("CLR") || metar.contains("NSC") || metar.contains("CAVOK")) return random.nextInt(3);
        if (metar.contains("OVC")) return random.nextInt(10) + 21;
        if (metar.contains("BKN")) return random.nextInt(8) + 13;
        if (metar.contains("SCT")) return random.nextInt(10) + 3;
        if (metar.contains("FEW")) return random.nextInt(5) + 1;
        return 0;
    }
}
