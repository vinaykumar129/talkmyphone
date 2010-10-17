package com.googlecode.talkmyphone.actions;

import java.util.ArrayList;

import com.googlecode.talkmyphone.contacts.ContactsManager;
import com.googlecode.talkmyphone.contacts.Phone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class DialAction extends Action {
	
	private Context mContext;
	
	public DialAction(Context context) {
		mContext = context;
	}

	private boolean dial(String number) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + number));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
	}
	
    @Override
    public void execute(Intent intent) {
        String searchedText = intent.getStringExtra("args");

        String number = null;
        String contact = null;

        if (Phone.isCellPhoneNumber(searchedText)) {
            number = searchedText;
            contact = ContactsManager.getContactName(number);
        } else {
            ArrayList<Phone> mobilePhones = ContactsManager.getMobilePhones(searchedText);
            if (mobilePhones.size() > 1) {
                appendResult("Specify more details:");

                for (Phone phone : mobilePhones) {
                    appendResult(phone.contactName + " - " + phone.cleanNumber);
                }
            } else if (mobilePhones.size() == 1) {
                Phone phone = mobilePhones.get(0);
                contact = phone.contactName;
                number = phone.cleanNumber;
            } else {
                appendResult("No match for \"" + searchedText + "\"");
            }
        }

        if( number != null) {
            appendResult("Dial " + contact + " (" + number + ")");
            if(!dial(number)) {
                appendResult("Error can't dial.");
            }
        }
    }

}
