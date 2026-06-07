package com.PRO33X.MyPrayers;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.PRO33X.mCode.OneChoicePrefView;
import com.PRO33X.mCode.MultiChoicePrefView;
import java.util.LinkedHashMap;
import android.os.Build;
import com.PRO33X.mCode.PrayerScheduler;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import android.content.Context;
import android.widget.Toast;
import java.util.ArrayList;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init_layout();
        init_action_bar();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void init_layout() {
        Resources res = getResources();

        OneChoicePrefView calculation = findViewById(R.id.calculation_method);
        LinkedHashMap<String, String> calc_options = new LinkedHashMap<>();
        calc_options.put(res.getString(R.string.Egypt), "Egypt");
        calc_options.put(res.getString(R.string.Tehran), "Tehran");
        calc_options.put(res.getString(R.string.ISNA), "ISNA");
        calc_options.put(res.getString(R.string.Jafari), "Jafari");
        calc_options.put(res.getString(R.string.MWL), "MWL");
        calc_options.put(res.getString(R.string.Makkah), "Makkah");
        calc_options.put(res.getString(R.string.Karachi), "Karachi");
        calculation.setOptions(calc_options, "Makkah");

        OneChoicePrefView juristic = findViewById(R.id.juristic_method);
        LinkedHashMap<String, String> juristic_options = new LinkedHashMap<>();
        juristic_options.put(getString(R.string.juristic_shafii_standard), "Shafii");
        juristic_options.put(getString(R.string.juristic_hanafi), "Hanafi");
        juristic.setOptions(juristic_options, "Shafii");

        OneChoicePrefView time_format = findViewById(R.id.time_format);
        LinkedHashMap<String, String> time_options = new LinkedHashMap<>();
        time_options.put(getString(R.string.time_auto), "TF_Auto");
        time_options.put(getString(R.string.time_12hour), "TF_12");
        time_options.put(getString(R.string.time_24hour), "TF_24");
        time_format.setOptions(time_options, "TF_Auto");

        MultiChoicePrefView notifications = findViewById(R.id.notification_prayers);
        LinkedHashMap<String, String> notif_options = new LinkedHashMap<>();
        notif_options.put(res.getString(R.string.prayer_fajr), "Fajr");
        notif_options.put(res.getString(R.string.prayer_sunrise), "Sunrise");
        notif_options.put(res.getString(R.string.prayer_zohr), "Zohr");
        notif_options.put(res.getString(R.string.prayer_asr), "Asr");
        notif_options.put(res.getString(R.string.prayer_maghrib), "Magrib");
        notif_options.put(res.getString(R.string.prayer_eshaa), "Eshaa");
        notifications.setOptions(notif_options);
    }

    public void init_action_bar() {
        try {
            ActionBar actionBar = getActionBar();
            TextView textView = new TextView(this);
            textView.setLayoutParams(new ActionBar.LayoutParams(
            ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.WRAP_CONTENT));
            textView.setText(getString(R.string.settings));
            textView.setTextSize(20.0f);
            textView.setTextColor(Color.WHITE);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(textView);
            actionBar.setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {

            Permissions.check(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    getString(R.string.permission_notification_rationale_message),
                    new Permissions.Options()
                            .setRationaleDialogTitle(getString(R.string.permission_notification_rationale_title))
                            .setSettingsDialogTitle(getString(R.string.permission_notification_settings_title)),
                    new PermissionHandler() {
                        @Override
                        public void onGranted() {
                            PrayerScheduler.scheduleAll(SettingsActivity.this);
                        }

                        @Override
                        public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                            Toast.makeText(context,
                                    getString(R.string.permission_notification_denied_toast),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        PrayerScheduler.scheduleAll(this);
    }
}