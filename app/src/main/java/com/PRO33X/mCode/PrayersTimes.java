package com.PRO33X.mCode;

import android.content.Context;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.tamrah.islamic.pray.PrayTime;

public class PrayersTimes {

    public enum Prayer {
        FAJR, SUNRISE, ZOHR, ASR, MAGRIB, ESHAA
    }

    public static final int AZAN_DURATION = 240;

    private final PrayerPrefs mPrayerPrefs;
    private final PrayTime pray;

    public PrayersTimes(Context ctx) {
        mPrayerPrefs = new PrayerPrefs(ctx);
        pray         = buildPrayTime(Calendar.getInstance());
    }

    private PrayTime buildPrayTime(Calendar calendar) {
        return new PrayTime(
            calendar,
            mPrayerPrefs.getLatitude(),
            mPrayerPrefs.getLongitude(),
            TimeZone.getDefault(),
            mPrayerPrefs.getCalculationMethod(),
            mPrayerPrefs.getJuristicMethod()
        );
    }

    public Date getTomorrowFajr() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return buildPrayTime(calendar).getFajr();
    }

    public Date getPrayerDate(Prayer prayer) {
        switch (prayer) {
            case FAJR:    return pray.getFajr();
            case SUNRISE: return pray.getSunrise();
            case ZOHR:    return pray.getDhuhr();
            case ASR:     return pray.getAsr();
            case MAGRIB:  return pray.getMaghrib();
            case ESHAA:   return pray.getIsha();
            default:      return null;
        }
    }

    public Prayer getNextPrayer() {
        int cur = LocalTime.now().toSecondOfDay();
        for (Prayer prayer : Prayer.values()) {
            int t = date2Sec(getPrayerDate(prayer));
            if (cur >= t && cur < t + AZAN_DURATION) return prayer;
            if (cur < t) return prayer;
        }
        return Prayer.FAJR; // after Isha next day Fajr
    }

    public boolean isAzanTime() {
        int cur = LocalTime.now().toSecondOfDay();
        for (Prayer prayer : Prayer.values()) {
            int t = date2Sec(getPrayerDate(prayer));
            if (cur >= t && cur < t + AZAN_DURATION) return true;
        }
        return false;
    }

    public boolean isTomorrowFajr() {
        int cur = LocalTime.now().toSecondOfDay();
        return cur > date2Sec(pray.getFajr()) + AZAN_DURATION
            && cur > date2Sec(pray.getIsha()) + AZAN_DURATION;
    }

    public int date2Sec(Date date) {
        return LocalDateTime
            .ofInstant(date.toInstant(), ZoneId.systemDefault())
            .toLocalTime()
            .toSecondOfDay();
    }
}