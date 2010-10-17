package com.googlecode.talkmyphone.actions;

import java.util.ArrayList;

import com.googlecode.talkmyphone.contacts.ContactsManager;
import com.googlecode.talkmyphone.contacts.Phone;
import com.googlecode.talkmyphone.sms.SmsMmsManager;

import android.content.Intent;

public class SendSmsToLastRecipientAction extends Action {

    /** sends a SMS to the specified contact */
    private void sendSMS(String message, String contact) {
        if (Phone.isCellPhoneNumber(contact)) {
            appendResult("Sending sms to " + ContactsManager.getContactName(contact));
            SmsMmsManager.sendSMSByPhoneNumber(message, contact);
        } else {
            ArrayList<Phone> mobilePhones = ContactsManager.getMobilePhones(contact);
            if (mobilePhones.size() > 1) {
                appendResult("Specify more details:");

                for (Phone phone : mobilePhones) {
                    appendResult(phone.contactName + " - " + phone.cleanNumber);
                }
            } else if (mobilePhones.size() == 1) {
                Phone phone = mobilePhones.get(0);
                appendResult("Sending sms to " + phone.contactName + " (" + phone.cleanNumber + ")");
                SmsMmsManager.sendSMSByPhoneNumber(message, phone.cleanNumber);
            } else {
                appendResult("No match for \"" + contact + "\"");
            }
        }
    }

    @Override
    public void execute(Intent intent) {
        if (SendOrReadSmsAction.lastRecipient == null) {
            appendResult("Error: no recipient registered.");
        } else {
            String args = intent.getStringExtra("args");
            sendSMS(args, SendOrReadSmsAction.lastRecipient);
        }
    }

}
