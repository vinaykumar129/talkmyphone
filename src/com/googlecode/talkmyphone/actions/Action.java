package com.googlecode.talkmyphone.actions;

import android.content.Context;
import android.content.Intent;

public abstract class Action {

    public abstract void execute(Context context, Intent intent);

}
