package com.marlan.weatheroutput.service.sheets;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.marlan.weatheroutput.model.Config;
import com.marlan.weatheroutput.utilities.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class SheetsClient {
    private static final String APPLICATION_NAME = "DCS-Dynamic-Weather";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_NAME = "secrets\\google_sheets_service_account_api_key.json";

    private SheetsClient() {
    }

    public static void setSheetValue(final String spreadsheetId, final String spreadsheetRange,
                                     final String value, final String workingDir, final Config config)
            throws IOException, GeneralSecurityException {

        if (!validParameters(workingDir, config)) return;

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        final GoogleCredentials googleCredentials = ServiceAccountCredentials
                .fromStream(new FileInputStream(workingDir + CREDENTIALS_FILE_NAME))
                .createScoped(SCOPES);
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(googleCredentials);

        Sheets service = new Sheets.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();

        ValueRange requestBody = new ValueRange();
        requestBody.setValues(List.of(List.of(value)));
        Sheets.Spreadsheets.Values.Update request = service.spreadsheets().values().update(spreadsheetId, spreadsheetRange, requestBody);
        request.setValueInputOption("RAW");

        String response;
        try {
            response = request.execute().toString();
        } catch (GoogleJsonResponseException e) {
            response = e.toString();
        }
        Logger.info("Sheets Update Response: " + response);
    }

    private static boolean validParameters(final String workingDir, final Config config) {
        if (!Files.exists(Paths.get(workingDir + CREDENTIALS_FILE_NAME))) {
            Logger.warning("Credentials file not found: " + workingDir + CREDENTIALS_FILE_NAME);
            return false;
        }
        if (config.getSpreadsheetId().isEmpty()) {
            Logger.warning("Spreadsheet ID empty");
            return false;
        }
        if (config.getSpreadsheetRange().isEmpty()) {
            Logger.warning("Spreadsheet Range empty");
            return false;
        }
        return true;
    }

}