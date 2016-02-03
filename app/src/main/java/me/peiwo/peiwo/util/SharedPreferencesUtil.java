package me.peiwo.peiwo.util;

import me.peiwo.peiwo.constans.Constans;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Dong Fuhai on 2014-05-19 下午3:35.
 * 
 * @modify:
 */
public class SharedPreferencesUtil {

	public static void putIntExtra(Context context, String key, int value) {
		if(context == null)
			return;
		SharedPreferences preferences = context.getSharedPreferences(
				Constans.SP_NAME, Context.MODE_PRIVATE);
		preferences.edit().putInt(key, value).commit();
	}

	public static int getIntExtra(Context context, String key, int defValue) {
		if(context == null)
			return 0;
		SharedPreferences preferences = context.getSharedPreferences(
				Constans.SP_NAME, Context.MODE_PRIVATE);
		return preferences.getInt(key, defValue);
	}

	public static void putLongExtra(Context context, String key, long value) {
		if(context == null)
			return;
		SharedPreferences preferences = context.getSharedPreferences(
				Constans.SP_NAME, Context.MODE_PRIVATE);
		preferences.edit().putLong(key, value).commit();
	}

	public static long getLongExtra(Context context, String key, long defValue) {
		if(context == null)
			return 0;
		SharedPreferences preferences = context.getSharedPreferences(
				Constans.SP_NAME, Context.MODE_PRIVATE);
		return preferences.getLong(key, defValue);
	}

	public static void putStringExtra(Context context, String key, String value) {
		if(context == null)
			return;
		SharedPreferences preferences = context.getSharedPreferences(
				Constans.SP_NAME, Context.MODE_PRIVATE);
		preferences.edit().putString(key, value).commit();
	}

	public static String getStringExtra(Context context, String key,
			String defValue) {
		if(context == null)
			return "";
		SharedPreferences preferences = context.getSharedPreferences(
				Constans.SP_NAME, Context.MODE_PRIVATE);
		return preferences.getString(key, defValue);
	}

	public static boolean getBooleanExtra(Context context, String key,
			boolean defValue) {
		if(context == null)
			return false;
		SharedPreferences preferences = context.getSharedPreferences(
				Constans.SP_NAME, Context.MODE_PRIVATE);
		return preferences.getBoolean(key, defValue);
	}

	public static void putBooleanExtra(Context context, String key,
			boolean value) {
		if(context == null)
			return;
		SharedPreferences preferences = context.getSharedPreferences(
				Constans.SP_NAME, Context.MODE_PRIVATE);
		preferences.edit().putBoolean(key, value).commit();
	}
}
