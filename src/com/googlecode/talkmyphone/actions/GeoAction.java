package com.googlecode.talkmyphone.actions;

import java.util.List;

import com.googlecode.talkmyphone.geo.GeoManager;

import android.content.Intent;
import android.location.Address;

public class GeoAction extends Action {

    private void geo(String text) {
        List<Address> addresses = GeoManager.geoDecode(text);
        if (addresses != null) {
            if (addresses.size() > 1) {
                appendResult("Specify more details:");
                for (Address address : addresses) {
                    StringBuilder addr = new StringBuilder();
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        addr.append(address.getAddressLine(i) + "\n");
                    }
                    appendResult(addr.toString());
                }
            } else if (addresses.size() == 1) {
                GeoManager.launchExternal(addresses.get(0).getLatitude() + "," + addresses.get(0).getLongitude());
            }
        } else {
            appendResult("No match for \"" + text + "\"");
            // For emulation testing
            // GeoManager.launchExternal("48.833199,2.362232");
        }
    }

    @Override
    public void execute(Intent intent) {
        String args = intent.getStringExtra("args");
        geo(args);
    }

}
