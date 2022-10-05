package com.marlan.weatherupdate.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatherupdate.model.config.Config;
import com.marlan.weatherupdate.model.dto.DTO;
import com.marlan.weatherupdate.model.metar.AVWXMetar;
import com.marlan.weatherupdate.model.station.AVWXStation;
import com.marlan.weatherupdate.service.AVWXClient;
import com.marlan.weatherupdate.service.missioneditor.MissionEditor;
import com.marlan.weatherupdate.service.missioneditor.MissionValues;
import com.marlan.weatherupdate.utilities.FileHandler;
import com.marlan.weatherupdate.utilities.Log;
import com.marlan.weatherupdate.utilities.MizUtility;

/**
 * Controller for Weather Update module
 */
public class WeatherUpdateController {
    private WeatherUpdateController(){
    }

    /**
     * @param WORKING_DIR Working directory of the program which is the location of this file (which should also include
     *                    the other folders and files needed for the program to run e.g. data, constants, secrets, etc.)
     */
    public static void run(final String WORKING_DIR) {
        Log.info("Working Directory: " + WORKING_DIR);
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

        MissionValues missionValues = new MissionValues(config, dto, stationAVWX, metarAVWX);
        MissionEditor missionEditor = new MissionEditor(stationAVWX, missionValues);

        String replacedMissionContent = missionEditor.editMission(missionContent);

        FileHandler.overwriteFile(WORKING_DIR, MISSION_FILE, replacedMissionContent);

        mizUtility.updateMiz(WORKING_DIR, dto.getMission(), MISSION_FILE);

        FileHandler.deleteFile(WORKING_DIR, MISSION_FILE);

        Log.close();
    }

}
