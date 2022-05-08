package com.sohrab.obd.reader.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import static android.media.AudioManager.STREAM_RING;

public class MultimediaUtils {
    private static MediaPlayer player = null;

    public static synchronized void playSound(Context ctx, String filename) {
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
                    Uri.parse("/storage/emulated/0/_My/Obd-Monitor/resources-audio/" + filename),
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

    private static void stopPlayer() {
        try {
            if (player != null) {
                player.stop();
            }
        } catch (Throwable e) {
            Logs.error(e);
        }
    }
}
