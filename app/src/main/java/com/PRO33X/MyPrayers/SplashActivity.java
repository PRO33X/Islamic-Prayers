package com.PRO33X.MyPrayers;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import com.PRO33X.mCode.PreferenceManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class SplashActivity extends Activity {
    /*
    * Basically we're using this activity 
    * to determine what we should run first
    * (to get location or start actual app)
    * but it's not an actual SplashScreen :)
    */
    private static final int SPLASH_DELAY = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstTime = prefs.getBoolean("isFirstTime", true);
        float latitude = prefs.getFloat("latitude", 0);
        float longitude = prefs.getFloat("longitude", 0);
        boolean isLocationSet = longitude != 0 && latitude != 0;
        Class<? extends Activity> nextActivity;
        // fancy toasts to tell what's happening 
        if (!isFirstTime && !isLocationSet) {
            String str = getResources().getString(R.string.Error_noLocation);
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
            nextActivity = GetLocation.class;
        } else if (isFirstTime && isLocationSet) {
            String str = getResources().getString(R.string.Error_MissedSP);
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
            nextActivity = GetLocation.class;
        } else if (isFirstTime && !isLocationSet) {
            String str = getResources().getString(R.string.Msg_SetLocation);
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
            nextActivity = GetLocation.class;
        } else {
            nextActivity = MainActivity.class;
        }
        final Class<? extends Activity> pass = nextActivity;
        new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashActivity.this, pass));
                        finish();
                    }
                }, SPLASH_DELAY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isFirstTime", false);
        editor.apply();
    }
}