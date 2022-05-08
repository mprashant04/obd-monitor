package com.sohrab.obd.reader.common;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.sohrab.obd.reader.obd.ObdStatus;
import com.sohrab.obd.reader.util.Logs;
import com.sohrab.obd.reader.util.MultimediaUtils;
import com.sohrab.obd.reader.util.Utils;

public class AppAutoTerminate {
    private static void handle(AppCompatActivity activity) {
        Logs.info("Auto terminate handling.... engine off since " + ObdStatus.getEngineOffSinceSeconds() + " sec");

        if (ObdStatus.getEngineOffSinceSeconds() > AppConfig.getAutoTerminateAfterEngineOffSeconds()) {
            Logs.info("Terminating app....");
            MultimediaUtils.playSound(activity, MultimediaUtils.SoundFile.APP_CLOSING);
            Utils.delay(5000);

            activity.finishAffinity();
            activity.finishAndRemoveTask();
            Utils.delay(5000);

            System.exit(0);
        }
    }

    public static void init(final AppCompatActivity activity) {
//        new Handler().postDelayed(new Runnable() {
//            public void run() {
//                handle(activity);
//            }
//        }, 1000 * 10);


        final Handler m_Handler = new Handler();
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                handle(activity);
                m_Handler.postDelayed(this, 1000 * 30);// move this inside the run method
            }
        };
        mRunnable.run();
    }

}
