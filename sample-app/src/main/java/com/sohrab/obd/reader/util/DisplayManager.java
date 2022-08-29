package com.sohrab.obd.reader.util;


import android.content.Context;
import android.os.PowerManager;

public class DisplayManager {

    public static synchronized void wakeupScreen(Context context, int timeout) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //powerManager.isInteractive()
        //wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");

        PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyApp::MyLock");
        wl.acquire(timeout);
        PowerManager.WakeLock wl_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyCpuLock");
        wl_cpu.acquire(timeout);
    }

}
