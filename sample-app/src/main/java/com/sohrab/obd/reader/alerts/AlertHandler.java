package com.sohrab.obd.reader.alerts;

import android.content.Context;
import android.content.Intent;

import com.sohrab.obd.reader.common.AppConfig;
import com.sohrab.obd.reader.obd.VehicleStatus;
import com.sohrab.obd.reader.util.DateUtils;
import com.sohrab.obd.reader.util.Logs;
import com.sohrab.obd.reader.util.MultimediaUtils;

import java.util.Date;

public class AlertHandler {


    private static Date lastAlertOn = DateUtils.addHours(new Date(), -1);
    private static Date speedWentBelowAlertLevelOn = DateUtils.addHours(new Date(), -1);
    private static Date lastHealthStatusSentToTaskerOn = DateUtils.addHours(new Date(), -1);

    private static boolean coolantOptimalTemperatureReached = false;
    private static boolean speedAboveLimit = false;
    private static boolean alertTriggered = false;
    private static MultimediaUtils.SoundFile alertSoundFilename = null;
    private static String alertMessage = "";

    private static final int WAIT_AFTER_ENGINE_START = 5;
    private static final int WAIT_AFTER_ENGINE_START_FOR_COOLANT_TEMP_OPTIMAL_ALERT = WAIT_AFTER_ENGINE_START + 25;


    public static synchronized void handle(Context context) {
        try {
            if (VehicleStatus.engineRunningDurationSeconds() > WAIT_AFTER_ENGINE_START) {  //start alerting only after few seconds after things stabilize
                alertHighSpeed(context);
                alertOptimalCoolantTemperature(context);

                alertReset();
                alertCheck(VehicleStatus.getCoolantTemperature() >= AppConfig.getCoolantAlertTemperature(), MultimediaUtils.SoundFile.ALERT_HIGH_COOLANT_TEMP, "Coolant temperature alert - " + VehicleStatus.getCoolantTemperature());
                alertCheck(VehicleStatus.getBatteryVoltage() <= AppConfig.getLowVoltageAlertLimit(), MultimediaUtils.SoundFile.ALERT_LOW_VOLTAGE, "Voltage low alert - " + VehicleStatus.getBatteryVoltage());
                alertShow(context);

                sendHealthStatusToTasker(context);   //OK health status only after all success, hence no try-catch blocks in above sub methods
            }
        } catch (Throwable ex) {
            Logs.error(ex);
        }
    }

    private static void alertOptimalCoolantTemperature(Context context) {
        if (!coolantOptimalTemperatureReached) {
            if (VehicleStatus.getCoolantTemperature() >= AppConfig.getCoolantOptimalTemperature()) {
                coolantOptimalTemperatureReached = true;
                if (VehicleStatus.engineRunningDurationSeconds() > WAIT_AFTER_ENGINE_START_FOR_COOLANT_TEMP_OPTIMAL_ALERT) {  //if engine was already hot on startup, don't show this alert.
                    MultimediaUtils.playSound(context, MultimediaUtils.SoundFile.ALERT_OPTIMAL_COOLANT_TEMP, "Optimal coolant temperature reached  " + VehicleStatus.getCoolantTemperature() + " C");
                }
            }
        }
    }


    private static void alertHighSpeed(Context context) {
        if (!speedAboveLimit && VehicleStatus.getSpeed() >= AppConfig.getHighSpeedAlertKmpl()) {
            speedAboveLimit = true;

            //speed need to stay below alert level for configured number of seconds, to trigger next alert
            if (DateUtils.diffInSeconds(speedWentBelowAlertLevelOn) > AppConfig.getHighSpeedAlertIntervalSeconds()) {
                MultimediaUtils.playSound(context, MultimediaUtils.SoundFile.ALERT_SPEED, "High speed  " + VehicleStatus.getSpeed() + " km/h");
            }

        } else if (speedAboveLimit && VehicleStatus.getSpeed() < AppConfig.getHighSpeedAlertKmpl()) {
            speedAboveLimit = false;
            speedWentBelowAlertLevelOn = new Date();
        }
    }

    private static void alertShow(Context context) {
        if (alertTriggered && DateUtils.diffInSeconds(lastAlertOn) > AppConfig.getAlertIntervalSeconds()) {
            MultimediaUtils.playSound(context, alertSoundFilename, alertMessage, MultimediaUtils.LogLevel.WARN);
            lastAlertOn = new Date();
        }
    }

    private static void alertCheck(boolean alertCondition, MultimediaUtils.SoundFile soundFileName, String logMessage) {
        if (alertCondition) {
            if (alertMessage.length() > 0) alertMessage += ",   ";
            alertMessage += logMessage;

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
        alertMessage = "";
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
