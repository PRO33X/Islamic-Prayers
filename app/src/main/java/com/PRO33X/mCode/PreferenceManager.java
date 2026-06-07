package com.PRO33X.mCode;

import android.content.Context;
import android.content.SharedPreferences;


public class PreferenceManager {

	private final Context mContext;
	public PreferenceManager(Context context) {
		mContext = context;
	}

	public static SharedPreferences getDefaultSharedPreferences(Context context) {
		return context.getSharedPreferences(getDefaultSharedPreferencesName(context),
				getDefaultSharedPreferencesMode());
	}

	private static String getDefaultSharedPreferencesName(Context context) {
		return context.getPackageName() + "_preferences";
	}

	private static int getDefaultSharedPreferencesMode() {
		return Context.MODE_PRIVATE;
	}

}
