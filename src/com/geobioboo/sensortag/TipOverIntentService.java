package com.geobioboo.sensortag;

import android.app.IntentService;
import android.content.Intent;

public class TipOverIntentService extends IntentService {

    private static boolean mIsRunning;

    public TipOverIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!mIsRunning) {
            // start bluetooth listening
        }
    }

}
