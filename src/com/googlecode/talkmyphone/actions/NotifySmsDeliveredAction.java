package com.googlecode.talkmyphone.actions;

import com.googlecode.talkmyphone.XmppService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class NotifySmsDeliveredAction extends Action {

    @Override
    public void execute(Context context, Intent intent) {
        switch (intent.getIntExtra("ResultCode", Activity.RESULT_OK))
        {
            case Activity.RESULT_OK:
                XmppService.getInstance().send("SMS delivered");
                break;
            case Activity.RESULT_CANCELED:
                XmppService.getInstance().send("SMS not delivered");
                break;
        }
    }

}
