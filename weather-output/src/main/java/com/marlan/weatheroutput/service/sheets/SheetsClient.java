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
import com.marlan.weatheroutput.model.DTO;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import static java.lang.System.out;

public record SheetsClient(String spreadsheetId, String spreadsheetRange, String value, String dir, DTO dto) {
    private static final String APPLICATION_NAME = "DCS-Dynamic-Weather";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_NAME = "credentials.json";

    public void setSheetValue() throws IOException, GeneralSecurityException {

        if (!validParameters()) return;

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        final GoogleCredentials googleCredentials = ServiceAccountCredentials
                .fromStream(new FileInputStream(dir + CREDENTIALS_FILE_NAME))
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
        out.println(response);

    }

    private boolean validParameters() {
        if (!Files.exists(Paths.get(dir + CREDENTIALS_FILE_NAME))) {
            out.println("ERROR: Credentials file not found: " + dir + CREDENTIALS_FILE_NAME);
            return false;
        }
        if (dto.getSpreadsheetId().isEmpty()) {
            out.println("ERROR: Spreadsheet ID empty.");
            return false;
        }
        if (dto.getSpreadsheetRange().isEmpty()) {
            out.println("ERROR: Spreadsheet Range empty.");
            return false;
        }
        return true;
    }

}