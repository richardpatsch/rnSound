package com.rnsound;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.HeadlessJsTaskService;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
        Log.d("asldf", "BROADCAST RECEIVED");
        context.startService(new Intent(context, MyTaskService.class));
        HeadlessJsTaskService.acquireWakeLockNow(context);
    }
}