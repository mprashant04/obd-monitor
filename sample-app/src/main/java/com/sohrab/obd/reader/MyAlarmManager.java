package com.sohrab.obd.reader;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sohrab.obd.reader.common.AppAutoTerminate;
import com.sohrab.obd.reader.util.Logs;

import java.util.Date;

public class MyAlarmManager extends BroadcastReceiver {
    private static final String LOG_PREFIX = "AlarmManager: ";

    private static boolean initiated = false;
    private static final long INTERVAL = 60 * 1000;  //looks like andoroid honours only min 1 min interval, so seeing alarm trigger every 1 minute

    //the method will be fired when the alarm is triggerred
    @Override
    public void onReceive(Context context, Intent intent) {
        process(context);
    }


    private static void process(Context context) {
        AppAutoTerminate.handle(context);
    }


    public static synchronized void init(Activity activity) {
        if (initiated) return;
        initiated = true;

        Logs.warn(LOG_PREFIX + "**** Init ****");

        AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(activity, MyAlarmManager.class);
        PendingIntent pi = PendingIntent.getBroadcast(activity, 0, i, 0);

        am.setRepeating(AlarmManager.RTC, new Date().getTime(), INTERVAL, pi);

        //process(activity); //first time processing after app start...
    }
}
