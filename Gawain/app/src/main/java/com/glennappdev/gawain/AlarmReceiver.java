package com.glennappdev.gawain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tomergoldst.timekeeper.model.Alarm;

import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //Extract alarms from intent
        List<Alarm> alarms = intent.getParcelableArrayListExtra("alarms");

    }
}