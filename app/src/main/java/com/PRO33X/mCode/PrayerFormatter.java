package com.PRO33X.mCode;

import android.content.Context;
import org.tamrah.islamic.hijri.HijraCalendar;
import com.PRO33X.MyPrayers.R;
import com.PRO33X.mCode.PrayersTimes.Prayer;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PrayerFormatter {
    private final Context context;
    private final PrayersTimes pt;
    private final SimpleDateFormat timeFormat;
    private final SimpleDateFormat timeFormatSec;
    private final SimpleDateFormat dayFormat;
    private final SimpleDateFormat yearFormat;

    public PrayerFormatter(Context ctx) {
        context = ctx.getApplicationContext();
        pt = new PrayersTimes(context);
        PrayerPrefs prefs = new PrayerPrefs(context);
        timeFormat = prefs.getTimeFormat();
        timeFormatSec = prefs.getTimeFormatSec();
        dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
        yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
    }

    public String getPrayerName(Prayer prayer) {
        switch (prayer) {
            case FAJR:
                return context.getString(R.string.prayer_fajr);
            case SUNRISE:
                return context.getString(R.string.prayer_sunrise);
            case ZOHR:
                return context.getString(R.string.prayer_zohr);
            case ASR:
                return context.getString(R.string.prayer_asr);
            case MAGRIB:
                return context.getString(R.string.prayer_maghrib);
            case ESHAA:
                return context.getString(R.string.prayer_eshaa);
            default:
                return "";
        }
    }

    public String getNextPrayerTime() {
        Prayer next = pt.getNextPrayer();
        boolean isTomorrow = pt.isTomorrowFajr();
        String name = isTomorrow ? context.getString(R.string.tomorrow_fajr) : getPrayerName(next);
        if (pt.isAzanTime()) {
            return name + " Azan: " + timeFormat.format(pt.getPrayerDate(next));
        }
        int nowSec = LocalTime.now().toSecondOfDay();
        int nextSec = isTomorrow ? pt.date2Sec(pt.getTomorrowFajr()) + 86_400 : pt.date2Sec(pt.getPrayerDate(next));
        return formatNextPrayer(name, secondsToString(nextSec - nowSec));
    }

    public String formatNextPrayer(String prayerName, String formattedTime) {
        return context.getString(R.string.remaining_time_format, prayerName, formattedTime);
    }

    public String secondsToString(int totalSeconds) {
        int h = totalSeconds / 3600;
        int m = (totalSeconds % 3600) / 60;
        int s = totalSeconds % 60;
        if (h > 0) {
            return h + ":" + pad(m) + ":" + pad(s);
        } else if (m > 0) {
            return m + ":" + pad(s);
        } else {
            return s + " " + context.getString(R.string.seconds_label);
        }
    }

    public String getPrayerTime(Prayer prayer) {
        Date date = pt.getPrayerDate(prayer);
        return date != null ? timeFormat.format(date) : "--:--";
    }

    public String getFajrTime() {
        return getPrayerTime(Prayer.FAJR);
    }

    public String getSunriseTime() {
        return getPrayerTime(Prayer.SUNRISE);
    }

    public String getZohrTime() {
        return getPrayerTime(Prayer.ZOHR);
    }

    public String getAsrTime() {
        return getPrayerTime(Prayer.ASR);
    }

    public String getMaghribTime() {
        return getPrayerTime(Prayer.MAGRIB);
    }

    public String getIshaTime() {
        return getPrayerTime(Prayer.ESHAA);
    }

    public Date getPrayerDate(Prayer prayer) {
        return pt.getPrayerDate(prayer);
    }

    public String getTimeNow() {
        return timeFormatSec.format(Calendar.getInstance().getTime());
    }

    public String getGregorianDate() {
        Calendar c = Calendar.getInstance();
        return dayFormat.format(c.getTime()) + ". " + gregorianMonthName(c.get(Calendar.MONTH) + 1) + " " + yearFormat.format(c.getTime());
    }

    private String gregorianMonthName(int month) {
        int
                [] ids = {R.string.jan, R.string.feb, R.string.mar, R.string.apr, R.string.may, R.string.jun, R.string.jul, R.string.aug, R.string.sep, R.string.oct, R.string.nov, R.string.dec};
        if (month < 1 || month > 12) return "";
        return context.getString(ids[month - 1]);
    }

    public String getHijriDate() {
        return formattedHijri();
    }

    private String formattedHijri() {
        HijraCalendar hijra = HijraCalendar.getInstance();
        int day = hijra.get(Calendar.DAY_OF_MONTH);
        int month = hijra.get(Calendar.MONTH);
        int year = hijra.get(Calendar.YEAR);
        return day + ". " + hijriMonthName(month) + " " + year;
    }

    private String hijriMonthName(int month) {
        int
                [] ids = {R.string.Hijri_1_Month, R.string.Hijri_2_Month, R.string.Hijri_3_Month, R.string.Hijri_4_Month, R.string.Hijri_5_Month, R.string.Hijri_6_Month, R.string.Hijri_7_Month, R.string.Hijri_8_Month, R.string.Hijri_9_Month, R.string.Hijri_10_Month, R.string.Hijri_11_Month, R.string.Hijri_12_Month};
        if (month < 1 || month > 12) return "";
        return context.getString(ids[month - 1]);
    }

    private static String pad(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }
}