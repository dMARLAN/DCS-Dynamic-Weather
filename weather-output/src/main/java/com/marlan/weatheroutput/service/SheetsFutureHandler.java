package com.marlan.weatheroutput.service;

import java.util.concurrent.*;

import static java.lang.System.out;

public class SheetsFutureHandler {
    private SheetsFutureHandler() {
    }

    public static void tryPutSpreadsheetValue(String spreadsheetId, String spreadsheetRange, String value, String dir) {
        SheetsClient sheetsClient = new SheetsClient(spreadsheetId, spreadsheetRange, value, dir);
        if ( spreadsheetId.length() == 0 || spreadsheetRange.length() == 0 ) {
            out.println("INFO: No spreadsheet ID/Range provided. Skipping Google Sheets update.");
        } else {
            FutureTask<String> futureTask = new FutureTask<>(sheetsClient);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(futureTask);
            while (true){
                if (futureTask.isDone()) {
                    out.println("INFO: Google Sheets Update complete.");
                    executorService.shutdown();
                    return;
                }
                try {
                    out.println("INFO: Waiting for Google Sheets Authorization...");
                    futureTask.get(30, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    out.println("INFO: Google Sheets Update timed out. Skipping.");
                    System.exit(0);
                } catch (ExecutionException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }

        }
    }
}
