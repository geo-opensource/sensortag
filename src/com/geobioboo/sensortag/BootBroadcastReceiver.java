package com.geobioboo.sensortag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, TipOverIntentService.class);
        context.startService(startServiceIntent);
    }

}
