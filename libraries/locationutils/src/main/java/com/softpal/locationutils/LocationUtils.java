package com.softpal.locationutils;

import android.location.Location;
import android.app.Activity;
import com.softpal.locationutils.Helper.GPSTracker;
import android.content.Context;
import com.github.softpal.R;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import android.Manifest;
import android.widget.Toast;

import java.util.ArrayList;
import android.os.Build;


public class LocationUtils {

	public static Location getMyLocation(Activity activity) {
		Location location = new Location("");
		if (activity == null || activity.isFinishing()) {
			return location;
		}
		if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == 0 || activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == 0) {
			GPSTracker gPSTracker = new GPSTracker(activity);
			if (gPSTracker.canGetLocation()) {
				location.setLatitude(gPSTracker.getLatitude());
				location.setLongitude(gPSTracker.getLongitude());
				return location;
			}
			gPSTracker.showSettingsAlert();
			return location;
		}
		requestLocationPermissions(activity);
		return location;
	}

	public static void requestLocationPermissions(final Context context) {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        String rationale = context.getString(R.string.loc_rationale);
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle(context.getString(R.string.loc_info))
                .setSettingsDialogTitle(context.getString(R.string.loc_warning));
    
        Permissions.check(context, permissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                Toast.makeText(context, context.getString(R.string.loc_granted), Toast.LENGTH_SHORT).show();
            }
    
            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                Toast.makeText(context, context.getString(R.string.loc_denied), Toast.LENGTH_SHORT).show();
                requestLocationPermissions(context);
            }
        });
    }
	
}