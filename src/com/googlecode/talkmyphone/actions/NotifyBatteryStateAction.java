package com.googlecode.talkmyphone.actions;

import com.googlecode.talkmyphone.XmppService;

import android.content.Context;
import android.content.Intent;

public class NotifyBatteryStateAction extends Action {
    private int lastPercentageNotified = -1;
    @Override
    public void execute(Context context, Intent intent) {
        int level = intent.getIntExtra("level", 0);
        if (lastPercentageNotified == -1) {
            notifyAndSavePercentage(level);
        } else {
            if (level != lastPercentageNotified && level % 5 == 0) {
                notifyAndSavePercentage(level);
            }
        }
    }
    private void notifyAndSavePercentage(int level) {
        XmppService.getInstance().send("Battery level " + level + "%");
        lastPercentageNotified = level;
    }
}
