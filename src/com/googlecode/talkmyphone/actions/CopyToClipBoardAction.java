package com.googlecode.talkmyphone.actions;

import com.googlecode.talkmyphone.XmppService;

import android.content.Context;
import android.content.Intent;
import android.text.ClipboardManager;

public class CopyToClipBoardAction extends Action {

    @Override
    public void execute(Context context, Intent intent) {
        String text = intent.getStringExtra("args");
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
            XmppService.getInstance().send("Text copied");
        }
        catch(Exception ex) {
            XmppService.getInstance().send("Clipboard access failed");
        }
    }

}
