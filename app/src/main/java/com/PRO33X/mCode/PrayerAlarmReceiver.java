package com.PRO33X.mCode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PrayerAlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_PRAYER_NAME = "prayer_name";
    public static final String EXTRA_NOTIF_ID    = "notif_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra(EXTRA_PRAYER_NAME);
        int    id   = intent.getIntExtra(EXTRA_NOTIF_ID, 0);
        if (name != null) NotificationHelper.show(context, id, name);
        PrayerScheduler.scheduleAll(context);
    }
}