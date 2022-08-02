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

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class SheetsClient {
    private final String spreadsheetId;
    private final String spreadsheetRange;
    private final String value;
    private final String dir;
    private final String applicationName;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_NAME = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_NAME = "credentials.json";

    public SheetsClient(String spreadsheetId, String spreadsheetRange, String value, String dir, String applicationName) {
        this.spreadsheetId = spreadsheetId;
        this.spreadsheetRange = spreadsheetRange;
        this.value = value;
        this.dir = dir;
        this.applicationName = applicationName;
    }

    public String setSheetValue() throws IOException, GeneralSecurityException {
        if (!Files.exists(Paths.get(CREDENTIALS_FILE_NAME))){
            return "ERROR: Credentials file not found";
        }

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport, dir))
                .setApplicationName(applicationName)
                .build();

        ValueRange requestBody = new ValueRange();
        requestBody.setValues(List.of(List.of(value)));
        Sheets.Spreadsheets.Values.Update request = service.spreadsheets().values().update(spreadsheetId, spreadsheetRange, requestBody);
        request.setValueInputOption("RAW");
        UpdateValuesResponse response = request.execute();
        return response.toString();
    }

    private Credential getCredentials(final NetHttpTransport httpTransport, final String dir) throws IOException {
        FileReader reader = new FileReader(dir + CREDENTIALS_FILE_NAME);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(dir + TOKENS_DIRECTORY_NAME)))
                .setScopes(SCOPES)
                .setApprovalPrompt("force")
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

}