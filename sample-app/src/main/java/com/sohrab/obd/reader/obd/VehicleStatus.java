package com.sohrab.obd.reader.obd;

import android.content.Context;

import com.sohrab.obd.reader.common.AppConfig;
import com.sohrab.obd.reader.common.Declarations;
import com.sohrab.obd.reader.trip.TripRecord;
import com.sohrab.obd.reader.util.DateUtils;
import com.sohrab.obd.reader.util.Logs;
import com.sohrab.obd.reader.util.MultimediaUtils;
import com.sohrab.obd.reader.util.StringUtils;

import java.util.Date;

public class VehicleStatus {
    private enum EngineRunningStatus {RUNNING, STOPPED, UNKNOWN}

    private static EngineRunningStatus engineStatus = EngineRunningStatus.UNKNOWN;
    private static Date engineStartedOn = new Date();
    private static Date engineStoppedOn = new Date();

    private static int coolantTemperature = 0;
    private static int speed = 0;
    private static double batteryVoltage = 0;

    private static Date lastLoggedOn = DateUtils.addHours(new Date(), -1);

    public static synchronized void update(Context context, TripRecord tripRecord) {
        updateEngineStatus(context, tripRecord);
        updateCoolantTemperature(context, tripRecord);
        updateBatteryVoltage(context, tripRecord);
        updateSpeed(context, tripRecord);

        log();
    }


    private static void updateSpeed(Context context, TripRecord tripRecord) {
        speed = tripRecord.getSpeed();
    }

    private static void updateCoolantTemperature(Context context, TripRecord tripRecord) {
        coolantTemperature = (int) tripRecord.getmEngineCoolantTemp();
    }

    private static void updateBatteryVoltage(Context context, TripRecord tripRecord) {
        batteryVoltage = tripRecord.getmControlModuleVoltageValue();
    }

    private static void updateEngineStatus(Context context, TripRecord tripRecord) {
        switch (engineStatus) {
            case UNKNOWN:
            case STOPPED:
                if (tripRecord.getEngineRpm() > 0) {
                    if (EngineRunningStatus.STOPPED.equals(engineStatus)) {
                        MultimediaUtils.playSound(context, MultimediaUtils.SoundFile.ENGINE_SWITCHED_ON,"Engine started");
                    }
                    engineStatus = EngineRunningStatus.RUNNING;
                    engineStartedOn = new Date();
                }
                break;

            case RUNNING:
                if (tripRecord.getEngineRpm() <= 0) {
                    MultimediaUtils.playSound(context, MultimediaUtils.SoundFile.ENGINE_SWITCHED_OFF, "Engine stopped");
                    engineStatus = EngineRunningStatus.STOPPED;
                    engineStoppedOn = new Date();
                }
                break;
        }
    }

    public static boolean isEngineRunning() {
        return EngineRunningStatus.RUNNING.equals(engineStatus);
    }

    public static long engineRunningDurationSeconds() {
        if (isEngineRunning())
            return DateUtils.diffInSeconds(engineStartedOn);
        return -1;
    }

    public static long engineOffDurationSeconds() {
        if (!isEngineRunning())
            return DateUtils.diffInSeconds(engineStoppedOn);
        return -1;
    }

    public static int getCoolantTemperature() {
        return coolantTemperature;
    }

    public static double getBatteryVoltage() {
        return batteryVoltage;
    }

    public static int getSpeed() {
        return speed;
    }

    private static void log() {
        if (DateUtils.diffInSeconds(lastLoggedOn) >= AppConfig.getObdStatsLoggingIntervalSeconds()) {
            lastLoggedOn = new Date();
            Logs.info(getStatus());
        }
    }

    public static String getStatus() {
        return "" + speed + " km/h,   "
                + "" + coolantTemperature + " °C,   "
                + "" + batteryVoltage + " V,   "
                + "engine-" + StringUtils.capitalizeFirstCharacter(engineStatus.toString()) + " (" + engineRunningDurationSeconds() + "/" + engineOffDurationSeconds() + "sec)";
    }
}
