package com.PRO33X.MyPrayers;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import com.PRO33X.mCode.PrayerFormatter;
import com.PRO33X.mCode.PreferenceManager;

public class MainActivity extends Activity {

    private TextView TimeeNow,
            Time2prayers,
            CristianCalendar,
            HijriCalendar,
            Fajr_Time,
            Sunrise_Time,
            Zohr_Time,
            Asr_Time,
            Maghrib_Time,
            Eshaa_Time,
            countryTv,
            cityTv;

    private LinearLayout locationHeader;
    private Timer timer;
    private PrayerFormatter formatter;
    private int lastDay = -1;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        init_layout();
        setGradientAppbar();
        updateTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        formatter = null;
        loadLocationLabels();
        refreshPrayerTimes();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }

    private void init_layout() {
        TimeeNow         = findViewById(R.id.time_tv);
        Time2prayers     = findViewById(R.id.TTN_Prayer);
        CristianCalendar = findViewById(R.id.gregorian_tv);
        HijriCalendar    = findViewById(R.id.hijri_tv);
        Fajr_Time        = findViewById(R.id.Fajr_Time);
        Sunrise_Time     = findViewById(R.id.Sunrise_Time);
        Zohr_Time        = findViewById(R.id.Zohr_Time);
        Asr_Time         = findViewById(R.id.Asr_Time);
        Maghrib_Time     = findViewById(R.id.Maghrib_Time);
        Eshaa_Time       = findViewById(R.id.Eshaa_Time);
        countryTv        = findViewById(R.id.country_tv);
        cityTv           = findViewById(R.id.city_tv);
        locationHeader   = findViewById(R.id.location_header);

        locationHeader.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showRetakeLocationDialog();
                return true;
            }
        });
    }

    private PrayerFormatter getFormatter() {
        int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        if (formatter == null || today != lastDay) {
            formatter = new PrayerFormatter(this);
            lastDay   = today;
        }
        return formatter;
    }

    private void refreshPrayerTimes() {
        PrayerFormatter f = getFormatter();
        Fajr_Time.setText(f.getFajrTime());
        Sunrise_Time.setText(f.getSunriseTime());
        Zohr_Time.setText(f.getZohrTime());
        Asr_Time.setText(f.getAsrTime());
        Maghrib_Time.setText(f.getMaghribTime());
        Eshaa_Time.setText(f.getIshaTime());
    }

    private void updateTime() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PrayerFormatter f = getFormatter();
                        TimeeNow.setText(f.getTimeNow());
                        CristianCalendar.setText(f.getGregorianDate());
                        HijriCalendar.setText(f.getHijriDate());
                        Time2prayers.setText(f.getNextPrayerTime());
                    }
                });
            }
        }, 0, 1000);
    }

    private void loadLocationLabels() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String country      = prefs.getString("country", "");
        String city = prefs.getString("city_location", "");

        countryTv.setText(getString(R.string.country_label,
            country.isEmpty() ? getString(R.string.unavailable_info) : country));
        cityTv.setText(getString(R.string.city_label,
            city.isEmpty() ? getString(R.string.unavailable_info) : city));
    }

    private void showRetakeLocationDialog() {
        new android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_update_location_title))
            .setMessage(getString(R.string.dialog_update_location_message))
            .setPositiveButton(getString(R.string.dialog_btn_yes), new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    startActivity(new Intent(MainActivity.this, GetLocation.class));
                }
            })
            .setNegativeButton(getString(R.string.dialog_btn_no), null)
            .show();
    }

    public void setGradientAppbar() {
        try {
            ActionBar actionBar = getActionBar();
            GradientDrawable gd = new GradientDrawable();
            gd.setColors(new int[]{ Color.parseColor("#ffe3c74b"), Color.parseColor("#ffe39b3d") });
            gd.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
            actionBar.setBackgroundDrawable(gd);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.MenuSettings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.MenuExit) {
            System.exit(0);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}