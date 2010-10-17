package com.googlecode.talkmyphone.actions;

import com.googlecode.talkmyphone.LocationService;
import android.content.Context;
import android.content.Intent;

public class StopLocatingPhoneAction extends Action {
	
	private Context mContext;
	
	public StopLocatingPhoneAction(Context context) {
		mContext = context;
	}

    @Override
    public void execute(Intent intent) {
        Intent i = new Intent(mContext, LocationService.class);
        i.setAction(LocationService.STOP_SERVICE);
        mContext.startService(i);
    }

}
