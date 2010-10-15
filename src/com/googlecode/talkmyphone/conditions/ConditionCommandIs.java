package com.googlecode.talkmyphone.conditions;

import android.content.Context;
import android.content.Intent;

public class ConditionCommandIs extends Condition {

    private String command;

    public ConditionCommandIs(String command) {
        this.command = command;
    }

    @Override
    public boolean isTrue(Context context, Intent intent) {
        return command.equals(intent.getStringExtra("command"));
    }

}
