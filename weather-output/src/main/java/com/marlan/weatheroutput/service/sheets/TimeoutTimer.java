package com.marlan.weatheroutput.service.sheets;

import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;

import java.io.IOException;

public class TimeoutTimer extends Thread {
    LocalServerReceiver receiver;
    int timeout;

    public TimeoutTimer(LocalServerReceiver receiver, int timeout) {
        this.receiver = receiver;
        this.timeout = timeout;
    }

    @Override
    public void run () {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            receiver.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
