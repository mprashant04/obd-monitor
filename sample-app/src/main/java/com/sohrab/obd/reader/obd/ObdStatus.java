package com.sohrab.obd.reader.obd;

import com.sohrab.obd.reader.util.DateUtils;

import java.util.Date;

public class ObdStatus {

    private static Date latestEngineRunningOn = new Date();


    public static long getEngineOffSinceSeconds() {
        return DateUtils.diffInSeconds(latestEngineRunningOn);
    }


    public static synchronized void engineRunning() {
        latestEngineRunningOn = new Date();
    }


}
