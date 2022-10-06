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

    private Date lastOnlineTime_front = null;
    private Date lastOnlineTime_rear = null;
    private boolean wasOnline = false;
    private int offlineInterval = 0;

    private Date lastOfflineNotificationTime = null;

    public WifiValidator() {
        reset();
    }

    public void reset() {
        wasOnline = false;
        lastOfflineNotificationTime = new Date();

        lastOnlineTime_front = DateUtils.addHours(new Date(), -5);
        lastOnlineTime_rear = DateUtils.addHours(new Date(), -5);

        offlineInterval = AppConfig.getDmSsidMarkOfflineAfterSeconds();
    }


    public void validate(Context context) {
        try {
            synchronized (this) {

                if (isOnlineFront() && isOnlineRear()) {
                    //----------- Both Online ---------------------------------------------------
                    if (!wasOnline)
                        playNotification(context, MultimediaUtils.SoundFile.DM_OK, "Dashcams ok" + logSuffix(), MultimediaUtils.LogLevel.INFO);
                    wasOnline = true;

                    offlineInterval = AppConfig.getDmSsidMarkOfflineAfterSeconds() * AppConfig.getDmSsidMarkOfflineAfterMultiplier();  //increment to longer interval after cameras found online after car start

                } else {
                    //----------- Either/both offline  -------------------------------------------
                    wasOnline = false;
                    if (DateUtils.diffInSeconds(lastOfflineNotificationTime) > AppConfig.getDmAlertIntervalSeconds()) {
                        lastOfflineNotificationTime = new Date();
                        if (!isOnlineFront() && !isOnlineRear())
                            playNotification(context, MultimediaUtils.SoundFile.DM_FAILED_BOTH, "Both dashcams offline" + logSuffix(), MultimediaUtils.LogLevel.WARN);
                        else if (!isOnlineFront())
                            playNotification(context, MultimediaUtils.SoundFile.DM_FAILED_FRONT, "Front dashcam offline" + logSuffix(), MultimediaUtils.LogLevel.WARN);
                        else if (!isOnlineRear())
                            playNotification(context, MultimediaUtils.SoundFile.DM_FAILED_REAR, "Rear dashcam offline" + logSuffix(), MultimediaUtils.LogLevel.WARN);
                    }
                }

            }
        } catch (Throwable ex) {
            Logs.error(ex);
        }
    }

    private String logSuffix(){
        return " (" + offlineInterval + ")";
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
        return (DateUtils.diffInSeconds(onlineDate) < offlineInterval);
    }


    public void update(List<ScanResult> results) {
        try {
            synchronized (this) {

                String oldStatusSummary = getStatusSumamryString();

                for (ScanResult r : results) {
                    lastOnlineTime_front = checkIfOnline(r, AppConfig.getDmSsidFront(), lastOnlineTime_front);
                    lastOnlineTime_rear = checkIfOnline(r, AppConfig.getDmSsidRear(), lastOnlineTime_rear);
                }

                String ssids = "";
                for (ScanResult r : results) ssids += r.SSID + ", ";
                Logs.info(LOG_PREFIX + "Updated (" + oldStatusSummary + ")->(" + getStatusSumamryString() + ") (" + ssids + ")");

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
