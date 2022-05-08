package com.sohrab.obd.reader.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.sohrab.obd.reader.common.Declarations;

import java.io.File;

import static android.media.AudioManager.STREAM_RING;

public class MultimediaUtils {

    public enum SoundFile {
        ENGINE_SWITCHED_OFF("engine-off.mp3"),
        APP_STARTED("app-started.mp3"),
        OBD_DEVICE_CONNECTED("obd-device-connected.mp3"),
        OBD_DEVICE_DISCONNECTED("obd-device-disconnected.mp3"),
        APP_CLOSING("app-closing.mp3"),
        ALERT_MULTIPLE("multiple-alerts.mp3"),
        ALERT_HIGH_COOLANT_TEMP("high-engine-temp.mp3"),
        ALERT_LOW_VOLTAGE("low-voltage.mp3"),
        ALERT_SPEED("speed-alert.mp3"),
        ALERT_OPTIMAL_COOLANT_TEMP("optimal-coolant-temp.mp3");

        private String fileName = "";

        SoundFile(String s) {
            this.fileName = s;
        }

        public String getFileName() {
            return fileName;
        }
    }


    private static MediaPlayer player = null;

    public static synchronized void playSound(Context ctx, SoundFile file) {
        AssetManager am;
        try {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setLegacyStreamType(STREAM_RING)
                    .build();

            AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

            stopPlayer();

            player = MediaPlayer.create(ctx,
                    Uri.parse(buildFilePath(file)),
                    null,
                    audioAttributes,
                    audioManager.generateAudioSessionId());
            player.setLooping(false);

            player.start();
//            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    mp.release();
//                }
//            });


        } catch (Throwable e) {
            Logs.error(e);
        }
    }

    private static String buildFilePath(SoundFile file) {
        return Declarations.ROOT_SD_FOLDER_PATH + "/resources-audio/" + file.getFileName();
    }

    private static void stopPlayer() {
        try {
            if (player != null) {
                player.stop();
            }
        } catch (Throwable e) {
            Logs.error(e);
        }
    }

    public static void checkIfAllSoundFilesPresent(Context ctx) {
        try {
            String missingFiles = "";
            boolean failed = false;
            for (SoundFile s : SoundFile.values()) {
                if (!validateSoundFile(s)) {
                    failed = true;
                    missingFiles += "\n - " + s.getFileName();
                }
            }

            if (failed) {
                DialogUtils.alertDialog(ctx, "Missing sound files...", "Following sound files are missing..." + missingFiles);
            }

        } catch (Throwable e) {
            Logs.error(e);
            DialogUtils.alertDialog(ctx, "Error", "Error while validating sound files...");
        }
    }

    private static boolean validateSoundFile(SoundFile s) {
        return new File(buildFilePath(s)).exists();
    }

    public static void testAllSoundFiles(Context ctx) {

        for (SoundFile s : SoundFile.values()) {
            playSound(ctx, s);
            Utils.delay(5000);
        }

        System.exit(0);

    }
}
