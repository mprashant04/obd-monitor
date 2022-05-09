package com.sohrab.obd.reader.common;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.sohrab.obd.reader.obd.VehicleStatus;
import com.sohrab.obd.reader.util.Logs;

public class AppAutoTerminate {
    private static void handle(AppCompatActivity activity) {
        if (VehicleStatus.isEngineRunning()) return;

        Logs.info("Auto terminate handling.... engine off since " + VehicleStatus.engineOffDurationSeconds() + " sec");
        if (VehicleStatus.engineOffDurationSeconds() > AppConfig.getAutoTerminateAfterEngineOffSeconds()) {
            Logs.info("Terminating app....");
            //MultimediaUtils.playSound(activity, MultimediaUtils.SoundFile.APP_CLOSING);
            //Utils.delay(5000);

            activity.finishAffinity();
            activity.finishAndRemoveTask();
            //Utils.delay(5000);

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
