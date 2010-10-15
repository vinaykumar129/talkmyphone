package com.googlecode.talkmyphone.conditions;

import android.content.Context;
import android.content.Intent;

public abstract class Condition {

    abstract public boolean isTrue(Context context, Intent intent);

}
