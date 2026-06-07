package com.PRO33X.MyPrayers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.PRO33X.mCode.PreferenceManager;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.softpal.locationutils.LocationUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetLocation extends Activity {
    private Button btnHed2Main, btnGetLocation;
    private TextView showLocation;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_location);
        if (Build.VERSION.SDK_INT >= 21 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != 0 && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != 0) {
            requestLocationPermissions();
        }
        showLocation = findViewById(R.id.showLocation);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnHed2Main = findViewById(R.id.head2main);
        btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAndDisplayLocation();
            }
        });
        btnHed2Main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GetLocation.this, MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private void requestLocationPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        Permissions.Options options = new Permissions.Options().setRationaleDialogTitle(getString(R.string.permission_location_rationale_title)).setSettingsDialogTitle(getString(R.string.permission_location_settings_title)).setSettingsDialogMessage(getString(R.string.permission_location_settings_message));
        Permissions.check(this, permissions, getString(R.string.permission_location_rationale_message), options, new PermissionHandler() {
            @Override
            public void onGranted() {
                Toast.makeText(GetLocation.this, getString(R.string.permission_location_granted_toast), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                Toast.makeText(GetLocation.this, getString(R.string.permission_location_denied_toast), Toast.LENGTH_SHORT).show();
                requestLocationPermissions();
            }
        });
    }

    private void fetchAndDisplayLocation() {
        btnGetLocation.setEnabled(false);
        btnHed2Main.setVisibility(View.GONE);
        showLocation.setText(R.string.fetching_location);
        Location location = LocationUtils.getMyLocation(this);
        if (location == null || (location.getLatitude() == 0 && location.getLongitude() == 0)) {
            showLocation.setText(R.string.location_not_set);
            btnGetLocation.setEnabled(true);
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String resultText;
                boolean success = false;
                try {
                    String timezoneId = getTimezoneId();
                    int utcOffset = getUtcOffset();
                    List<
                            Address> addresses = new Geocoder(GetLocation.this).getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String countryName = address.getCountryName();
                        String countryCode = address.getCountryCode();
                        String cityName = resolveCityName(address);
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(GetLocation.this).edit();
                        editor.putFloat("latitude", (float) location.getLatitude());
                        editor.putFloat("longitude", (float) location.getLongitude());
                        editor.putString("city_location", cityName);
                        editor.putString("country", countryName);
                        editor.putString("country_code", countryCode);
                        editor.apply();
                        resultText = getString(R.string.location_line_country, countryName) + "\n" + getString(R.string.location_line_code, countryCode) + "\n" + getString(R.string.location_line_city, cityName) + "\n" + formatCoords(location, timezoneId, utcOffset);
                    } else {
                        resultText = getString(R.string.location_city_unavailable) + "\n" + formatCoords(location, timezoneId, utcOffset);
                    }
                    success = true;
                } catch (IOException e) {
                    resultText = getString(R.string.location_gps_error);
                }
                final String finalText = resultText;
                final boolean finalSuccess = success;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showLocation.setText(finalText);
                        btnGetLocation.setEnabled(true);
                        btnHed2Main.setVisibility(finalSuccess ? View.VISIBLE : View.GONE);
                        Toast.makeText(GetLocation.this, finalSuccess ? getString(R.string.location_set_success) : finalText, finalSuccess ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private String resolveCityName(Address address) {
        if (address.getLocality() != null) return address.getLocality();
        if (address.getAdminArea() != null) return address.getAdminArea();
        return address.getAddressLine(address.getMaxAddressLineIndex());
    }

    private String formatCoords(Location loc, String timezoneId, int utcOffset) {
        String offsetStr = (utcOffset >= 0 ? "+" : "") + utcOffset + "h";
        return getString(R.string.location_line_latitude, String.valueOf(loc.getLatitude())) + "\n" + getString(R.string.location_line_longitude, String.valueOf(loc.getLongitude())) + "\n" + getString(R.string.location_line_altitude, String.valueOf(loc.getAltitude())) + "\n" + getString(R.string.location_line_timezone, timezoneId) + "\n" + getString(R.string.location_line_utc_offset, offsetStr);
    }

    private String getTimezoneId() {
        return TimeZone.getDefault().getID();
    }

    private int getUtcOffset() {
        TimeZone tz = TimeZone.getDefault();
        int offset = (tz.getOffset(System.currentTimeMillis()) / 1000) / 3600;
        return tz.inDaylightTime(new Date()) ? offset - 1 : offset;
    }
}