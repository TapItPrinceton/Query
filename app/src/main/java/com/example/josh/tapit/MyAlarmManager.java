package com.example.josh.tapit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyAlarmManager extends BroadcastReceiver {

    Context _context;
    @Override
    public void onReceive(Context context, Intent intent) {
        _context= context;
        //connect to server..

    }
}