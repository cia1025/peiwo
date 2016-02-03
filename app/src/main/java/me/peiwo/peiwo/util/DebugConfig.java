package me.peiwo.peiwo.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.util.TextUtils;
import org.json.JSONObject;

import android.content.Context;

public class DebugConfig {
	private static DebugConfig instance = null;

	public static DebugConfig getInstance() {
		if (instance == null) {
			instance = new DebugConfig();
		}
		return instance;
	}

	
	private String build_version_hash = null;
	private String build_version = null;
	private String build_version_manual = null;
	private String debug_available = null;
	
	public void readConfig(Context mContext) {
		InputStream ins = null;
		InputStreamReader inReader = null;
		BufferedReader reader = null;
		try {
			ins = mContext.getAssets().open("txt/debug_config.txt");
			inReader = new InputStreamReader(ins, "utf-8");
			reader = new BufferedReader(inReader);
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			JSONObject o = new JSONObject(sb.toString());
			build_version_hash = o.optString("build_version_hash");
			build_version = o.optString("build_version");
			build_version_manual = o.optString("build_version_manual");
			debug_available = o.optString("debug_available");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (inReader != null)
					inReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (ins != null)
					ins.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getBuild_version_hash() {
		if (build_version_hash == null) {
			return "";
		}
		return build_version_hash;
	}

	public String getBuild_version() {
		if (build_version == null) {
			return "";
		}
		return build_version;
	}

	public String getBuild_version_manual() {
		if (build_version_manual == null) {
			return "";
		}
		return build_version_manual;
	}
	
	public boolean isDebug_available() {
		if (!TextUtils.isEmpty(debug_available) && debug_available.equals("1")) {
			return true;
		}
		return false;
	}
}
