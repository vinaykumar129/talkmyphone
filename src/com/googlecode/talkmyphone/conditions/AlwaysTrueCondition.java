package com.googlecode.talkmyphone.conditions;

import android.content.Context;
import android.content.Intent;

public class AlwaysTrueCondition extends Condition {

    @Override
    public boolean isTrue(Context context, Intent intent) {
        return true;
    }

}
