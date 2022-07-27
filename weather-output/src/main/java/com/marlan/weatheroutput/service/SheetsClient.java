package com.marlan.weatheroutput.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.marlan.weatheroutput.utilities.FileHandler;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class SheetsClient {
    private static final String APPLICATION_NAME = "dcs-weather-output";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_NAME = "credentials.json";

    private SheetsClient() {
    }

    public static void setSheetValue(final String spreadsheetId, final String range, final String value, final String dir) throws IOException, GeneralSecurityException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport, dir))
                .setApplicationName(APPLICATION_NAME)
                .build();

        ValueRange requestBody = new ValueRange();
        requestBody.setValues(List.of(List.of(value)));
        Sheets.Spreadsheets.Values.Update request = service.spreadsheets().values().update(spreadsheetId, range, requestBody);
        request.setValueInputOption("RAW");
        UpdateValuesResponse response = request.execute();
        System.out.println("Sheets Update Response: " + response.toString());
    }

    private static Credential getCredentials(final NetHttpTransport httpTransport, final String dir) throws IOException {
        FileReader reader = new FileReader(dir + CREDENTIALS_FILE_NAME);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setScopes(SCOPES)
                .setAccessType("online")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}