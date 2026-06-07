package com.PRO33X.mCode;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import com.PRO33X.MyPrayers.R;

public class NotificationHelper {

    private static final String CHANNEL_ID   = "prayer_times";
    private static final String CHANNEL_NAME = "Prayer Times";

    public static void show(Context context, int id, String prayerName) {
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH));
            nm.notify(id, new Notification.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(prayerName)
                    .setContentText(context.getString(R.string.notif_prayer_body, prayerName))
                    .setAutoCancel(true)
                    .build());
        } else {
            nm.notify(id, new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(prayerName)
                    .setContentText(context.getString(R.string.notif_prayer_body, prayerName))
                    .setAutoCancel(true)
                    .build());
        }
    }
}