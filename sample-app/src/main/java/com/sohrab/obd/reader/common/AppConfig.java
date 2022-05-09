package com.sohrab.obd.reader.common;

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

    public static int getCoolantOptimalTemperature() {
        return Integer.parseInt(props.getProperty("coolant-optimal-temperature", "9999"));
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

    public static int getAutoTerminateAfterEngineOffSeconds() {
        return Integer.parseInt(props.getProperty("auto-terminate-after-engine-off-seconds", "9999"));
    }

    public static int getOffsetSpeed() {
        return Integer.parseInt(props.getProperty("offset-speed", "0"));
    }

    public static int getOffsetCoolantTemperature() {
        return Integer.parseInt(props.getProperty("offset-coolant-temperature", "0"));
    }

}
