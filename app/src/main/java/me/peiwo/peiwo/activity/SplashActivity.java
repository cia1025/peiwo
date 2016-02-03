package me.peiwo.peiwo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.PWActionConfig;
import me.peiwo.peiwo.util.HourGlassAgent;
import me.peiwo.peiwo.util.SharedPreferencesUtil;
import me.peiwo.peiwo.util.UserManager;

public class SplashActivity extends Activity {

    private static final int max_delay = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
        if (hourGlassAgent.getStatistics() && hourGlassAgent.getK2() == 0) {
            hourGlassAgent.setK2(1);
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.postK("k2");
        }
        init();
    }


    private void init() {
        if (getIntent() != null
                && PWActionConfig.ACTION_SERVER_DOWNTIME.equals(getIntent()
                .getAction())) {
            getServerState();
            return;
        }
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        app.cleanImages();
        setUpStartUpScreen();
    }

    private void delayPop() {
        new Handler().postDelayed(() -> {
            if (!UserManager.isLogin(SplashActivity.this)) {
                PeiwoApp app = (PeiwoApp) getApplicationContext();
                if (app.getStartWelcome()) {
                    finish();
                    return;
                }
                Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);

            } else {
                SharedPreferencesUtil.putBooleanExtra(SplashActivity.this, "old_user_" + UserManager.getUid(SplashActivity.this), false);
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(mainIntent);
                PeiwoApp.getApplication().initUserBackground();
            }
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.loadStartUpScreenImg();
            finish();
        }, max_delay);
    }

    private void setUpStartUpScreen() {
        ImageView iv_startup_screen = (ImageView) findViewById(R.id.iv_startup_screen);
        String startup_screen_url = SharedPreferencesUtil.getStringExtra(this, "startup_screen_url", null);
        if (!TextUtils.isEmpty(startup_screen_url)) {
            showScreenImage(startup_screen_url, iv_startup_screen);
        } else {
            delayPop();
        }
    }

    private void showScreenImage(String startup_screen_url, ImageView iv_startup_screen) {
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(false).build();
        ImageLoader.getInstance().displayImage(startup_screen_url, iv_startup_screen, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                delayPop();
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                delayPop();
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                delayPop();
            }
        });
    }

    private void getServerState() {
        alertServerDownTime();
/*		ApiRequestWrapper.getServerState(this, new MsgStructure() {
            @Override
			public void onReceive(JSONObject data) {

			}

			@Override
			public void onError(int error, Object ret) {
				try {
					if (ret instanceof JSONObject) {
						// {"server": {"state": 1}}
						JSONObject o = (JSONObject) ret;
						if (o.has("server")) {
							JSONObject oo = o.getJSONObject("server");
							if (oo.optInt("state", -1) == 2) {
								
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		});*/
    }

    private void alertServerDownTime() {
        final ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.server_down);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(SplashActivity.this)
                        .setView(iv)
                        .setOnCancelListener(
                                new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface arg0) {
                                        finish();
                                    }
                                })
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        finish();
                                    }
                                }).create().show();
            }
        });
    }
}
