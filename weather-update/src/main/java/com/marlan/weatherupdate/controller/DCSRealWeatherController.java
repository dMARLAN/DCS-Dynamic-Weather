package com.marlan.weatherupdate.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatherupdate.model.dto.DTO;
import com.marlan.weatherupdate.model.metar.AVWXMetar;
import com.marlan.weatherupdate.model.station.AVWXStation;
import com.marlan.weatherupdate.service.AVWXClient;
import com.marlan.weatherupdate.service.MissionHandlerService;
import com.marlan.weatherupdate.utilities.DirUtility;
import com.marlan.weatherupdate.utilities.FileUtility;
import com.marlan.weatherupdate.utilities.MizUtility;

import java.io.IOException;
import java.net.URISyntaxException;

public class DCSRealWeatherController {

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        final String dir = DirUtility.getWorkingDir(args);
        final String MISSION_FILE = "mission";
        final String DATA_FILE = "dto.json";

        AVWXClient avwxClient = new AVWXClient();

        String dataContent = FileUtility.readFile(dir, DATA_FILE);
        DTO dto = gson.fromJson(dataContent, DTO.class);

        AVWXMetar metarAVWX;
        AVWXStation stationAVWX;

        metarAVWX = gson.fromJson(avwxClient.getMetar(dto).body(), AVWXMetar.class);
        stationAVWX = gson.fromJson(avwxClient.getStation(dto, metarAVWX).body(), AVWXStation.class);

        dto.setIcao(stationAVWX.getIcao());
        FileUtility.writeJSON(dir, DATA_FILE, dto);

        MizUtility.extractMission(dir, dto.getMission());
        String missionContent = FileUtility.readFile(dir, MISSION_FILE);

        MissionHandlerService missionHandlerService = new MissionHandlerService(dto, stationAVWX, metarAVWX);

        String replacedMissionContent = missionHandlerService.editMission(missionContent);

        FileUtility.overwriteFile(dir, MISSION_FILE, replacedMissionContent);

        MizUtility.updateMiz(dir, dto.getMission(), MISSION_FILE);

        FileUtility.deleteFile(dir, MISSION_FILE);
    }

}
