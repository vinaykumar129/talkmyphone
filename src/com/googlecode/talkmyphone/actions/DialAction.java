package com.googlecode.talkmyphone.actions;

import java.util.ArrayList;

import com.googlecode.talkmyphone.contacts.ContactsManager;
import com.googlecode.talkmyphone.contacts.Phone;
import com.googlecode.talkmyphone.phone.PhoneManager;

import android.content.Intent;

public class DialAction extends Action {

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
            if(!PhoneManager.Dial(number)) {
                appendResult("Error can't dial.");
            }
        }
    }

}
