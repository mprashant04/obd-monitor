package com.sohrab.obd.reader.common;

import android.content.Context;

import com.sohrab.obd.reader.obd.ObdStatus;
import com.sohrab.obd.reader.util.Logs;
import com.sohrab.obd.reader.util.MultimediaUtils;
import com.sohrab.obd.reader.util.Utils;

public class AppAutoTerminate {
    public static void handle(Context context) {
        if (ObdStatus.getLatestDataReceivedSinceSeconds() > 5) {
            Logs.info("No data received since " + ObdStatus.getLatestDataReceivedSinceSeconds() + " sec");
            if (ObdStatus.getLatestDataReceivedSinceSeconds() > AppConfig.getAutoTerminateWhenNoDataSeconds()) {
                Logs.info("Terminating app....");
                MultimediaUtils.playSound(context, MultimediaUtils.SoundFile.APP_CLOSING);
                Utils.delay(5000);
                System.exit(0);
            }
        }
    }
}
