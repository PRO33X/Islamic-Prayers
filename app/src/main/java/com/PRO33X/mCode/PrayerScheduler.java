package com.PRO33X.mCode;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Date;
import java.util.Set;
import com.PRO33X.mCode.PrayersTimes.Prayer;

public class PrayerScheduler {

    public static void scheduleAll(Context context) {
        PrayerPrefs     prefs   = new PrayerPrefs(context);
        Set<String>     enabled = prefs.getNotificationPrayers();
        PrayerFormatter fmt     = new PrayerFormatter(context);
        AlarmManager    am      = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        for (Prayer prayer : Prayer.values()) {
            int    id   = prayer.ordinal();
            String name = fmt.getPrayerName(prayer);
            cancel(context, am, id);
            if (!enabled.contains(name)) continue;
            Date date = fmt.getPrayerDate(prayer);
            if (date == null) continue;
            long millis = date.getTime();
            if (millis <= System.currentTimeMillis())
                millis += 86_400_000L;
            setAlarm(am, millis, buildIntent(context, id, name));
        }
    }

    private static void setAlarm(AlarmManager am, long millis, PendingIntent pi) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms())
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi);
            else
                am.setWindow(AlarmManager.RTC_WAKEUP, millis, 60_000L, pi);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, millis, pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, millis, pi);
        }
    }

    private static void cancel(Context context, AlarmManager am, int id) {
        am.cancel(buildIntent(context, id, null));
    }

    private static PendingIntent buildIntent(Context context, int id, String name) {
        Intent intent = new Intent(context, PrayerAlarmReceiver.class);
        if (name != null) {
            intent.putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, name);
            intent.putExtra(PrayerAlarmReceiver.EXTRA_NOTIF_ID, id);
        }
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            flags |= PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(context, id, intent, flags);
    }
}