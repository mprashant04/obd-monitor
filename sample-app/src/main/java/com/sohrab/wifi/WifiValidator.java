package com.sohrab.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;

import com.sohrab.obd.reader.common.AppConfig;
import com.sohrab.obd.reader.util.DateUtils;
import com.sohrab.obd.reader.util.Logs;
import com.sohrab.obd.reader.util.MultimediaUtils;

import java.util.Date;
import java.util.List;

import static com.sohrab.wifi.WifiScanner.LOG_PREFIX;

public class WifiValidator {

//    private Date lastValidatedOn = DateUtils.addHours(new Date(), -1);
//    private static final int VALIDATE_FREQUENCY_SECONDS = 10;

    private Date lastOnlineTime_front = DateUtils.addHours(new Date(), -5);
    private Date lastOnlineTime_rear = DateUtils.addHours(new Date(), -5);
    private boolean wasOnline = false;

    private Date lastOfflineNotificationTime = new Date();


    public void validate(Context context) {
        try {
            synchronized (this) {

                if (isOnlineFront() && isOnlineRear()) {
                    //----------- Both Online ---------------------------------------------------
                    if (!wasOnline)
                        playNotification(context, MultimediaUtils.SoundFile.DM_OK, "Dashcams ok", MultimediaUtils.LogLevel.INFO);
                    wasOnline = true;

                } else {
                    //----------- Either/both offline  -------------------------------------------
                    wasOnline = false;
                    if (DateUtils.diffInSeconds(lastOfflineNotificationTime) > AppConfig.getDmAlertIntervalSeconds()) {
                        lastOfflineNotificationTime = new Date();
                        if (!isOnlineFront() && !isOnlineRear())
                            playNotification(context, MultimediaUtils.SoundFile.DM_FAILED_BOTH, "Both dashcams offline", MultimediaUtils.LogLevel.WARN);
                        else if (!isOnlineFront())
                            playNotification(context, MultimediaUtils.SoundFile.DM_FAILED_FRONT, "Front dashcam offline", MultimediaUtils.LogLevel.WARN);
                        else if (!isOnlineRear())
                            playNotification(context, MultimediaUtils.SoundFile.DM_FAILED_REAR, "Rear dashcam offline", MultimediaUtils.LogLevel.WARN);
                    }
                }

            }
        } catch (Throwable ex) {
            Logs.error(ex);
        }
    }

    private void playNotification(Context context, MultimediaUtils.SoundFile soundFile, String logMsg, MultimediaUtils.LogLevel logLevel) {
        MultimediaUtils.playSound(context,
                soundFile,
                LOG_PREFIX + logMsg + " (" + getStatusSumamryString() + ")",
                logLevel);
    }

    private String getStatusSumamryString() {
        return DateUtils.diffInSeconds(lastOnlineTime_front) + "/" + DateUtils.diffInSeconds(lastOnlineTime_rear);
    }


    private boolean isOnlineFront() {
        return isOnline(lastOnlineTime_front, AppConfig.getDmSsidFront());
    }

    private boolean isOnlineRear() {
        return isOnline(lastOnlineTime_rear, AppConfig.getDmSsidRear());
    }

    private boolean isOnline(Date onlineDate, String ssid) {
        return (DateUtils.diffInSeconds(onlineDate) < AppConfig.getDmSsidMarkOfflineAfterSeconds());
    }


    public void update(List<ScanResult> results) {
        try {
            synchronized (this) {

                for (ScanResult r : results) {
                    lastOnlineTime_front = checkIfOnline(r, AppConfig.getDmSsidFront(), lastOnlineTime_front);
                    lastOnlineTime_rear = checkIfOnline(r, AppConfig.getDmSsidRear(), lastOnlineTime_rear);
                }

                String ssids = "";
                for (ScanResult r : results) ssids += r.SSID + ", ";
                Logs.info(LOG_PREFIX + "Updated (" + getStatusSumamryString() + ") (" + ssids + ")");

            }
        } catch (Throwable ex) {
            Logs.error(ex);
        }
    }

    private Date checkIfOnline(ScanResult r, String dashcamSsid, Date lastOnlineTime) {
        if (r.SSID != null && r.SSID.trim().equalsIgnoreCase(dashcamSsid))
            return new Date();

        return lastOnlineTime;
    }
}
