package com.sohrab.obd.reader.obd;

import android.content.Context;

import com.sohrab.obd.reader.trip.TripRecord;
import com.sohrab.obd.reader.util.DateUtils;
import com.sohrab.obd.reader.util.MultimediaUtils;

import java.util.Date;

public class VehicleStatus {
    private enum EngineRunningStatus {RUNNING, STOPPED, UNKNOWN}

    private static EngineRunningStatus engineStatus = EngineRunningStatus.UNKNOWN;
    private static Date engineStartedOn = new Date();
    private static Date engineStoppedOn = new Date();

    public static void update(Context context, TripRecord tripRecord) {
        updateEngineStatus(context, tripRecord);
    }

    private static synchronized void updateEngineStatus(Context context, TripRecord tripRecord) {
        switch (engineStatus) {
            case UNKNOWN:
            case STOPPED:
                if (tripRecord.getEngineRpm() > 0) {
                    if (EngineRunningStatus.STOPPED.equals(engineStatus))
                        MultimediaUtils.playSound(context, MultimediaUtils.SoundFile.ENGINE_SWITCHED_ON);
                    engineStatus = EngineRunningStatus.RUNNING;
                    engineStartedOn = new Date();
                }
                break;

            case RUNNING:
                if (tripRecord.getEngineRpm() <= 0) {
                    MultimediaUtils.playSound(context, MultimediaUtils.SoundFile.ENGINE_SWITCHED_OFF);
                    engineStatus = EngineRunningStatus.STOPPED;
                    engineStoppedOn = new Date();
                }
                break;
        }
    }

    public static long engineRunningDurationSeconds() {
        if (EngineRunningStatus.RUNNING.equals(engineStatus))
            return DateUtils.diffInSeconds(engineStartedOn);
        return -1;
    }

    public static long engineOffDurationSeconds() {
        if (!EngineRunningStatus.RUNNING.equals(engineStatus))
            return DateUtils.diffInSeconds(engineStoppedOn);
        return -1;
    }

}
