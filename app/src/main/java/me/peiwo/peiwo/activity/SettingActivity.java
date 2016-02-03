package me.peiwo.peiwo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.tencent.connect.auth.QQAuth;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.model.UserInfo;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.PWActionConfig;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.util.*;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SettingActivity extends BaseActivity implements
        OnClickListener {

    private static final int REQUEST_CODE_BIND = 5000;
    private static final int REQUEST_CODE_RESET = 6000;
    private TextView tv_bind_status;
    private FeedbackAgent mFbAgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);

        mFbAgent = new FeedbackAgent(this);
        mFbAgent.sync();
        initView();
    }

    private void initView() {
        TitleUtil.setTitleBar(this, "设置", new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        }, null);
        TextView mTvPrivacySetting = (TextView) findViewById(R.id.privacy_setting);
        mTvPrivacySetting.setOnClickListener(this);
        TextView mTvJudgeUs = (TextView) findViewById(R.id.judge_us);
        mTvJudgeUs.setOnClickListener(this);
//        TextView mTvUpgrade = (TextView) findViewById(R.id.upgrade);
//        mTvUpgrade.setOnClickListener(this);

        TextView mTvCacheClean = (TextView) findViewById(R.id.cache_clean);
        mTvCacheClean.setOnClickListener(this);

        Button mBtnLogout = (Button) findViewById(R.id.setting_logout);
        mBtnLogout.setOnClickListener(this);
        Button mBtnFeedBack = (Button) findViewById(R.id.setting_feedback);
        mBtnFeedBack.setOnClickListener(this);

//        TextView tv_bind_phonenumber = (TextView) findViewById(R.id.tv_bind_phonenumber);
        //tv_bind_phonenumber.setOnClickListener(this);
        TextView tv_change_password = (TextView) findViewById(R.id.tv_change_password);
        tv_change_password.setOnClickListener(this);
        View rl_bind_phone = findViewById(R.id.rl_bind_phone);
        rl_bind_phone.setOnClickListener(this);
        TextView tv_black_list = (TextView) findViewById(R.id.black_list);
        tv_black_list.setOnClickListener(this);
        TextView tv_find_nerver = (TextView) findViewById(R.id.tv_find_nerver);
        tv_bind_status = (TextView) findViewById(R.id.tv_bind_status);
        if (!TextUtils.isEmpty(UserManager.getPWUserPhone(this))) {
            tv_bind_status.setText("已绑定");
        }

        int ver = SharedPreferencesUtil.getIntExtra(this, Constans.SP_KEY_SERVER_APPVER, 0);
        int localVer = Integer.valueOf(PWUtils.getVersionCode(this).replace(".", ""));
        if (ver != 0 && ver > localVer) {
            findViewById(R.id.tv_find_nerver).setVisibility(View.VISIBLE);
            findViewById(R.id.rl_upgrade).setOnClickListener(this);
        } else {
            tv_find_nerver.setVisibility(View.VISIBLE);
            tv_find_nerver.setText("已是最新版本");
            findViewById(R.id.rl_upgrade).setOnClickListener(v -> showToast(SettingActivity.this, "当前已是最新版本"));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.privacy_setting:
                startActivity(new Intent(this, MsgSettingActivity.class));
                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMECLICKMESSAGESET);
                break;
            case R.id.setting_feedback: {
                setuserInfo();
                mFbAgent.startFeedbackActivity();
            }
            break;
            case R.id.judge_us:
                startActivity(new Intent(SettingActivity.this, AboutUsActivity.class));
                break;
            case R.id.rl_upgrade:
                String data_str = SharedPreferencesUtil.getStringExtra(this, Constans.SP_KEY_UPGRADE_DATA, "");
                if (TextUtils.isEmpty(data_str)) return;

                Intent intent = new Intent(SettingActivity.this, UpgradeAppActivity.class);
                intent.putExtra("data_str", data_str);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

//            case R.id.tv_wildcatguide:
//            	Intent guidIntent=new Intent(SettingActivity.this,StartWildCatGuideActivity.class);
//            	guidIntent.putExtra("isFromSetting", true);
//            	startActivity(guidIntent);
//            	break;
            case R.id.black_list:
                startActivity(new Intent(SettingActivity.this,
                        BlockListActivity.class));
                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMESETSBLACK);
                break;
            case R.id.cache_clean:
                File cacheDir = FileManager.getTempFilePath();
                if (cacheDir.exists()) {
                    File[] fList = cacheDir.listFiles();
                    if (fList != null && fList.length > 0) {
                        for (File f : fList) {
                            f.delete();
                        }
                    }
                }
                cacheDir = FileManager.getImagePath();
                if (cacheDir.exists()) {
                    File[] fList = cacheDir.listFiles();
                    if (fList != null && fList.length > 0) {
                        for (File f : fList) {
                            f.delete();
                        }
                    }
                }
                cacheDir = FileManager.getLogPath();
                if (cacheDir.exists()) {
                    File[] fList = cacheDir.listFiles();
                    if (fList != null && fList.length > 0) {
                        for (File f : fList) {
                            f.delete();
                        }
                    }
                }
                cacheDir = FileManager.getVoicePath();
                if (cacheDir.exists()) {
                    File[] fList = cacheDir.listFiles();
                    if (fList != null && fList.length > 0) {
                        for (File f : fList) {
                            f.delete();
                        }
                    }
                }
                showToast(this, "已清除…");
                break;
            case R.id.setting_logout:
                PeiwoApp app = (PeiwoApp) getApplicationContext();
                if (app.getIsCalling()) {
                    showToast(this, "您当前正在通话");
                    return;
                }
                logoutUser();
                break;
            case R.id.rl_bind_phone:
                bindPhoneNumber();
                break;
            case R.id.tv_change_password:
                changePassWord();
                break;
            default:
                break;
        }
    }

    private void bindPhoneNumber() {
        // 进绑定手机界面
        final String phone = UserManager.getPWUserPhone(this);
        if (!TextUtils.isEmpty(phone)) {
            new AlertDialog.Builder(this)
                    .setMessage(String.format(Locale.getDefault(), "您已绑定手机号%s,现在可通过手机号和陪我号登录陪我了！", PWUtils.getRealPhone(phone)))
                    .setNegativeButton("取消", null)
                    .setPositiveButton("修改绑定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(SettingActivity.this, ResetPhoneActivity.class);
                            intent.putExtra(ResetPhoneActivity.KEY_PHONENO, phone);
                            startActivityForResult(intent, REQUEST_CODE_RESET);
                        }
                    })
                    .create().show();
        } else {
            //不同的activity
            Intent intent = new Intent(SettingActivity.this, BindPhoneActivity.class);
            startActivityForResult(intent, REQUEST_CODE_BIND);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_BIND:
                    tv_bind_status.setText("已绑定");
                    break;
                case REQUEST_CODE_RESET:
                    tv_bind_status.setText("已绑定");
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void changePassWord() {
        if (TextUtils.isEmpty(UserManager.getPWUserPhone(this))) {
            // PPAlert.showToast(this, "")
            new AlertDialog.Builder(this)
                    .setMessage("您还没有绑定手机号，无法修改登录密码")
                    .setPositiveButton("去绑定",
                            (dialog, which) -> {
                                // 去手机绑定界面
                                bindPhoneNumber();
                            }).setNegativeButton("取消", null).create().show();
        } else {
            // 去修改密码界面
            startActivity(new Intent(this, ModifyPassWordActivty.class));
        }
    }

    private void setuserInfo() {
        UserInfo info = mFbAgent.getUserInfo();
        if (info == null)
            info = new UserInfo();
        Map<String, String> contact = info.getContact();
        if (contact == null)
            contact = new HashMap<>();
        contact.put("peiwo_id", String.valueOf(UserManager.getPWUser(this).uid));

        info.setContact(contact);
        mFbAgent.setUserInfo(info);
    }

    private void logoutUser() {
        showAnimLoading();
        ApiRequestWrapper.signout(this, UserManager.getUid(SettingActivity.this), new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                System.out.println("SettingActivity logout User ");
                dismissAnimLoading();
                QQAuth mQQAuth = QQAuth.createInstance(Constans.QQ_APP_ID, getApplicationContext());
                mQQAuth.logout(SettingActivity.this);
                PeiwoApp.getApplication().logoutLocalUser();
                // setResult(MainActivity.RESULT_LOGOUT);
                sendBroadcast(new Intent(PWActionConfig.ACTION_LOGIN_OUT));

                TcpProxy.getInstance().disconnectionTcp();
                finish();
            }

            @Override
            public void onError(int error, Object ret) {
                dismissAnimLoading();
                runOnUiThread(new Runnable() {
                    public void run() {
                        showToast(SettingActivity.this, getResources().getString(R.string.err_http_req));
                    }
                });
            }
        });
    }

}
