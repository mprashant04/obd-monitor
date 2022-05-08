package com.sohrab.obd.reader.obd;

import com.sohrab.obd.reader.util.DateUtils;

import java.util.Date;

public class ObdStatus {

    private static Date latestDataReceivedOn = new Date();


    public static long getLatestDataReceivedSinceSeconds() {
        return DateUtils.diffInSeconds(latestDataReceivedOn);
    }


    public static synchronized void dataReceived() {
        latestDataReceivedOn = new Date();
    }


}
