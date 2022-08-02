package com.marlan.weatheroutput.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatheroutput.model.DAO;
import com.marlan.weatheroutput.service.DiscordClient;
import com.marlan.weatheroutput.utilities.SheetsFutureHandler;
import com.marlan.weatheroutput.utilities.DirHandler;
import com.marlan.weatheroutput.utilities.FileHandler;

import java.io.IOException;
import java.net.URISyntaxException;

public class DiscordWebHookService {
    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        final String DATA_FILE = "dao.json";
        final String dir = DirHandler.getWorkingDir(args);

        String dataFileContent = FileHandler.readFile(dir, DATA_FILE);
        DAO dao = gson.fromJson(dataFileContent, DAO.class);

        String jsonInput = """
                {
                  "embeds": [
                   {
                    "description": "$METAR",
                    "color": 0
                   }
                  ]
                }
                """.replace("$METAR", dao.getMetar());

        DiscordClient.post(dao, jsonInput);
        SheetsFutureHandler.put(dao.getSpreadsheetId(), dao.getSpreadsheetRange(), dao.getMetar(), dir);
    }
}
