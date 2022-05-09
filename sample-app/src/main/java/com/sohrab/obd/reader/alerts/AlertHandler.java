package com.sohrab.obd.reader.alerts;

import android.content.Context;
import android.content.Intent;

import com.sohrab.obd.reader.common.AppConfig;
import com.sohrab.obd.reader.common.Declarations;
import com.sohrab.obd.reader.trip.TripRecord;
import com.sohrab.obd.reader.util.DateUtils;
import com.sohrab.obd.reader.util.Logs;
import com.sohrab.obd.reader.util.MultimediaUtils;

import java.util.Date;

public class AlertHandler {
    private static Date lastAlertOn = DateUtils.addHours(new Date(), -1);
    private static Date lastSpeedAlertOn = DateUtils.addHours(new Date(), -1);
    private static Date lastHealthStatusSentToTaskerOn = DateUtils.addHours(new Date(), -1);
    private static Date instanciatedOn = new Date();

    private static boolean coolantOptimalTemperatureReached = false;
    private static boolean engineRunning = false;
    private static boolean speedAboveLimit = false;
    private static boolean alertTriggered = false;
    private static MultimediaUtils.SoundFile alertSoundFilename = null;


    public static synchronized void handle(Context context, TripRecord tripRecord) {
        try {
            if (DateUtils.diffInSeconds(instanciatedOn) < 5)
                return;  //skip alerts for first few seconds, to prevent false alerts

            float coolantTemp = tripRecord.getmEngineCoolantTemp();
            double voltage = tripRecord.getmControlModuleVoltageValue();

            alertEngineSwitchOff(context, tripRecord);

            if (engineRunning) {
                alertHighSpeed(context, tripRecord);
                alertOptimalCoolantTemperature(context, tripRecord);

                alertReset();
                alertCheck(coolantTemp >= AppConfig.getCoolantAlertTemperature(), MultimediaUtils.SoundFile.ALERT_HIGH_COOLANT_TEMP, "Coolant temperature alert - " + coolantTemp);
                alertCheck(voltage <= AppConfig.getLowVoltageAlertLimit(), MultimediaUtils.SoundFile.ALERT_LOW_VOLTAGE, "Voltage low alert - " + voltage);
                alertShow(context);

                sendHealthStatusToTasker(context);   //OK health status only after all success, hence no try-catch blocks in above sub methods
            }
        } catch (Throwable ex) {
            Logs.error(ex);
        }
    }

    private static void alertOptimalCoolantTemperature(Context context, TripRecord tripRecord) {
        if (!coolantOptimalTemperatureReached) {
            if (tripRecord.getmEngineCoolantTemp() >= AppConfig.getCoolantOptimalTemperature()) {
                coolantOptimalTemperatureReached = true;
                MultimediaUtils.playSound(context, MultimediaUtils.SoundFile.ALERT_OPTIMAL_COOLANT_TEMP);
                Logs.info(Declarations.BELL_CHAR_HTML + " Optimal coolant temperature reached - " + tripRecord.getmEngineCoolantTemp() + " C");
            }
        }
    }

    private static void alertEngineSwitchOff(Context context, TripRecord tripRecord) {
        if (engineRunning && tripRecord.getEngineRpm() <= 0) {
            engineRunning = false;
            MultimediaUtils.playSound(context, MultimediaUtils.SoundFile.ENGINE_SWITCHED_OFF);
        } else if (!engineRunning && tripRecord.getEngineRpm() > 0) {
            engineRunning = true;
        }
    }

    private static void alertHighSpeed(Context context, TripRecord tripRecord) {
        int speed = tripRecord.getSpeed();
        if (!speedAboveLimit && speed >= AppConfig.getHighSpeedAlertKmpl()) {
            speedAboveLimit = true;

            if (DateUtils.diffInSeconds(lastSpeedAlertOn) > AppConfig.getHighSpeedAlertIntervalSeconds()) {
                MultimediaUtils.playSound(context, MultimediaUtils.SoundFile.ALERT_SPEED);
                lastSpeedAlertOn = new Date();
            }

        } else if (speedAboveLimit && speed < AppConfig.getHighSpeedAlertKmpl()) {
            speedAboveLimit = false;
        }
    }

    private static void alertShow(Context context) {
        if (alertTriggered && DateUtils.diffInSeconds(lastAlertOn) > AppConfig.getAlertIntervalSeconds()) {
            MultimediaUtils.playSound(context, alertSoundFilename);
            lastAlertOn = new Date();
        }
    }

    private static void alertCheck(boolean alertCondition, MultimediaUtils.SoundFile soundFileName, String logMessage) {
        if (alertCondition) {
            Logs.warn(Declarations.BELL_CHAR_HTML + " " + logMessage);

            if (!alertTriggered) {
                alertTriggered = true;
                alertSoundFilename = soundFileName;
            } else {
                alertSoundFilename = MultimediaUtils.SoundFile.ALERT_MULTIPLE;
            }
        }
    }

    private static void alertReset() {
        alertTriggered = false;
    }


    public static void sendHealthStatusToTasker(Context context) {
        try {
            if (DateUtils.diffInSeconds(lastHealthStatusSentToTaskerOn) > AppConfig.getTaskerHealthStatusIntervalSeconds()) {
                Intent intent = new Intent("com.sohrab.obd.reader.connection_ok");
                intent.putExtra("status", "ok");
                //intent.putExtra("title", title);
                context.sendBroadcast(intent);
                lastHealthStatusSentToTaskerOn = new Date();

                Logs.info("Tasker health OK status sent");
            }
        } catch (Throwable ex) {
            Logs.error(ex);
        }
    }


}
