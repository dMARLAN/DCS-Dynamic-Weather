package com.marlan.weatheroutput.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marlan.weatheroutput.model.DAO;
import com.marlan.weatheroutput.service.DiscordClient;
import com.marlan.weatheroutput.service.SheetsClient;
import com.marlan.weatheroutput.utilities.DirHandler;
import com.marlan.weatheroutput.utilities.FileHandler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import static java.lang.System.out;

public class DiscordWebHookService {
    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException, GeneralSecurityException {
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
        out.println("Response Code: " + DiscordClient.postChannel(dao, jsonInput).statusCode());
        SheetsClient.setSheetValue(dao.getSpreadsheetId(), dao.getSpreadsheetRange(), dao.getMetar(), dir);
    }

}
