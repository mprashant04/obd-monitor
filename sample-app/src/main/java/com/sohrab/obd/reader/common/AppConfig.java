package com.sohrab.obd.reader.common;

import android.support.v7.app.AppCompatActivity;

import com.sohrab.obd.reader.util.DialogUtils;
import com.sohrab.obd.reader.util.Logs;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class AppConfig {
    private static Properties props = null;

    private enum SettingNames {
        ALERT_REPEAT_INTERVAL_SECONDS("alert-repeat-interval-seconds"),
        AUTO_TERMINATE_AFTER_ENGINE_OFF_SECONDS("auto-terminate-after-engine-off-seconds"),
        COOLANT_ALERT_TEMPERATURE("coolant-alert-temperature"),
        COOLANT_OPTIMAL_TEMPERATURE("coolant-optimal-temperature"),
        HEALTH_STATUS_TO_TASKER_INTERVAL_SECONDS("health-status-to-tasker-interval-seconds"),
        HIGH_SPEED_ALERT_INTERVAL_SECONDS("high-speed-alert-interval-seconds"),
        HIGH_SPEED_ALERT_KMPH("high-speed-alert-kmph"),
        OFFSET_COOLANT_TEMPERATURE("offset-coolant-temperature"),
        OFFSET_SPEED("offset-speed"),
        OBD_STATS_LOGGING_INTERVAL_SECONDS("obd-stats-logging-interval-seconds"),
        VOLTAGE_ALERT("voltage-alert"),

        //--------- DashCam monitoring settings -------------------
        DM_ENABLED("dm-enabled"),
        DM_SSID_FRONT("dm-ssid-front"),
        DM_SSID_REAR("dm-ssid-rear"),
        DM_WIFI_SCAN_INTERVAL_SECONDS("dm-wifi-scan-interval-seconds"),
        DM_DISPLAY_WAKEUP_DURING_SCAN_SECONDS("dm-display-wakeup-during-scan-seconds"),
        DM_SSID_MARK_OFFLINE_AFTER_SECONDS("dm-ssid-mark-offline-after-seconds"),
        DM_SSID_MARK_OFFLINE_AFTER_MULTIPLIER("dm-ssid-mark-offline-after-multiplier"),
        DM_ALERT_INTERVAL_SECONDS("dm-alert-interval-seconds");

        private String value = "";

        SettingNames(String val) {
            this.value = val;
        }

        public String getValue() {
            return value;
        }
    }

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try {
            props = new Properties();
            props.load(new FileInputStream(new File(Declarations.ROOT_SD_FOLDER_PATH + "/conf.txt")));
            //Logs.error(props.toString());
        } catch (Throwable ex) {
            Logs.error(ex);
        }
    }

    public static int getCoolantAlertTemperature() {
        return Integer.parseInt(props.getProperty(SettingNames.COOLANT_ALERT_TEMPERATURE.getValue(), "99999"));
    }

    public static int getCoolantOptimalTemperature() {
        return Integer.parseInt(props.getProperty(SettingNames.COOLANT_OPTIMAL_TEMPERATURE.getValue(), "99999"));
    }

    public static float getLowVoltageAlertLimit() {
        return Float.parseFloat(props.getProperty(SettingNames.VOLTAGE_ALERT.getValue(), "-99999"));
    }

    public static int getAlertIntervalSeconds() {
        return Integer.parseInt(props.getProperty(SettingNames.ALERT_REPEAT_INTERVAL_SECONDS.getValue(), "99999"));
    }

    public static int getHighSpeedAlertKmpl() {
        return Integer.parseInt(props.getProperty(SettingNames.HIGH_SPEED_ALERT_KMPH.getValue(), "99999"));
    }

    public static int getHighSpeedAlertIntervalSeconds() {
        return Integer.parseInt(props.getProperty(SettingNames.HIGH_SPEED_ALERT_INTERVAL_SECONDS.getValue(), "99999"));
    }

    public static int getTaskerHealthStatusIntervalSeconds() {
        return Integer.parseInt(props.getProperty(SettingNames.HEALTH_STATUS_TO_TASKER_INTERVAL_SECONDS.getValue(), "99999"));
    }

    public static int getAutoTerminateAfterEngineOffSeconds() {
        return Integer.parseInt(props.getProperty(SettingNames.AUTO_TERMINATE_AFTER_ENGINE_OFF_SECONDS.getValue(), "99999"));
    }

    public static int getOffsetSpeed() {
        return Integer.parseInt(props.getProperty(SettingNames.OFFSET_SPEED.getValue(), "0"));
    }

    public static int getOffsetCoolantTemperature() {
        return Integer.parseInt(props.getProperty(SettingNames.OFFSET_COOLANT_TEMPERATURE.getValue(), "0"));
    }

    public static int getObdStatsLoggingIntervalSeconds() {
        return Integer.parseInt(props.getProperty(SettingNames.OBD_STATS_LOGGING_INTERVAL_SECONDS.getValue(), "99999"));
    }

    public static boolean isDmEnabled() {
        return Integer.parseInt(props.getProperty(SettingNames.DM_ENABLED.getValue(), "0")) == 1;
    }

    public static String getDmSsidFront() {
        return props.getProperty(SettingNames.DM_SSID_FRONT.getValue(), "").trim();
    }

    public static String getDmSsidRear() {
        return props.getProperty(SettingNames.DM_SSID_REAR.getValue(), "").trim();
    }

    public static int getDmSsidMarkOfflineAfterSeconds() {
        return Integer.parseInt(props.getProperty(SettingNames.DM_SSID_MARK_OFFLINE_AFTER_SECONDS.getValue(), "60"));
    }

    public static int getDmSsidMarkOfflineAfterMultiplier() {
        return Integer.parseInt(props.getProperty(SettingNames.DM_SSID_MARK_OFFLINE_AFTER_MULTIPLIER.getValue(), "2"));
    }

    public static int getDmWifiScanIntervalSeconds() {
        return Integer.parseInt(props.getProperty(SettingNames.DM_WIFI_SCAN_INTERVAL_SECONDS.getValue(), "60"));
    }

    public static int getDmDisplayWakeupDuringScanSeconds() {
        return Integer.parseInt(props.getProperty(SettingNames.DM_DISPLAY_WAKEUP_DURING_SCAN_SECONDS.getValue(), "5"));
    }

    public static int getDmAlertIntervalSeconds() {
        return Integer.parseInt(props.getProperty(SettingNames.DM_ALERT_INTERVAL_SECONDS.getValue(), "120"));
    }


    public static boolean validateIfAllConfigValuesPresent(final AppCompatActivity ctx) {
        String missingConf = "";
        boolean missing = false;
        for (SettingNames s : SettingNames.values()) {
            if (props.get(s.getValue()) == null) {
                missing = true;
                missingConf += "\n   - " + s.getValue();
            }
        }

        if (missing) {
            Logs.error("Missing config values.... terminating app" + missingConf);
            DialogUtils.alertDialog(ctx, "Missing Config!!!", "Following settings missing in config file, terminating app in few seconds....\n" + missingConf);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    AppAutoTerminate.terminate(ctx);
                }
            }, 1000 * 20);
        }

        return !missing;
    }

}
