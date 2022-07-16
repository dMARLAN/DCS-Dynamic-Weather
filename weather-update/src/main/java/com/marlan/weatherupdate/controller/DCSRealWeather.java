package com.marlan.weatherupdate.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatherupdate.model.AVWXWeather;
import com.marlan.weatherupdate.model.WeatherUpdateData;
import com.marlan.weatherupdate.service.AVWXClient;
import com.marlan.weatherupdate.service.FileHandlerService;
import com.marlan.weatherupdate.service.MissionHandlerService;
import com.marlan.weatherupdate.service.MizHandlerService;

import static java.lang.System.getProperty;
import static java.lang.System.out;

public class DCSRealWeather {

    public static void main(String[] args) throws Exception {
        final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        final String DIR = getProperty("user.dir") + "\\";
        AVWXClient avwxClient = new AVWXClient();
        MizHandlerService mizHandlerService = new MizHandlerService();
        FileHandlerService fileHandlerService = new FileHandlerService();
        MissionHandlerService missionHandlerService = new MissionHandlerService();
        final String MISSION_FILE_NAME = "mission";

        String dataContent = fileHandlerService.readFile(DIR, "Data.txt");
        WeatherUpdateData weatherUpdateData = gson.fromJson(dataContent, WeatherUpdateData.class);

        AVWXWeather weatherAVWX = gson.fromJson(avwxClient.getWeather(weatherUpdateData).body(), AVWXWeather.class);
        out.println("METAR: " + weatherAVWX.getSanitized());

        mizHandlerService.extractMission(DIR + weatherUpdateData.getMission());
        String missionContent = fileHandlerService.readFile(DIR, MISSION_FILE_NAME);

        String replacedMissionContent = missionHandlerService.editMission(missionContent, weatherAVWX);

        fileHandlerService.overwriteFile(DIR, MISSION_FILE_NAME, replacedMissionContent);

        mizHandlerService.updateMiz(weatherUpdateData.getMission(), MISSION_FILE_NAME);
    }
}
