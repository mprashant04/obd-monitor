package com.sohrab.obd.reader.common;

import com.sohrab.obd.reader.common.Declarations;
import com.sohrab.obd.reader.util.Logs;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class AppConfig {
    private static Properties props = null;

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
        return Integer.parseInt(props.getProperty("coolant-alert-temperature", "9999"));
    }

    public static float getLowVoltageAlertLimit() {
        return Float.parseFloat(props.getProperty("voltage-alert", "-9999"));
    }

    public static int getAlertIntervalSeconds() {
        return Integer.parseInt(props.getProperty("alert-interval-seconds", "9999"));
    }

    public static int getHighSpeedAlertKmpl() {
        return Integer.parseInt(props.getProperty("high-speed-alert", "9999"));
    }

    public static int getHighSpeedAlertIntervalSeconds() {
        return Integer.parseInt(props.getProperty("high-speed-alert-interval-seconds", "9999"));
    }

    public static int getTaskerHealthStatusIntervalSeconds() {
        return Integer.parseInt(props.getProperty("health-status-to-tasker-interval-seconds", "9999"));
    }

    public static int getAutoTerminateWhenNoDataSeconds() {
        return Integer.parseInt(props.getProperty("auto-terminate-when-no-data-seconds", "9999"));
    }

}
