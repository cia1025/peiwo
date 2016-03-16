package me.peiwo.peiwo.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;
import butterknife.OnClick;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.PWActionConfig;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.util.*;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

public class WelcomeActivity extends BaseActivity implements
        OnPageChangeListener {
    private static final int REQUEST_CODE_USERINIT = 1000;
    private boolean islogin = false;
    private ImageView[] dots;
    private int currentIndex;

    private static final int REQUEST_CODE_REGISTER = 2000;
    public static final int SOCIAL_TYPE_PHONE = 3;
    private ToggleButton mConvertEnvBtn;

    private IWXAPI mWXApi;
    //    private BroadcastReceiver wxReceiver;
    private MyHandler mHandler;
    //    private boolean stopResponseWXReceive = false;
    private static final int[] G_IMAGES = {R.drawable.guide_page_1, R.drawable.guide_page_2, R.drawable.guide_page_3, R.drawable.guide_page_4};
    private static final String[] MUSIC_PATH = {"guide_music/guide_music_1.mp3", "guide_music/guide_music_2.mp3", "guide_music/guide_music_3.mp3", "guide_music/guide_music_4.mp3"};
    HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        if (app.getStartWelcome()) {
            finish();
            return;
        }
        app.setStartWelcome(true);
        setContentView(R.layout.activity_welcome);
        EventBus.getDefault().register(this);
        //EventBus.getDefault().post(new Intent(PWActionConfig.ACTION_LOGIN_IN));
//        DisplayMetrics metrics = PWUtils.getMetrics(this);
//        Log.i("DisplayMetrics", "metrics.width == " + metrics.widthPixels);
//        Log.i("DisplayMetrics", "metrics.heightPixels == " + metrics.heightPixels);
//        metrics.heightPixels += metrics.heightPixels;
//        Log.i("DisplayMetrics", "metrics.width == " + metrics.widthPixels);
//        Log.i("DisplayMetrics", "metrics.heightPixels == " + metrics.heightPixels);

        mWXApi = WXAPIFactory.createWXAPI(this, Constans.WX_APP_ID, true);
        mWXApi.registerApp(Constans.WX_APP_ID);

//        wxReceiver = new WXReceiver();
//        registerReceiver(wxReceiver, new IntentFilter(PWActionConfig.ACTION_WXSHARE_SUCCESS));
        mHandler = new MyHandler(this);
        initDots();
        ViewPager mPager = (ViewPager) findViewById(R.id.viewpager);
        mPager.setAdapter(new GuideAdapter());
        //mPager.setOffscreenPageLimit(G_IMAGES.length);
        mPager.addOnPageChangeListener(this);
        boolean isDebugable = PeiwoApp.getApplication().isDebuggable();
        mConvertEnvBtn = (ToggleButton) findViewById(R.id.convert_enviroment_btn);
        if (isDebugable) {
            mConvertEnvBtn.setVisibility(View.VISIBLE);
            boolean isLastChecked = PeiwoApp.getApplication().isOnLineEnv();
            mConvertEnvBtn.setChecked(isLastChecked);
        }
        playMusicByIndex(0);

        if (hourGlassAgent.getStatistics() && hourGlassAgent.getK3() == 0) {
            hourGlassAgent.setK3(1);
            postK("k3");
        }
    }

    private void postK(String k) {
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        app.postK(k);
    }

    private void releasePlayer() {
//        Intent intent = new Intent(this, PlayerService.class);
//        intent.setAction(PlayerService.PLAY_ACTION_RELEASE);
//        startService(intent);
        //AudioPlayerUtil.releasePlayerNow(this);
        //PlayerService.getInstance().releaseIgnoreCaseCommand();
    }


    private void initDots() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.ll);

        dots = new ImageView[4];

        for (int i = 0; i < 4; i++) {
            dots[i] = (ImageView) ll.getChildAt(i);
            dots[i].setEnabled(true);
        }

        currentIndex = 0;
        dots[currentIndex].setEnabled(false);
    }

    private void setCurrentDot(int position) {
        if (position < 0 || position > 4 - 1 || currentIndex == position) {
            return;
        }

        dots[position].setEnabled(false);
        dots[currentIndex].setEnabled(true);

        currentIndex = position;
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int index) {
        setCurrentDot(index);
        if (index == G_IMAGES.length - 1) {
            findViewById(R.id.v_signup_action).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.v_signup_action).setVisibility(View.INVISIBLE);
        }
        playMusicByIndex(index);
        /**********/
        if (index == 1) {
            if (hourGlassAgent.getStatistics() && hourGlassAgent.getK4() == 0) {
                hourGlassAgent.setK4(1);
                postK("k4");
            }
        } else if (index == 2) {
            if (hourGlassAgent.getStatistics() && hourGlassAgent.getK5() == 0) {
                hourGlassAgent.setK5(1);
                postK("k5");
            }
        } else if (index == 3) {
            if (hourGlassAgent.getStatistics() && hourGlassAgent.getK6() == 0) {
                hourGlassAgent.setK6(1);
                postK("k6");
            }
        }
        /**********/
    }

    private void playMusicByIndex(int index) {
        //AudioPlayerUtil.playAudioByAssetsPath(this, MUSIC_PATH[index], false);
        //PlayerService playerService = PlayerService.getInstance();
        //playerService.playAssetFileCommand(playerService.getMusicAssetPath(this, MUSIC_PATH[index]), false);
    }

    @Override
    public void onResume() {
//        stopResponseWXReceive = false;
        super.onResume();
        //EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //EventBus.getDefault().register(this);
    }

//    class WXReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (stopResponseWXReceive) return;
//            String wxcode = intent.getStringExtra("wxcode");
//            signInWX(wxcode);
//        }
//    }


    static class MyHandler extends Handler {
        WeakReference<WelcomeActivity> activity_ref;

        public MyHandler(WelcomeActivity activity) {
            activity_ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            WelcomeActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing())
                return;
            int what = msg.what;
            switch (what) {
                case WHAT_DATA_RECEIVE:
                    theActivity.doHandleLogin();
                    break;
                case WHAT_DATA_RECEIVE_ERROR:
                    theActivity.dismissAnimLoading();
                    int errorCode = msg.arg1;
                    String tipString = "连接失败,请稍后重试(" + errorCode + ")";
                    switch (errorCode) {
                        case AsynHttpClient.PW_RESPONSE_DATA_NOT_AVAILABLE:
                            tipString = "此账号已被封禁";
                            break;
                        case AsynHttpClient.ERROR_MSG_NETWORK_NOT_AVAILABLE:
                            tipString = "网络连接失败";
                            break;
                        case AsynHttpClient.DATA_NOT_EXISTS:
                            tipString = "账号密码错误";
                            break;
                        case AsynHttpClient.PW_RESPONSE_OPERATE_ERROR:
                            tipString = "账号不存在";
                            break;
                    }
                    theActivity.showToast(theActivity, tipString);
                    break;
            }
            super.handleMessage(msg);
        }

    }


    /*private void signInWX(String wxcode) {
        showAnimLoading("", false, false, false);
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("social_type", "4"));
        params.add(new BasicNameValuePair("access_token", wxcode));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_ACCOUNT_SIGNIN, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                PWUserModel modle = new PWUserModel(data);
                if (UserManager.saveUser(WelcomeActivity.this, modle)) {
                    mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE);
                } else {
                    mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_ERROR);
                }
            }

            @Override
            public void onError(int error, Object ret) {
                Message msg = mHandler.obtainMessage();
                msg.what = WHAT_DATA_RECEIVE_ERROR;
                msg.arg1 = error;
                mHandler.sendMessage(msg);
            }
        });
    }*/

    @OnClick(R.id.iv_register)
    void goRegister() {
        if (hourGlassAgent.getStatistics() && hourGlassAgent.getK35() == 0) {
            hourGlassAgent.setK35(1);
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.postK("k35");
        }
        startActivity(new Intent(this, RegisterActivity.class));
    }

    @OnClick(R.id.iv_login)
    void goLogIn() {
        if (hourGlassAgent.getStatistics() && hourGlassAgent.getK17() == 0) {
            hourGlassAgent.setK17(1);
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.postK("k17");
        }
        startActivity(new Intent(this, PhoneLoginActivity.class));
    }

    public void click(View v) {
        if (PWUtils.isMultiClick())
            return;
        switch (v.getId()) {
//            case R.id.iv_register:
            // 注册
//			startActivityForResult(new Intent(this, RegisterActivity.class),
//					REQUEST_CODE_REGISTER);
            //微信注册
//                if (!mWXApi.isWXAppInstalled()) {
//                    showToast(this, getString(R.string.wechat_not_installed));
//                    return;
//                }
//                SendAuth.Req wxReq = new SendAuth.Req();
//                wxReq.scope = "snsapi_userinfo";
//                wxReq.state = PWUtils.getDeviceId(this);
//                mWXApi.sendReq(wxReq);
//                if (mHourGlassAgent.getStatistics() && mHourGlassAgent.getK7() == 0) {
//                    mHourGlassAgent.setK7(1);
//                    PeiwoApp app = (PeiwoApp) getApplicationContext();
//                    app.postK("k7");
//                }
//                break;
//            case R.id.tv_login:
//                // 登录
//                stopResponseWXReceive = true;
//                startActivity(new Intent(this, PhoneLoginActivity.class));
//                break;
            case R.id.convert_enviroment_btn:
                PeiwoApp app = PeiwoApp.getApplication();
                String toastStr = "";
                if (mConvertEnvBtn.isChecked()) {
                    app.goOnlineEnviroment();
                    toastStr = getResources().getString(R.string.go_online_enviroment);
                    CustomLog.d("go to online enviroment.");
                } else {
                    boolean isSuccessful = app.goOfflineEnvironment();
                    CustomLog.d("go to offline enviroment. is success ? " + isSuccessful);
                    toastStr = getResources().getString(R.string.go_offline_enviroment);
                }
                showToast(this, toastStr);
//			System.exit(0);
                break;
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        app.setStartWelcome(false);
        if (mWXApi != null) {
            mWXApi.unregisterApp();
        }
//        if (wxReceiver != null) {
//            unregisterReceiver(wxReceiver);
//            wxReceiver = null;
//        }
        super.onDestroy();
    }


    public void onEventMainThread(Intent intent) {
        if (PWActionConfig.ACTION_LOGIN_IN.equals(intent.getAction())) {
            finish();
        }
    }

    @Override
    public void finish() {
        EventBus.getDefault().unregister(this);
        if (islogin) {
            TcpProxy.getInstance().connectionTcp();
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.getRongCloudTokenAndConnect();
            app.setUpAtuserAndNodisturb();
        }
        releasePlayer();
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_USERINIT) {
            doRegistResult();
        } else if (resultCode == RESULT_OK
                && requestCode == REQUEST_CODE_REGISTER) {
            // 注册回调
            doRegistResult();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void doRegistResult() {
        doHandleLogin();
    }

    private void doHandleLogin() {
        // 微博或者QQ登陆时未绑定手机
        String phone = UserManager.getUserPhone(getApplicationContext());

        if (UserManager.getUserState(this) == UserManager.STATE_UNINITED) {
            // 未完善用户信息
            dismissAnimLoading();
            Intent intent = new Intent(this, UserDetailSettingActivity.class);
            if (TextUtils.isEmpty(phone)) {
                // 未绑定手机
                intent.putExtra("nophone", true);
            }
            startActivityForResult(intent, REQUEST_CODE_USERINIT);
        } else {
            islogin = true;
            startActivity(new Intent(this, MainActivity.class));
            dismissAnimLoading();
            reportTokenOrRegID();
            finish();
        }
    }

    private void reportTokenOrRegID() {
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        String token = SharedPreferencesUtil.getStringExtra(this,
                Constans.SP_KEY_XGTOKEN, "");
        String regId = SharedPreferencesUtil.getStringExtra(this,
                Constans.SP_KEY_XIAOMIREGID, "");
        if (!TextUtils.isEmpty(token)) {
            app.reportPushToken(token, Constans.PLATFORM_XG);
            return;
        }
        if (!TextUtils.isEmpty(regId)) {
            app.reportPushToken(regId, Constans.PLATFORM_XIAOMI);
        }
    }

    /*************************************************************************/
    public class GuideAdapter extends PagerAdapter {
        private SparseArray<ImageView> mViewCachedArray;
        private int reqWidth;
        private int reqHeight;

        public GuideAdapter() {
            int statusBarHeight = PWUtils.getStatusBarHeight(WelcomeActivity.this);
            DisplayMetrics metrics = PWUtils.getMetrics(WelcomeActivity.this);
            reqWidth = metrics.widthPixels;
            reqHeight = metrics.heightPixels - statusBarHeight;
            if (reqWidth >= 1080) {
                reqWidth = 720;
            }
            if (reqHeight >= 1920 - statusBarHeight) {
                reqHeight = 1280 - statusBarHeight;
            }
        }

        @Override
        public int getCount() {
            return G_IMAGES.length;//== null ? 0 : mImageList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (mViewCachedArray == null) {
                mViewCachedArray = new SparseArray<>();
            }

            ImageView topLayout = mViewCachedArray.get(position);
            if (topLayout == null) {
                topLayout = new ImageView(WelcomeActivity.this);
                ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                );
                topLayout.setLayoutParams(tlp);
                topLayout.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mViewCachedArray.put(position, topLayout);
            } else {
                topLayout = mViewCachedArray.get(position);
            }
            setImageRes(G_IMAGES[position], topLayout);
            //topLayout.setImageResource(G_IMAGES[position]);
            container.addView(topLayout);

            return topLayout;
        }

        private void setImageRes(int gImage, ImageView topLayout) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(getResources(), gImage, options);
                options.inSampleSize = ImageUtil.calculateInSampleSize(options, reqWidth, reqHeight);
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                topLayout.setImageBitmap(BitmapFactory.decodeResource(getResources(), gImage, options));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            final View v = mViewCachedArray.get(position);
            if (v != null) {
                container.removeView(v);
            }
        }

    }
}
