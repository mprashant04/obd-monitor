package com.sohrab.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.sohrab.obd.reader.common.AppConfig;
import com.sohrab.obd.reader.obd.VehicleStatus;
import com.sohrab.obd.reader.util.DateUtils;
import com.sohrab.obd.reader.util.DisplayManager;
import com.sohrab.obd.reader.util.Logs;
import com.sohrab.obd.reader.util.Utils;

import java.util.Date;
import java.util.List;

public class WifiScanner {
    protected static final String LOG_PREFIX = "&#128247";

    private WifiManager wifiManager = null;
    private Date lastScannedOn = null;
    private WifiValidator wifiValidator;


    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (success) {
                scanSuccess();
            } else {
                scanFailure();
            }
        }
    };

    private void reset() {
        if (null == wifiValidator) wifiValidator = new WifiValidator();

        lastScannedOn = DateUtils.addHours(new Date(), -5);
        wifiValidator.reset();
    }

    public void init(final Context context) {
        try {
            reset();
            if (!AppConfig.isDmEnabled())
                return;

            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);


            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            context.registerReceiver(wifiScanReceiver, intentFilter);


            (new Thread() {
                public void run() {
                    while (true) {
                        if (VehicleStatus.isEngineRunning()) {
                            reScan(context);
                            wifiValidator.validate(context);
                        } else {
                            reset();
                        }
                        Utils.delay(2000);
                    }
                }
            }).start();

            //boolean throttleEnabled = wifiManager.isScanThrottleEnabled();

        } catch (Throwable ex) {
            Logs.error(ex);
        }
    }


    private void reScan(Context context) {
        try {
            if (DateUtils.diffInSeconds(lastScannedOn) < AppConfig.getDmWifiScanIntervalSeconds())
                return;

            lastScannedOn = new Date();

            //looks like wifi scanning resumes when display is awakened
            DisplayManager.wakeupScreen(context, AppConfig.getDmDisplayWakeupDuringScanSeconds() * 1000);
            Utils.delay(AppConfig.getDmDisplayWakeupDuringScanSeconds() * 100);

            // this is expected to be deprecated soon - https://stackoverflow.com/questions/56401057/wifimanager-startscan-deprecated-alternative
            boolean success = wifiManager.startScan();
            if (success)
                Logs.info(LOG_PREFIX + "scan triggered");
            else
                Logs.error(LOG_PREFIX + "scan trigger failed");

        } catch (Throwable ex) {
            Logs.error(ex);
        }
    }

    private synchronized void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();

        wifiValidator.update(results);
    }

    private void scanFailure() {
        Logs.error(LOG_PREFIX + "Scan failed");
    }
}

