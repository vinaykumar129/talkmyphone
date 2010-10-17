package com.googlecode.talkmyphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class CallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("TalkMyPhone", 0);
        boolean notifyIncomingCalls = prefs.getBoolean("notifyIncomingCalls", false);

        if (notifyIncomingCalls) {
            PhoneStateListener phoneListener = new PhoneCallListener(context);
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }
}
