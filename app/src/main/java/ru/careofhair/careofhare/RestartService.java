package ru.careofhair.careofhare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestartService extends BroadcastReceiver {
    public RestartService() {
        Log.d("test", "reciverCreate");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("test", "onReceive");
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, Send2SMS.class);
            context.startService(pushIntent);
        }
    }
}
