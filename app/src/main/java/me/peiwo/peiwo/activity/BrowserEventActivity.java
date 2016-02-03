package me.peiwo.peiwo.activity;

import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.util.UserManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class BrowserEventActivity extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String scheme = intent.getScheme();
		Uri uri = intent.getData();
		System.out.println("scheme:" + scheme);// <a
												// href="peiwo://contact_info?uid=2088">test</a>
		if (uri != null && "peiwo".equals(scheme)) {
			String host = uri.getHost(); // host:contact_info
			int uid = 0;
			try {
				uid = Integer.valueOf(uri.getQueryParameter("uid"));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			
			if (!UserManager.isLogin(this)) {
				PeiwoApp app = (PeiwoApp) getApplicationContext();
				if (app.getStartWelcome()) {
					finish();
					return;
				}
                Intent newIntent = new Intent(this, WelcomeActivity.class);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(newIntent);
			} else {
				if ("contact_info".equals(host)) {
					Intent newIntent = new Intent(this, UserInfoActivity.class);
					newIntent.putExtra(UserInfoActivity.TARGET_UID, uid);
					startActivity(newIntent);
				} else {
	                Intent mainIntent = new Intent(this, MainActivity.class);
	                mainIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	                startActivity(mainIntent);
	                PeiwoApp.getApplication().initUserBackground();
				}
			}
		}
		finish();
	}
}
