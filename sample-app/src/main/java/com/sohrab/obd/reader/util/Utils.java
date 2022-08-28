package com.sohrab.obd.reader.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.sohrab.obd.reader.common.AppAutoTerminate;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.POWER_SERVICE;

public class Utils {
    public static void delay(int milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
        }
    }


    public static synchronized boolean isBatteryOptimizationEnabled(Context context) {
        Intent intent = new Intent();
        String packageName = context.getPackageName();
        PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
        return !pm.isIgnoringBatteryOptimizations(packageName);
    }

    public static void checkExternalStorageAccess(Context context) {
        if (Environment.isExternalStorageManager()) {
            //todo when permission is granted
        } else {
            //request for the permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
        }
    }

    public static boolean checkRequiredPermissions(final Activity context) {

        //give permissions one at time, there is specific dependency on giving location dependencys
        if (givePermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION))
            if (givePermission(context, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                return true;

        Logs.error("Terminating app....");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                AppAutoTerminate.terminate((AppCompatActivity) context);
            }
        }, 1000 * 1);

        return false;
    }

    private static boolean givePermission(final Activity context, String permissionName) {
        if (ActivityCompat.checkSelfPermission(context, permissionName) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{permissionName}, 1);
            return false;
        }
        return true;
    }
}
