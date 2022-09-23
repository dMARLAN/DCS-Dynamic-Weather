package com.marlan.weatherupdate.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatherupdate.model.config.Config;
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
        final String WORKING_DIR = DirHandler.getWorkingDir(args);
        Logger.setDir(WORKING_DIR);

        final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

        final String MISSION_FILE = "mission";
        final String DTO_PATH = "data\\dto.json";
        final String CONFIG_PATH = "config.json";

        String dataContent = FileHandler.readFile(WORKING_DIR, DTO_PATH);
        String configFileContent = FileHandler.readFile(WORKING_DIR, CONFIG_PATH);

        DTO dto = gson.fromJson(dataContent, DTO.class);
        Config config = gson.fromJson(configFileContent, Config.class);

        AVWXClient avwxClient = new AVWXClient(WORKING_DIR);
        AVWXMetar metarAVWX = gson.fromJson(avwxClient.getMetar(dto).body(), AVWXMetar.class);
        AVWXStation stationAVWX = gson.fromJson(avwxClient.getStation(metarAVWX).body(), AVWXStation.class);

        dto.setIcao(stationAVWX.getIcao());
        FileHandler.writeJSON(WORKING_DIR, DTO_PATH, dto);

        MizUtility mizUtility = new MizUtility(config);
        mizUtility.extractMission(WORKING_DIR, dto.getMission());
        String missionContent = FileHandler.readFile(WORKING_DIR, MISSION_FILE);

        MissionEditor missionEditor = new MissionEditor(dto, stationAVWX, metarAVWX);

        String replacedMissionContent = missionEditor.editMission(missionContent);

        FileHandler.overwriteFile(WORKING_DIR, MISSION_FILE, replacedMissionContent);

        mizUtility.updateMiz(WORKING_DIR, dto.getMission(), MISSION_FILE);

        FileHandler.deleteFile(WORKING_DIR, MISSION_FILE);
    }

}
