package com.googlecode.talkmyphone.actions;

import com.googlecode.talkmyphone.geo.GeoManager;

import android.content.Intent;

public class StopLocatingPhoneAction extends Action {

    @Override
    public void execute(Intent intent) {
        GeoManager.stopLocatingPhone();
    }

}
