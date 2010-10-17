package com.googlecode.talkmyphone.actions;

import java.util.ArrayList;

import com.googlecode.talkmyphone.contacts.Contact;
import com.googlecode.talkmyphone.contacts.ContactAddress;
import com.googlecode.talkmyphone.contacts.ContactsManager;
import com.googlecode.talkmyphone.contacts.Phone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class NotifyMatchingContactsAction extends Action {

    private Context mContext;

    public NotifyMatchingContactsAction (Context context) {
        mContext = context;
    }

    private String makeBold(String s) {
        SharedPreferences prefs = mContext.getSharedPreferences("TalkMyPhone", 0);
        boolean formatChatResponses = prefs.getBoolean("formatResponses", false);
        String res = s;
        if (formatChatResponses) {
           res = " *" + s + "* ";
        }
        return res;
    }

    private String makeItalic(String s) {
        SharedPreferences prefs = mContext.getSharedPreferences("TalkMyPhone", 0);
        boolean formatChatResponses = prefs.getBoolean("formatResponses", false);
        String res = s;
        if (formatChatResponses) {
           res = " _" + s + "_ ";
        }
        return res;
    }

    @Override
    public void execute(Intent intent) {

        String searchedText = intent.getStringExtra("args");

        ArrayList<Contact> contacts = ContactsManager.getMatchingContacts(searchedText);

        if (contacts.size() > 0) {
            for (Contact contact : contacts) {
                StringBuilder strContact = new StringBuilder();
                strContact.append(makeBold(contact.name));

                ArrayList<Phone> mobilePhones = ContactsManager.getPhones(contact.id);
                if (mobilePhones.size() > 0) {
                    strContact.append("\r\n" + makeItalic("Phones"));
                    for (Phone phone : mobilePhones) {
                        strContact.append("\r\n" + phone.label + " - " + phone.cleanNumber);
                    }
                }

                ArrayList<ContactAddress> emails = ContactsManager.getEmailAddresses(contact.id);
                if (emails.size() > 0) {
                    strContact.append("\r\n" + makeItalic("Emails"));
                    for (ContactAddress email : emails) {
                        strContact.append("\r\n" + email.label + " - " + email.address);
                    }
                }

                ArrayList<ContactAddress> addresses = ContactsManager.getPostalAddresses(contact.id);
                if (addresses.size() > 0) {
                    strContact.append("\r\n" + makeItalic("Addresses"));
                    for (ContactAddress address : addresses) {
                        strContact.append("\r\n" + address.label + " - " + address.address);
                    }
                }
                Intent i = new Intent("ACTION_TALKMYPHONE_MESSAGE_TO_TRANSMIT");
                i.putExtra("message", strContact.toString() + "\r\n");
                mContext.sendBroadcast(i);
            }
        } else {
            Intent i = new Intent("ACTION_TALKMYPHONE_MESSAGE_TO_TRANSMIT");
            i.putExtra("message", "No match for \"" + searchedText + "\"");
            mContext.sendBroadcast(i);
        }
    }

}
