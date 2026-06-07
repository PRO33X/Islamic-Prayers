package com.softpal.locationutils.Helper;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.app.Activity;
import android.Manifest;
import android.os.Build;
import android.app.AlertDialog;
import com.github.softpal.R;

public class GPSTracker extends Service implements LocationListener {
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 60000;
    boolean canGetLocation = false;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    double latitude;
    Location location;
    protected LocationManager locationManager;
    double longitude;
    private final Activity mActivity;
    private final Context mContext;

    public GPSTracker(Activity activity) {
        mContext = activity;
        mActivity = activity;
        getLocation();
    }

    public boolean canGetLocation() {
        return canGetLocation;
    }

    public float getAccurecy() {
        return location.getAccuracy();
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService("location");
            isGPSEnabled = locationManager.isProviderEnabled("gps");
            isNetworkEnabled = locationManager.isProviderEnabled("network");
            if (isGPSEnabled || isNetworkEnabled) {
                canGetLocation = true;
                if (isNetworkEnabled) {
                    if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == 0 || mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == 0) {
                        locationManager.requestLocationUpdates("network", MIN_TIME_BW_UPDATES, 10.0f, this);
                    }

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation("network");
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                if (isGPSEnabled && location == null) {
                    locationManager.requestLocationUpdates("gps", MIN_TIME_BW_UPDATES, 10.0f, this);

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation("gps");
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.getAccuracy() != 0.0f) {
            int i = (location.getAccuracy() > -1.0f ? 1 : (location.getAccuracy() == -1.0f ? 0 : -1));
        }
        locationManager.removeUpdates(this);
        location.getAccuracy();
    }

    // @Override
    // public void onProviderDisabled(String str) {}

    // @Override
    // public void onProviderEnabled(String str) {}

    // @Override
    // public void onStatusChanged(String str, int i, Bundle bundle) {}

    public void showSettingsAlert() {
        final AlertDialog create = new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.gps_settings_title))
                .setMessage(mContext.getString(R.string.gps_settings_message))
                .setPositiveButton(mContext.getString(R.string.gps_settings_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        GPSTracker.this.mContext.startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
                    }
                })
                .setNegativeButton(mContext.getString(R.string.gps_settings_negative), null)
                .create();

        create.setCancelable(false);

        if (mActivity != null && !mActivity.isFinishing() && !create.isShowing()) {
            create.show();
        }

        create.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface d, int w, KeyEvent k) {
                if (w != 4) {
                    return false;
                }
                create.dismiss();
                return true;
            }
        });
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}