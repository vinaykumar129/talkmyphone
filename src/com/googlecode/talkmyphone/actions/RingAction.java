package com.googlecode.talkmyphone.actions;

import com.googlecode.talkmyphone.XmppService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.Settings;

public class RingAction extends Action {

    private static MediaPlayer mMediaPlayer;

    /** Retrieves the ringtone from the options
     * @param context Context to get the preferences from
     */
    private String getRingtoneFromPreferences(Context context) {
        String res = "";
        SharedPreferences prefs = context.getSharedPreferences("TalkMyPhone", 0);
        String ringtone = prefs.getString("ringtone", "");
        if (ringtone.equals("")) {
            ringtone = Settings.System.DEFAULT_RINGTONE_URI.toString();
        }
        return res;
    }

    /** builds a new player
     *
     * @param context context to build the player in
     * @param ringtone ringtone to set in the player
     */
    private void buildNewPlayer(Context context, String ringtone) {
        Uri ringtoneUri = Uri.parse(ringtone);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(context, ringtoneUri);
        } catch (Exception e) {
            XmppService.getInstance().send(e.toString());
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        mMediaPlayer.setLooping(true);
    }

    /**
     * Destroys any previous instance of the player
     */
    public static void destroyPreviousPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        mMediaPlayer = null;
    }

    private void play(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            try {
                mMediaPlayer.prepare();
            } catch (Exception e) {
                XmppService.getInstance().send(e.toString());
            }
            mMediaPlayer.start();
        }
    }

    @Override
    public void execute(Context context, Intent intent) {
        // first, destroy previous player
        destroyPreviousPlayer();

        // Get the ringtone
        String ringtone = getRingtoneFromPreferences(context);
        if (ringtone.equals("")) {
            XmppService.getInstance().send("Unable to ring, change the ringtone in the options");
        } else {
            XmppService.getInstance().send("Ringing phone");
            buildNewPlayer(context, ringtone);
            play(context);
        }
    }
}
