package com.marlan.weatherupdate.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatherupdate.model.dto.DTO;
import com.marlan.weatherupdate.model.metar.AVWXMetar;
import com.marlan.weatherupdate.model.station.AVWXStation;
import com.marlan.weatherupdate.service.AVWXClient;
import com.marlan.weatherupdate.service.MissionEditor;
import com.marlan.weatherupdate.utilities.DirHandler;
import com.marlan.weatherupdate.utilities.FileHandler;
import com.marlan.weatherupdate.utilities.Logger;
import com.marlan.weatherupdate.utilities.MizUtility;

import java.io.IOException;
import java.net.URISyntaxException;

public class DCSRealWeatherController {

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        final String DIR = DirHandler.getWorkingDir(args);
        Logger.setDir(DIR);

        final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

        final String MISSION_FILE = "mission";
        final String DTO_PATH = "data\\dto.json";

        String dataContent = FileHandler.readFile(DIR, DTO_PATH);

        DTO dto = gson.fromJson(dataContent, DTO.class);

        AVWXMetar metarAVWX;
        AVWXStation stationAVWX;

        AVWXClient avwxClient = new AVWXClient();
        metarAVWX = gson.fromJson(avwxClient.getMetar(dto).body(), AVWXMetar.class);
        stationAVWX = gson.fromJson(avwxClient.getStation(dto, metarAVWX).body(), AVWXStation.class);

        dto.setIcao(stationAVWX.getIcao());
        FileHandler.writeJSON(DIR, DTO_PATH, dto);

        MizUtility.extractMission(DIR, dto.getMission());
        String missionContent = FileHandler.readFile(DIR, MISSION_FILE);

        MissionEditor missionEditor = new MissionEditor(dto, stationAVWX, metarAVWX);

        String replacedMissionContent = missionEditor.editMission(missionContent);

        FileHandler.overwriteFile(DIR, MISSION_FILE, replacedMissionContent);

        MizUtility.updateMiz(DIR, dto.getMission(), MISSION_FILE);

        FileHandler.deleteFile(DIR, MISSION_FILE);
    }

}
