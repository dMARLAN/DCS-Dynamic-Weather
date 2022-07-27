package com.marlan.weatherupdate.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatherupdate.model.datafile.DAO;
import com.marlan.weatherupdate.model.metar.AVWXMetar;
import com.marlan.weatherupdate.model.station.AVWXStation;
import com.marlan.weatherupdate.service.AVWXClient;
import com.marlan.weatherupdate.service.MissionHandlerService;
import com.marlan.weatherupdate.utilities.DirUtility;
import com.marlan.weatherupdate.utilities.FileUtility;
import com.marlan.weatherupdate.utilities.MizUtility;

import static java.lang.System.out;

public class DCSRealWeather {

    public static void main(String[] args) throws Exception {
        final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        final String dir = DirUtility.getWorkingDir(args);
        final String MISSION_FILE = "mission";
        final String DATA_FILE = "dao.json";

        AVWXClient avwxClient = new AVWXClient();

        String dataContent = FileUtility.readFile(dir, DATA_FILE);
        DAO dao = gson.fromJson(dataContent, DAO.class);

        AVWXMetar metarAVWX = gson.fromJson(avwxClient.getMetar(dao).body(), AVWXMetar.class);
        AVWXStation stationAVWX = gson.fromJson(avwxClient.getStation(dao, metarAVWX).body(), AVWXStation.class);

        dao.setIcao(stationAVWX.getIcao());
        FileUtility.writeJSON(dir, DATA_FILE, dao);

        if (metarAVWX.getMeta().getWarning() != null) {
            out.println("WARNING: " + metarAVWX.getMeta().getWarning());
        }
        out.println("INFO: METAR: " + metarAVWX.getSanitized());

        MizUtility.extractMission(dir, dao.getMission());
        String missionContent = FileUtility.readFile(dir, MISSION_FILE);

        MissionHandlerService missionHandlerService = new MissionHandlerService(dao.getWeatherType(), stationAVWX, metarAVWX);
        String replacedMissionContent = missionHandlerService.editMission(missionContent);

        FileUtility.overwriteFile(dir, MISSION_FILE, replacedMissionContent);

        MizUtility.updateMiz(dir, dao.getMission(), MISSION_FILE);

        FileUtility.deleteFile(dir, MISSION_FILE);
    }
}
