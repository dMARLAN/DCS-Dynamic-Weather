package com.marlan.weatherupdate.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatherupdate.model.AVWXWeather;
import com.marlan.weatherupdate.model.WeatherUpdateData;
import com.marlan.weatherupdate.service.MizHandler;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import static java.lang.System.*;

public class DCSRealWeather {

    public static void main(String[] args) throws Exception {
        final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        final String DIR = getProperty("user.dir") + "\\";
        final String API_KEY;
        final String ICAO;
        final String MIZ_NAME;

        Path dataFilePath = Path.of(DIR + "Data.txt");
        String dataFileContent = Files.readString(dataFilePath);
        WeatherUpdateData weatherUpdateData = gson.fromJson(dataFileContent, WeatherUpdateData.class);
        API_KEY = weatherUpdateData.getAvwxApiKey();
        ICAO = weatherUpdateData.getIcao();
        MIZ_NAME = weatherUpdateData.getMission();

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("https://avwx.rest/api/metar/" + ICAO))
                .header("Authorization", API_KEY)
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

        AVWXWeather weatherAVWX = gson.fromJson(getResponse.body(), AVWXWeather.class);
        out.println("METAR: " + weatherAVWX.getSanitized());

        String mizPath = DIR + "mission_files\\" + MIZ_NAME;
        MizHandler mizHandler = new MizHandler(mizPath);
        mizHandler.extractMission();

        Path missionFilePath = Path.of(DIR + "mission");
        String missionFileContent = Files.readString(missionFilePath);
        String replacedMissionFileContent = replaceMission(missionFileContent, weatherAVWX);

        File missionFile = new File(String.valueOf(missionFilePath));
        try (FileWriter fileWriter = new FileWriter(missionFile, false)) {
            fileWriter.write(replacedMissionFileContent);
        }
        mizHandler.setMissionFile(String.valueOf(missionFilePath));
        mizHandler.updateMiz();
    }

    private static String replaceMission(String mission, AVWXWeather weatherAVWX) throws NoSuchAlgorithmException {
        double qnhInMg = weatherAVWX.getAltimeter().getValue() * 25.4;
        double tempC = weatherAVWX.getTemperature().getValue();
        double windSpeed = Math.min(weatherAVWX.getWindSpeed().getValue() / 1.944, 15);
        double windDir = weatherAVWX.getWindDirection().getValue();
        int timeInSeconds = ((Integer.parseInt(weatherAVWX.getTime().getRepr().substring(2, 4)) + zuluToLocalConversion(weatherAVWX)) % 24) * 3600;
        String metar = weatherAVWX.getSanitized();

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
        String replaceDay = "[\"Day\"] = $day,\n".replace("$day", Integer.toString(Integer.parseInt(weatherAVWX.getTime().getRepr().substring(0,2))));
        mission = mission.replaceAll(regexDay, replaceDay);
        out.println("Day set to: " + Integer.parseInt(weatherAVWX.getTime().getRepr().substring(0, 2)));

        String regexMonth = "(\\[\"Month\"].*)\n";
        String replaceMonth = "[\"Month\"] = $month,\n".replace("$month", Integer.toString(Integer.parseInt(weatherAVWX.getTime().getDt().substring(5,7))));
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

    private static int zuluToLocalConversion(AVWXWeather avwxWeather){
        String station = avwxWeather.getStation();
        if (station.equals("KLSV")) return -6; // TODO: Expand on this!
        out.println("Station Unknown, ZuluToLocalConversion set to 0");
        return 0;
    }

    private static int selectCloudsPresetSuffix(String metar) throws NoSuchAlgorithmException {
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
