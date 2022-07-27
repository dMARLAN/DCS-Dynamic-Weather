package com.marlan.weatherupdate.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatherupdate.model.datafile.WeatherUpdateData;
import com.marlan.weatherupdate.model.metar.AVWXMetar;
import com.marlan.weatherupdate.model.station.AVWXStation;
import com.marlan.weatherupdate.service.*;

import static java.lang.System.out;

public class DCSRealWeather {

    public static void main(String[] args) throws Exception {
        final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        final String dir = DirHandler.getWorkingDir(args);
        final String MISSION_FILE = "mission";
        final String DATA_FILE = "dao.json";

        AVWXClient avwxClient = new AVWXClient();
        MizHandler mizHandler = new MizHandler();
        FileHandler fileHandler = new FileHandler();

        String dataContent = fileHandler.readFile(dir, DATA_FILE);
        WeatherUpdateData weatherUpdateData = gson.fromJson(dataContent, WeatherUpdateData.class);

        AVWXMetar metarAVWX = gson.fromJson(avwxClient.getMetar(weatherUpdateData).body(), AVWXMetar.class);
        AVWXStation stationAVWX = gson.fromJson(avwxClient.getStation(weatherUpdateData, metarAVWX).body(), AVWXStation.class);

        weatherUpdateData.setIcao(stationAVWX.getIcao());
        fileHandler.writeJSON(dir, DATA_FILE, weatherUpdateData);

        if (metarAVWX.getMeta().getWarning() != null) {
            out.println("WARNING: " + metarAVWX.getMeta().getWarning());
        }
        out.println("INFO: METAR: " + metarAVWX.getSanitized());

        mizHandler.extractMission(dir, weatherUpdateData.getMission());
        String missionContent = fileHandler.readFile(dir, MISSION_FILE);

        MissionHandler missionHandler = new MissionHandler(weatherUpdateData.getWeatherType(), stationAVWX, metarAVWX);
        String replacedMissionContent = missionHandler.editMission(missionContent);

        fileHandler.overwriteFile(dir, MISSION_FILE, replacedMissionContent);

        mizHandler.updateMiz(dir, weatherUpdateData.getMission(), MISSION_FILE);

        fileHandler.deleteFile(dir, MISSION_FILE);
    }
}
