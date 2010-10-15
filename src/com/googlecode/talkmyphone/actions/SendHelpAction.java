package com.googlecode.talkmyphone.actions;

import com.googlecode.talkmyphone.XmppService;

import android.content.Context;
import android.content.Intent;

public class SendHelpAction extends Action {

    @Override
    public void execute(Context context, Intent intent) {
        StringBuilder builder = new StringBuilder();
        builder.append("Available commands:\n");
        builder.append("- \"?\": shows this help.\n");
        builder.append("- \"dial:#contact#\": dial the specified contact.\n");
        builder.append("- \"reply:#message#\": send a sms to your last recipient with content message.\n");
        builder.append("- \"sms:#contact#[:#message#]\": sends a sms to number with content message or display last sent sms.\n");
        builder.append("- \"contact:#contact#\": display informations of a searched contact.\n");
        builder.append("- \"geo:#address#\": Open Maps or Navigation or Street view on specific address\n");
        builder.append("- \"where\": sends you google map updates about the location of the phone until you send \"stop\"\n");
        builder.append("- \"ring\": rings the phone until you send \"stop\"\n");
        builder.append("- \"copy:#text#\": copy text to clipboard\n");
        builder.append("and you can paste links and open it with the appropriate app\n");
        XmppService.getInstance().send(builder.toString());
    }

}
