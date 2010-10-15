package com.googlecode.talkmyphone;

import java.util.ArrayList;

import com.googlecode.talkmyphone.actions.Action;
import com.googlecode.talkmyphone.actions.NotifyBatteryStateAction;
import com.googlecode.talkmyphone.actions.RingAction;
import com.googlecode.talkmyphone.actions.SendHelpAction;
import com.googlecode.talkmyphone.actions.StopRingingAction;
import com.googlecode.talkmyphone.conditions.AlwaysTrueCondition;
import com.googlecode.talkmyphone.conditions.Condition;
import com.googlecode.talkmyphone.conditions.ConditionCommandIs;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * This class monitors phone events and user actions and triggers actions on events when conditions are met
 */
public class BroadcastsAndCommandsHandler {

    private Context mContext;
    private ArrayList<Rule> mRules = new ArrayList<Rule>();

    public void destroy() {
        for(Rule rule : mRules) {
            rule.enable(false);
        }
        mRules = null;
    }

    public void addRule(IntentFilter filteredEvent, Condition condition, Action action, String settingsName) {
        Rule rule =  new Rule(mContext, filteredEvent, condition, action, settingsName);
        mRules.add(rule);
    }

    public void updateRulesFromSettings() {
        for(Rule rule : mRules) {
            rule.updateFromSettings();
        }
    }

    public BroadcastsAndCommandsHandler(Context context){

        mContext = context;

        addRule(new IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                new AlwaysTrueCondition (),
                new NotifyBatteryStateAction(),
                "notifyBattery");

        addRule(new IntentFilter("ACTION_TALKMYPHONE_USER_COMMAND_RECEIVED"),
                new ConditionCommandIs("?"),
                new SendHelpAction(),
                null);

        addRule(new IntentFilter("ACTION_TALKMYPHONE_USER_COMMAND_RECEIVED"),
                new ConditionCommandIs("ring"),
                new RingAction(),
                null);

        addRule(new IntentFilter("ACTION_TALKMYPHONE_USER_COMMAND_RECEIVED"),
                new ConditionCommandIs("stop"),
                new StopRingingAction(),
                null);

        updateRulesFromSettings();

    }



}
