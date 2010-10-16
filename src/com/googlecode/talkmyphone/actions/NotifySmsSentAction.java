package com.googlecode.talkmyphone.actions;

import com.googlecode.talkmyphone.XmppService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.gsm.SmsManager;

public class NotifySmsSentAction extends Action {

    @Override
    public void execute(Context context, Intent intent) {
        switch (intent.getIntExtra("ResultCode", Activity.RESULT_OK))
        {
            case Activity.RESULT_OK:
                XmppService.getInstance().send("SMS sent");
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                XmppService.getInstance().send("Generic failure");
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                XmppService.getInstance().send("No service");
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                XmppService.getInstance().send("Null PDU");
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                XmppService.getInstance().send("Radio off");
                break;
        }
    }

}
