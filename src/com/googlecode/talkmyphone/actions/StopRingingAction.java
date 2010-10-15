package com.googlecode.talkmyphone.actions;

import android.content.Context;
import android.content.Intent;

public class StopRingingAction extends Action {

    @Override
    public void execute(Context context, Intent intent) {
        RingAction.destroyPreviousPlayer();
    }

}
