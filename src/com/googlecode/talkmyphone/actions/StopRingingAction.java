package com.googlecode.talkmyphone.actions;

import android.content.Intent;

public class StopRingingAction extends Action {

    @Override
    public void execute(Intent intent) {
        RingAction.destroyPreviousPlayer();
    }

}
