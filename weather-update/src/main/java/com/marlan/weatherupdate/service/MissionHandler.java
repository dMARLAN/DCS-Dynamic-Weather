package com.marlan.weatherupdate.service;

import com.marlan.weatherupdate.model.AVWXWeather;
import com.marlan.weatherupdate.utilities.ZuluLocalConverter;
import lombok.NoArgsConstructor;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import static java.lang.System.out;

@NoArgsConstructor
public class MissionHandler {
    public String editMission(String mission, AVWXWeather weatherAVWX) throws NoSuchAlgorithmException {
        double qnhInMg = weatherAVWX.getAltimeter().getValue() * 25.4;
        double tempC = weatherAVWX.getTemperature().getValue();
        double windSpeed = Math.min(weatherAVWX.getWindSpeed().getValue() / 1.944, 15);
        double windDir = weatherAVWX.getWindDirection().getValue();
        ZuluLocalConverter zuluLocalConverter = new ZuluLocalConverter();
        String metar = weatherAVWX.getSanitized();
        int timeInSeconds = ((Integer.parseInt(weatherAVWX.getTime().getRepr().substring(2, 4)) + zuluLocalConverter.zuluToLocalConversion(weatherAVWX.getStation())) % 24) * 3600;

        String cloudsPreset;
        int cloudsPresetSuffix = selectCloudsPresetSuffix(metar);
        if (cloudsPresetSuffix == 0) {
            cloudsPreset = "nil";
        } else {
            if (cloudsPresetSuffix > 27) {
                cloudsPreset = "RainyPreset" + cloudsPresetSuffix % 27; // Converts Presets28-30 to RainyPreset1-3
            } else {
                cloudsPreset = "Preset" + cloudsPresetSuffix;
            }
        }

        String regexCloudsPreset;
        String replaceCloudsPreset;
        if (!mission.contains("[\"preset\"]")) {
            regexCloudsPreset = "(\\[\"iprecptns\"].*)\n";
            replaceCloudsPreset = ("[\"iprecptns\"] = 0,\n            [\"preset\"] = \"$cloudsPreset\",\n").replace("$cloudsPreset", cloudsPreset);
        } else {
            regexCloudsPreset = "(\\[\"preset\"].*)\n";
            replaceCloudsPreset = "[\"preset\"] = \"$cloudsPreset\",\n".replace("$cloudsPreset", cloudsPreset);
        }
        mission = mission.replaceAll(regexCloudsPreset, replaceCloudsPreset);
        out.println("Clouds Preset set to: " + cloudsPreset);

        // TODO: Match Ground Speed only?
        String regexWindSpeed = "(\\[\"speed\"].*)\n";
        String replaceWindSpeed = "[\"speed\"] = $windSpeed,\n".replace("$windSpeed", Double.toString(windSpeed));
        mission = mission.replaceAll(regexWindSpeed, replaceWindSpeed);
        out.println("Wind Speed set to: " + windSpeed + " m/s");

        String regexWindDir = "(\\[\"dir\"].*)\n";
        String replaceWindDir = "[\"dir\"] = $windDir,\n".replace("$windDir", Double.toString(windDir));
        mission = mission.replaceAll(regexWindDir, replaceWindDir);
        out.println("Wind Direction set to: " + windDir);

        String regexStartTime = "(\\[\"start_time\"].*)\n";
        String replaceStartTime = "[\"start_time\"] = $startTime,\n".replace("$startTime", Integer.toString(timeInSeconds));
        mission = mission.replaceAll(regexStartTime, replaceStartTime);
        out.println("Start Time set to: " + timeInSeconds + "s");

        String regexDay = "(\\[\"Day\"].*)\n";
        String replaceDay = "[\"Day\"] = $day,\n".replace("$day", Integer.toString(Integer.parseInt(weatherAVWX.getTime().getRepr().substring(0, 2))));
        mission = mission.replaceAll(regexDay, replaceDay);
        out.println("Day set to: " + Integer.parseInt(weatherAVWX.getTime().getRepr().substring(0, 2)));

        String regexMonth = "(\\[\"Month\"].*)\n";
        String replaceMonth = "[\"Month\"] = $month,\n".replace("$month", Integer.toString(Integer.parseInt(weatherAVWX.getTime().getDt().substring(5, 7))));
        mission = mission.replaceAll(regexMonth, replaceMonth);
        out.println("Month set to: " + Integer.parseInt(weatherAVWX.getTime().getDt().substring(5, 7)));

        String regexTempC = "(\\[\"temperature\"].*)\n";
        String replaceTempC = "[\"temperature\"] = $tempC,\n".replace("$tempC", Double.toString(tempC));
        mission = mission.replaceAll(regexTempC, replaceTempC);
        out.println("Temperature set to: " + tempC + "C");

        String regexQNH = "(\\[\"qnh\"].*)\n";
        String replaceQNH = "[\"qnh\"] = $qnh,\n".replace("$qnh", Double.toString(qnhInMg));
        mission = mission.replaceAll(regexQNH, replaceQNH);
        out.println("QNH set to: " + qnhInMg + "inHg");

        return mission;
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
