package com.marlan.weatherupdate.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatherupdate.model.metar.AVWXMetar;
import com.marlan.weatherupdate.model.metar.fields.WeatherUpdateData;
import com.marlan.weatherupdate.model.station.AVWXStation;
import com.marlan.weatherupdate.service.AVWXClient;
import com.marlan.weatherupdate.service.FileHandler;
import com.marlan.weatherupdate.service.MissionHandler;
import com.marlan.weatherupdate.service.MizHandler;
import com.marlan.weatherupdate.utilities.DirHandler;

import static java.lang.System.out;

public class DCSRealWeather {

    public static void main(String[] args) throws Exception {
        final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        final String dir = DirHandler.getWorkingDir(args);
        final String MISSION_FILE_NAME = "mission";

        AVWXClient avwxClient = new AVWXClient();
        MizHandler mizHandler = new MizHandler();
        FileHandler fileHandler = new FileHandler();
        MissionHandler missionHandler = new MissionHandler();

        String dataContent = fileHandler.readFile(dir, "Data.txt");
        WeatherUpdateData weatherUpdateData = gson.fromJson(dataContent, WeatherUpdateData.class);

        AVWXMetar metarAVWX = gson.fromJson(avwxClient.getMetar(weatherUpdateData).body(), AVWXMetar.class);
        AVWXStation stationAVWX = gson.fromJson(avwxClient.getStation(weatherUpdateData, metarAVWX).body(), AVWXStation.class);
        if (metarAVWX.getMeta().getWarning() != null) {
            out.println("WARNING: " + metarAVWX.getMeta().getWarning());
        }
        out.println("INFO: METAR: " + metarAVWX.getSanitized());

        mizHandler.extractMission(dir, weatherUpdateData.getMission());
        String missionContent = fileHandler.readFile(dir, MISSION_FILE_NAME);

        String replacedMissionContent = missionHandler.editMission(missionContent, metarAVWX, stationAVWX);

        fileHandler.overwriteFile(dir, MISSION_FILE_NAME, replacedMissionContent);

        mizHandler.updateMiz(dir, weatherUpdateData.getMission(), MISSION_FILE_NAME);

        fileHandler.deleteFile(dir, MISSION_FILE_NAME);
    }
}
