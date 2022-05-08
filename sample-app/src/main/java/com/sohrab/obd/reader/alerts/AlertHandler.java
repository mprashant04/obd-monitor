package com.sohrab.obd.reader.alerts;

import android.content.Context;

import com.sohrab.obd.reader.trip.TripRecord;
import com.sohrab.obd.reader.util.MultimediaUtils;

public class AlertHandler {
    public static void checkCoolantTemp(Context context, TripRecord tripRecord) {
        float temp = tripRecord.getmEngineCoolantTempValue();
        if (temp > 10) {
            MultimediaUtils.playSound(context, "high-engine-temp.mp3");
        }
    }
}
