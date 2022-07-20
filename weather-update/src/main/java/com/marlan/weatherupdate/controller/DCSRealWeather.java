package com.marlan.weatherupdate.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatherupdate.model.AVWXWeather;
import com.marlan.weatherupdate.model.WeatherUpdateData;
import com.marlan.weatherupdate.service.*;
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

        AVWXWeather weatherAVWX = gson.fromJson(avwxClient.getWeather(weatherUpdateData).body(), AVWXWeather.class);
        out.println("METAR: " + weatherAVWX.getSanitized());

        mizHandler.extractMission(dir, weatherUpdateData.getMission());
        String missionContent = fileHandler.readFile(dir, MISSION_FILE_NAME);

        String replacedMissionContent = missionHandler.editMission(missionContent, weatherAVWX);

        fileHandler.overwriteFile(dir, MISSION_FILE_NAME, replacedMissionContent);

        mizHandler.updateMiz(dir, weatherUpdateData.getMission(), MISSION_FILE_NAME);

        fileHandler.deleteFile(dir,MISSION_FILE_NAME);
    }
}
