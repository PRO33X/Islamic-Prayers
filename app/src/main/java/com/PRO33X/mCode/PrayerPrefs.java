package com.PRO33X.mCode;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import android.text.format.DateFormat;
import org.tamrah.islamic.pray.CalculationMethod;
import org.tamrah.islamic.pray.JuristicMethod;
import java.util.HashSet;
import java.util.Set;

public class PrayerPrefs {
    private Context context;
    private SharedPreferences prefs;

    public PrayerPrefs(Context ctx) {
        context = ctx;
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public double getLatitude() {
        return prefs.getFloat("latitude", 0);
    }

    public double getLongitude() {
        return prefs.getFloat("longitude", 0);
    }

    public CalculationMethod getCalculationMethod() {
        String Value = prefs.getString("calc_method", "Makkah");
        switch (Value) {
            case "Egypt":
                return CalculationMethod.Egypt;
            case "Tehran":
                return CalculationMethod.Tehran;
            case "ISNA":
                return CalculationMethod.ISNA;
            case "Jafari":
                return CalculationMethod.Jafari;
            case "MWL":
                return CalculationMethod.MWL;
            case "Makkah":
                return CalculationMethod.Makkah;
            case "Karachi":
                return CalculationMethod.Karachi;
            default:
                prefs.edit().putString("calc_method", "Makkah").apply();
                return CalculationMethod.Makkah;
        }
    }

    public JuristicMethod getJuristicMethod() {
        String Value = prefs.getString("Juristic", "Shafii");
        switch (Value) {
            case "Shafii":
                return JuristicMethod.Shafii;
            case "Hanafi":
                return JuristicMethod.Hanafi;
            default:
                prefs.edit().putString("Juristic", "Shafii").apply();
                return JuristicMethod.Shafii;
        }
    }

    public SimpleDateFormat getTimeFormatSec() {
        String value = prefs.getString("TimeFormat", "TF_Auto");
        if (value.equals("TF_Auto")) {
            boolean is24HourFormat = DateFormat.is24HourFormat(context);
            if (is24HourFormat) {
                return new SimpleDateFormat("HH:mm:ss");
            } else {
                return new SimpleDateFormat("hh:mm:ss a");
            }
        } else if (value.equals("TF_12")) {
            return new SimpleDateFormat("hh:mm:ss a");
        } else if (value.equals("TF_24")) {
            return new SimpleDateFormat("HH:mm:ss");
        }
        return null;
    }

    public SimpleDateFormat getTimeFormat() {
        String value = prefs.getString("TimeFormat", "TF_Auto");
        if (value.equals("TF_Auto")) {
            boolean is24HourFormat = DateFormat.is24HourFormat(context);
            if (is24HourFormat) {
                return new SimpleDateFormat("HH:mm");
            } else {
                return new SimpleDateFormat("hh:mm a");
            }
        } else if (value.equals("TF_12")) {
            return new SimpleDateFormat("hh:mm a");
        } else if (value.equals("TF_24")) {
            return new SimpleDateFormat("HH:mm");
        }
        return null;
    }

    public Set<String> getNotificationPrayers() {
        return prefs.getStringSet("notif_prayers", new HashSet<String>());
    }
}