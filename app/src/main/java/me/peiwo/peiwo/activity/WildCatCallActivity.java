package me.peiwo.peiwo.activity;

import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.PWActionConfig;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.CallStateEvent;
import me.peiwo.peiwo.eventbus.event.WildCatCallingEvent;
import me.peiwo.peiwo.eventbus.event.WildCatMatchStateEvent;
import me.peiwo.peiwo.eventbus.event.WildCatMessageEvent;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.NetUtil;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.service.CoreService;
import me.peiwo.peiwo.service.CoreService.WildCatState;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.HourGlassAgent;
import me.peiwo.peiwo.util.ImageUtil;
import me.peiwo.peiwo.util.LinphoneManager;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.SharedPreferencesUtil;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.CallScrollView;
import me.peiwo.peiwo.widget.FlowLayout;

/**
 * Created by fuhaidong on 14-9-4. 随机通话界面
 */
public class WildCatCallActivity extends BaseCallActivity implements
        CallScrollView.OnScrollUpFinishListener {
    private HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
    public static final String K_CONSTELL = "constell";
    private String match_constell;

    public static final String START_MAIN = "start_main";

    public static final int HANDLE_RECONNECTION_TCP_EVENT = 0x10001;
    public static final int HANDLE_TCP_CONNECT_TIMEOUT = 0x10002;

    public static final int HANDLE_NET_STATUS_CHANGE = 0x11000;

    public static final int HANDLE_WILD_CAT_MATCH_STATE = 0x12000;

    public static final int HANDLE_WILD_CAT_DRAW_PROGRESSBAR = 0x12010;
    public static final int HANDLE_WILD_CAT_WAIT_TIMEOUT = 0x12011;

    private WildCatHandler mHandler;
    private ImageView iv_reputation;
    private View ll_action_img_container;
    private TextView wildcat_state_text;
    private TextView tv_hint_tips;
    private TextView tv_def_tips;
    // private View iv_refreshcat;
    /**
     * 外放模式下select为false,听筒模式下select为true
     */
    private View tv_voice_mode;
    private TextView tv_reputation_tips;
    private TextView wildcat_countdown_view;
    private TextView tv_prompt;
    private View view_wild_tag;

    private TextView tv_net_status;
    private View ll_reputation_tips_parent;
    private TextView wildcat_time_text;
    private int mTargetUid; // 表示正在聊天对方的uid
    //private int mUid; // 自己的uid
    private int mGender = 3;
    private int my_gender;
    private int counttimeH; // 小时
    private int counttimeM; // 分钟
    private int counttimeS; // 秒
    private boolean isbreak_mode_countdown = false;
    private boolean isbreak_mode_countlimit = false;

    private CallScrollView slideView;
    private ImageView wildcat_anim_image1;
    private ImageView wildcat_anim_image2;


    private ImageView wildcat_avatar_image;
    private TextView wildcat_name_text;


    private SoundPool mSoundPool;
    private int soundIdPpdl; // 匹配到了
    private int soundIdDz; // 点赞
    private boolean isWiredHeadsetOn = false;

    private boolean isactionforme = false; // 自己主动操作挂断

    private CHAT_MODE mChatMode = CHAT_MODE.MODE_COUNTDOWN;

    private boolean needAlertShareDialog = false;

    private PhoneStateListener phoneListener;

    //private View ct_tips;
    private HeadSetPlugReceiver headSetPlugReceiver;

    private NotificationManager mNotifyMgr;

    private Timer wildCatTimer = null;
    private Timer wildCatHintTimer = null;
    private static int currentState = 10000;
    private static final int YANG_SHENG_QI = 10000;
    private static final int TING_TONG = 10001;

    private JSONObject userJson;
//    private int recoredSecond;
//	private MediaRecorder recorder;
//	private String filePath;

    private AudioManager audioManager = null;
    private int wait_time = 5;
    private FlowLayout wildcat_tags_layout;
    private String call_id;
    private int time_stamp;

    @Override
    public void onScrollUpFinish() {
        runInBackground();
    }

    public enum WILDCAT_STATE {
        /**
         * 等待匹配状态
         */
        MATCHING_STATE,
        /**
         * 等待用户接听状态
         */
        WAIT_ANSWER_STATE,
        /**
         * 接听后等待WEBRTC连接成功
         */
        WAIT_CALLING_STATE,
        /**
         * WEBRTC连接成功，通话中
         */
        CALLING_STATE,
        //默认
        NORMAL
    }

    private WILDCAT_STATE wildcat_state;

    private enum CHAT_MODE {
        /**
         * 倒计时模式
         */
        MODE_COUNTDOWN,
        /**
         * 无限模式
         */
        MODE_NOTIME_LIMIT
    }


    public void onCreate(Bundle savedInstanceState) {
        DfineAction.CURRENT_CALL_STATUS = DfineAction.CURRENT_CALL_WILDCAT;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        // getWindow().getDecorView().setBackgroundColor(Color.parseColor("#2B3032"));
        View root = getLayoutInflater().inflate(R.layout.activity_wildcat, null);
        slideView = new CallScrollView(this);
        slideView.addView(root, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        slideView.setOnScrollUpFinishListener(this);
        setContentView(slideView);
        match_constell = getIntent().getStringExtra(K_CONSTELL);
        my_gender = UserManager.getGender(this);
        mGender = my_gender == 1 ? 2 : 1;
        if (mGender == 1) {
            //findViewById(R.id.tv_dasahng).setVisibility(View.GONE);
            //打赏全面开放
        }

        initView(root);
        time_stamp = (int) (System.currentTimeMillis() / 1000);


        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isWiredHeadsetOn()) {
            //进入限时聊前就有插入耳机
            isWiredHeadsetOn = true;
        }
        EventBus.getDefault().register(this);

        headSetPlugReceiver = new HeadSetPlugReceiver();
        registerReceiver(headSetPlugReceiver, new IntentFilter("android.intent.action.HEADSET_PLUG"));

        mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PeiwoApp.getApplication().wildCatCallNotification = false;
        PeiwoApp.getApplication().wildCatCallNotificationReStart = false;

        // 连上service之后开始随机通话
        // 1 男 2 女 3 all 希望匹配的性别
        if (TcpProxy.getInstance().isLoginStauts()) {
            timeSendWildCat();
        } else {
            wildcat_state_text.setText(getString(R.string.wild_cat_tcp_connecting));
            mHandler.sendEmptyMessageDelayed(HANDLE_RECONNECTION_TCP_EVENT, 5000);// 延迟等待5s
            mHandler.sendEmptyMessageDelayed(HANDLE_TCP_CONNECT_TIMEOUT, 20000);// 延迟等待20s
        }

        PWUtils.getWildcatShareData(this, null);
        startInvalidLoopHint(true);
    }


    @Override
    public void onPause() {
        LinphoneManager.getInstance().stopProximitySensorForActivity(this);
        if (!isactionforme) {//主动退出时不显示通知
            notificationCall();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!isactionforme) {
            try {
                ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
                if (runningTaskInfos != null) {
                    String topString = (runningTaskInfos.get(0).topActivity).toString();
                    if (!TextUtils.isEmpty(topString) && !topString.contains("me.peiwo")) {
                        PeiwoApp.getApplication().wildCatCallNotificationReStart = true;
                    } else {
                        PeiwoApp.getApplication().wildCatCallNotificationReStart = false;
                    }
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private void initView(View root) {
        view_wild_tag = findViewById(R.id.view_wild_tag);
        tv_hint_tips = (TextView) findViewById(R.id.tv_hint_tips);
        tv_def_tips = (TextView) findViewById(R.id.tv_def_tips);
        //mUid = mUser.uid;
        //赞
        iv_reputation = (ImageView) root.findViewById(R.id.iv_reputation);
        //时间显示
        wildcat_time_text = (TextView) root.findViewById(R.id.wildcat_time_text);
        //时间标签（倒计时、无限时）
        wildcat_countdown_view = (TextView) root.findViewById(R.id.wildcat_countdown_view);
        wildcat_state_text = (TextView) root.findViewById(R.id.wildcat_state_text);

        tv_prompt = (TextView) root.findViewById(R.id.tv_prompt);
        // iv_refreshcat = findViewById(R.id.iv_refreshcat);
        tv_voice_mode = root.findViewById(R.id.tv_voice_mode);

        //喜欢就点赞
        tv_reputation_tips = (TextView) root.findViewById(R.id.tv_reputation_tips);
        ll_reputation_tips_parent = root.findViewById(R.id.ll_reputation_tips_parent);

        //底点按钮
        ll_action_img_container = root.findViewById(R.id.ll_action_img_container);
        //顶部提示
        //ct_tips = root.findViewById(R.id.ct_tips);

        wildcat_tags_layout = (FlowLayout) root.findViewById(R.id.wildcat_tags_layout);
        wildcat_tags_layout.setMaxLine(2);
        //网络状态
        tv_net_status = (TextView) root.findViewById(R.id.tv_net_status);
        tv_net_status.setText("");

        wildcat_avatar_image = (ImageView) root.findViewById(R.id.wildcat_avatar_image);
        wildcat_name_text = (TextView) root.findViewById(R.id.wildcat_name_text);
        wildcat_anim_image1 = (ImageView) findViewById(R.id.wildcat_anim_image1);
        wildcat_anim_image2 = (ImageView) findViewById(R.id.wildcat_anim_image2);


        mHandler = new WildCatHandler(this);

        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundIdPpdl = mSoundPool.load(this, R.raw.ppdl, 1);
        soundIdDz = mSoundPool.load(this, R.raw.dz, 1);
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        app.setCalling(true, PeiwoApp.CALL_TYPE.CALL_WILD);

        listenerSystemCallState();

        changeViewState(WILDCAT_STATE.MATCHING_STATE);
    }


    private TelephonyManager manager = null;

    private void listenerSystemCallState() {
        manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        phoneListener = new MyPhoneListener();
        manager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    protected void cancelListenerSystemCallState() {
        if (manager != null && phoneListener != null) {
            manager.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    /**
     * 定时发送申请匹配
     */
    private void timeSendWildCat() {
        if (wildCatTimer != null) {
            wildCatTimer.cancel();
        }
        wildCatTimer = new Timer();
        wildCatTimer.schedule(new TimerTask() {
            private int count = 0;

            @Override
            public void run() {
                CustomLog.i(DfineAction.TCP_TAG, "wildCatTimer count = " + count);
                if (wildcat_state == WILDCAT_STATE.MATCHING_STATE) {
                    CustomLog.i(DfineAction.TCP_TAG, "wildCatTimer send wildcat, time_stamp == " + time_stamp);
                    if (count == 0) {
                        TcpProxy.getInstance().sendWildcatMessage(mGender, 0, time_stamp, match_constell);
                    } else {
                        TcpProxy.getInstance().sendWildcatMessage(mGender, 1, time_stamp, match_constell);
                    }
                }
                count++;
            }
        }, 500, 20 * 1000);
    }

    static class WildCatHandler extends Handler {
        WeakReference<WildCatCallActivity> activity_ref = null;

        public WildCatHandler(WildCatCallActivity activity) {
            activity_ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            //CustomLog.i("msg data == " + msg.getData());
            final WildCatCallActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) {
                return;
            }
            Resources res = theActivity.getResources();
            int what = msg.what;
            switch (what) {
                case DfineAction.IntentRewardResponseMessage:
                    //收到打赏的回执
                    theActivity.handleIntentRewardResponseMessage(theActivity.call_id, theActivity.mTargetUid, theActivity.my_gender, msg.getData());
                    break;
                case DfineAction.PayRewardResponseMessage:
                    theActivity.hanlePayRewardResponseMessage(msg.getData());
                    break;
                case DfineAction.RewardedMessage:
                    //type 匿名聊1，打电话2
                    theActivity.handleRewardedMessage(theActivity.mTargetUid, theActivity.userJson.optString("pic"), theActivity.userJson.optString("nickname"), "1", msg.getData());
                    break;
                case HANDLE_RECONNECTION_TCP_EVENT: {
                    if (TcpProxy.getInstance().isLoginStauts()) {
                        theActivity.wildcat_state_text.setText(theActivity.getString(R.string.wild_cat_matching));
                        theActivity.timeSendWildCat();
                        removeMessages(HANDLE_TCP_CONNECT_TIMEOUT);
                    } else {
                        sendEmptyMessageDelayed(HANDLE_RECONNECTION_TCP_EVENT, 2000);
                    }
                }
                break;
                case HANDLE_TCP_CONNECT_TIMEOUT: {
                    theActivity.showToast(theActivity, res.getString(R.string.network_not_stable));
                    theActivity.endCallActivity(DfineAction.WILDCAT_STOP_CALL_EXIT);
                }
                break;


                case CoreService.HANDLE_WILDCAT_UI_MATCH_SUCCESS: {
                    try {
                        Bundle b = msg.getData();
                        if (b != null) {
                            theActivity.userJson = new JSONObject(b.getString("user"));
                            //String reportsString = b.getString("reports");
                            theActivity.wait_time = b.getInt("wait_time");
                            //mGender是用来表示希望匹配的性别，不是自己的性别，举报的时候女的被举报是不显示的
//                            if (mGender == AsynHttpClient.GENDER_MASK_MALE) {
//                                if (userJson.has("welcome_percent")) {
//                                    showReportAlert(userJson.optDouble("welcome_percent"));
//                                }
//                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    theActivity.changeViewState(WILDCAT_STATE.WAIT_ANSWER_STATE);
                }
                break;
                case CoreService.HANDLE_WILDCAT_UI_CALL_READY: {
                    try {
                        Bundle b = msg.getData();
                        if (b != null) {
                            JSONObject data = new JSONObject(b.getString("data"));
                            theActivity.call_id = data.optString("call_id");
                            theActivity.userJson = new JSONObject(b.getString("user"));
                            theActivity.setUserData();
                            JSONArray hint_array = theActivity.userJson.has("hint") ? theActivity.userJson.getJSONArray("hint") : null;
                            //男女都显示标签，如果服务器不返回就不显示
                            theActivity.showReportAlert(hint_array);
                            theActivity.mTargetUid = data.optInt("tuid", 0);
                            //mGender是用来表示希望匹配的性别，不是自己的性别，举报的时候女的被举报是不显示的
                            if (theActivity.mGender == AsynHttpClient.GENDER_MASK_MALE) {
                                if (data.has("welcome_percent")) {
                                    //showReportAlert(data.optDouble("welcome_percent"));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    theActivity.changeViewState(WILDCAT_STATE.WAIT_CALLING_STATE);
                }
                break;
                case CoreService.HANDLE_WILDCAT_UI_CANCEL_ANSWER:

                    theActivity.substitutionUser(DfineAction.WILDCAT_STOP_CALL_NORMAL);
                    break;
                case CoreService.STOP_CALL:
                    // 挂断
                    Bundle bundle = msg.getData();
                    boolean stopMatch = false;
                    try {
                        if (bundle != null) {
                            JSONObject data = new JSONObject(bundle.getString("data"));
                            stopMatch = data.getInt("stop_match") == 1;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    CustomLog.d("STOP_CALL. stop match is : " + stopMatch);
                    if (PeiwoApp.getApplication().getNetType() == NetUtil.NO_NETWORK) {
                        theActivity.isactionforme = true;
                    }
                    if (theActivity.isactionforme) {
                        theActivity.finish();
                    } else if (stopMatch) {
                        theActivity.isactionforme = true;
                        theActivity.showToast(theActivity, theActivity.getResources().getString(R.string.be_reported_warning));
                        theActivity.finish();
                    } else {
                        theActivity.reWildCat();
                        theActivity.timeSendWildCat();
                    }
//              stopRecord();
                    break;
                case CoreService.CALL_BEGIN_RESPONSE:
                    // 变换view 开始打电话
                    if (theActivity.isWiredHeadsetOn) {
                        theActivity.setVoiceModeCommunication();
                    } else {
                        switch (currentState) {
                            case YANG_SHENG_QI:
                                theActivity.setVoiceModeNomal();
                                break;
                            case TING_TONG:
                                theActivity.setVoiceModeCommunication();
                                break;
                        }
                        currentState = YANG_SHENG_QI;
                    }
                    theActivity.mSoundPool.play(theActivity.soundIdPpdl, 1.0f, 1.0f, 0, 0, 1.0f);
                    theActivity.mChatMode = CHAT_MODE.MODE_COUNTDOWN;
                    theActivity.changeViewState(WILDCAT_STATE.CALLING_STATE);
                    theActivity.countTime();
                    theActivity.needAlertShareDialog = true;
                    EventBus.getDefault().post(new WildCatCallingEvent(true));
                    HourGlassAgent hourGlass = HourGlassAgent.getInstance();
                    if (hourGlass.getStatistics() && hourGlass.getK27() == 0) {
                        hourGlass.setK27(1);
                        PeiwoApp app = PeiwoApp.getApplication();
                        app.postK("k27");
                    }
                    hourGlass.setStatistics(false);
                    break;
                case CoreService.WILDCAT_BE_FORBIDDEN_:
                    // 匹配过程中点击断开随机匹配的
                    //测试提交git分支
                    Bundle data = msg.getData();
                    String notify = data.getString("notify");
                    theActivity.isactionforme = true;
                    //十分钟后关闭界面
                    theActivity.changeViewState(WILDCAT_STATE.MATCHING_STATE);

                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!theActivity.isFinishing()) {
                                theActivity.finish();
                            }
                        }
                    }, 12 * 1000);
                    break;
                case CoreService.WILDCAT_REPUTATION_NICE_MESSAGE:
                    // 无限时通话
                    theActivity.mSoundPool.play(theActivity.soundIdDz, 1.0f, 1.0f, 0, 0, 1.0f);
                    theActivity.ll_reputation_tips_parent.setVisibility(View.GONE);
                    theActivity.mChatMode = CHAT_MODE.MODE_NOTIME_LIMIT;
                    theActivity.isbreak_mode_countdown = true;
                    //双方互相点赞，启用添加好友按钮
//                    theActivity.vibratorPhone();
                    theActivity.countTime();
                    break;
                case CoreService.CALL_STATE_CHANGE: {
                    CallStateEvent event = (CallStateEvent) msg.obj;
                    if (!event.nTCPState) {
                        theActivity.tv_net_status.setText(R.string.calling_me_net_bad);
                    }
                }
                break;
                case HANDLE_NET_STATUS_CHANGE: {
                    CallStateEvent event = (CallStateEvent) msg.obj;
                    if (event.heart_lost_count > 0) {//自己丢包
                        theActivity.tv_net_status.setText(R.string.calling_me_net_bad);
                    } else {
                        if (event.remote_user_state > 0) {
                            theActivity.tv_net_status.setText(R.string.calling_other_net_bad);
                        } else {
                            theActivity.tv_net_status.setText("");
                        }
                    }
                }
                break;
                case HANDLE_WILD_CAT_MATCH_STATE: {
                    WildCatMatchStateEvent event = (WildCatMatchStateEvent) msg.obj;
                    if (event.wildcat_state == -1 && (CoreService.wildcatState == WildCatState.MATCHING
                            || CoreService.wildcatState == WildCatState.IDEL)) {
                        theActivity.timeSendWildCat();
                    } else {
                    /*if (tv_cat.getVisibility() == View.VISIBLE
                            && tv_cat.getText().toString().contains(getString(R.string.wild_cat_matching))){
		    			tv_cat.setText(getString(R.string.wild_cat_matching) + "(" + event.wildcat_state + ")");
		    		}*/
                    }
                }
                break;
                case HANDLE_WILD_CAT_DRAW_PROGRESSBAR:

                    break;
                case HANDLE_WILD_CAT_WAIT_TIMEOUT:
                    TcpProxy.getInstance().sendWildcatRequestCallReady();
                    theActivity.changeViewState(WILDCAT_STATE.WAIT_CALLING_STATE);
                    break;
            }
        }
    }

    private void setPromptValues(JSONArray hint_array, ImageView iv_wild_tag) {
        try {
            if (hint_array != null) {
                JSONObject object = hint_array.getJSONObject(0);
                if (object != null) {
                    view_wild_tag.setVisibility(View.VISIBLE);
                    tv_prompt.setText(object.getString("msg"));
                    switch (object.getInt("style")) {
                        case 1:
                            tv_prompt.setBackgroundResource(R.drawable.bg_wild_tag_yellow);
                            iv_wild_tag.setImageResource(R.drawable.ic_wild_tag_bad);
                            break;
                        case 2:
                            tv_prompt.setBackgroundResource(R.drawable.bg_wild_tag_green);
                            iv_wild_tag.setImageResource(R.drawable.ic_wild_tag_good);
                            break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showReportAlert(final JSONArray hint_array) {
        //CustomLog.i("showReportAlert(final JSONArray hint_array)");
        if (view_wild_tag.getVisibility() == View.VISIBLE) return;
//        view_wild_tag.setVisibility(View.VISIBLE);
        view_wild_tag.setTranslationX(0f);
        final ImageView iv_wild_tag = (ImageView) view_wild_tag.findViewById(R.id.iv_wild_tag);
        setPromptValues(hint_array, iv_wild_tag);

        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        iv_wild_tag.measure(w, h);
//        CustomLog.i("iv_wild_tag width == " + iv_wild_tag.getMeasuredWidth());
        int pad = iv_wild_tag.getMeasuredWidth();
        tv_prompt.setPadding(pad, 0, pad / 3, 0);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animWildTag(iv_wild_tag);
            }
        }, 2000);
//        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tv_prompt.getLayoutParams();
//        params.setMargins(iv_wild_tag.getMeasuredWidth() / 4, 0, 0, 0);
//        tv_prompt.setLayoutParams(params);

//        JSONArray reports = array.getJSONArray(0);
//        int reason = reports.getInt(0);
//        int num = reports.getInt(1);
//        if (num < 5) return;
//        StringBuilder sb = new StringBuilder();
//        sb.append("他被").append(num).append("人标注");
//        String reasonStr;
//        if (reason == 1) {
//            reasonStr = "\"色情狂\"";
//        } else if (reason == 5) {
//            reasonStr = "\"乱骂人\"";
//        } else {
//            reasonStr = "\"神经病\"";
//        }
//        sb.append(reasonStr);
//        if (tv_prompt.getVisibility() == View.VISIBLE) return;
//        String result = null;
//        if (welcome_percent >= 0.9) {
//            result = String.format("哇！受欢迎程度%.2f%s", welcome_percent * 100, "%");
//            //blue
//            tv_prompt.setBackgroundResource(R.drawable.bg_prompt_blue);
//        } else if (welcome_percent <= 0.4 && welcome_percent > 0) {
//            result = String.format("受欢迎程度仅为%.2f%s", welcome_percent * 100, "%");
//            //red
//            tv_prompt.setBackgroundResource(R.drawable.bg_prompt);
//        } else if (welcome_percent <= 0) {
//            result = "初来乍到请多关照";
//            tv_prompt.setBackgroundResource(R.drawable.bg_prompt_blue);
//        }
//        if (!TextUtils.isEmpty(result)) {
//            tv_prompt.setText(result);
//            showReportAnimation();
//        } else {
//            tv_prompt.setVisibility(View.GONE);
//        }
    }

    private void setAudioModeForNomal() {
        if (audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            if (!audioManager.isWiredHeadsetOn()) {
                setVoiceModeNomal();
            }
        }
    }

    private void countTime() {
        wildcat_time_text.setText(null);
        if (mChatMode == CHAT_MODE.MODE_COUNTDOWN) {
            // 倒计时模式
            wildcat_countdown_view.setText("(倒计时)");
            isbreak_mode_countdown = false;
            counttimeM = 2;
            counttimeS = 60;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!isbreak_mode_countdown) {
                        mHandler.postDelayed(this, 1000);
                        counttimeS--;
                        if (counttimeS == 0) {
                            counttimeS = 59;
                            counttimeM--;
                        }
                        if (counttimeM < 0) {
                            isbreak_mode_countdown = true;
                            wildcat_time_text.setText("00:00");
                            substitutionUser(DfineAction.WILDCAT_STOP_CALL_LIKE_TIMEOUT);
                            return;
                        }
                        StringBuilder result = new StringBuilder();
                        result.append("0");
                        result.append(counttimeM).append(":");
                        if (counttimeS < 10) {
                            result.append("0");
                        }
                        result.append(counttimeS);
                        wildcat_time_text.setText(result.toString());
                        if ("00:59".equals(result.toString())) {
                            // 震动一下
                            doVibrator();
                        }
                    }
                }
            });
        } else {
            // 无限时模式
            wildcat_countdown_view.setText("(无限时)");
            isbreak_mode_countlimit = false;
            counttimeM = 0;
            counttimeS = -1;
            counttimeH = 0;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!isbreak_mode_countlimit) {
                        mHandler.postDelayed(this, 1000);
                        counttimeS++;
                        if (counttimeS == 60) {
                            counttimeS = 0;
                            counttimeM++;
                        }
                        if (counttimeM == 60) {
                            counttimeM = 0;
                            counttimeH++;
                        }
                        StringBuilder result = new StringBuilder();
                        if (counttimeH > 0) {
                            if (counttimeH < 10) {
                                result.append("0");
                            }
                            result.append(counttimeH).append(":");
                        }
                        if (counttimeM < 10) {
                            result.append("0");
                        }
                        result.append(counttimeM).append(":");
                        if (counttimeS < 10) {
                            result.append("0");
                        }
                        result.append(counttimeS);
                        wildcat_time_text.setText(result.toString());
                    }
                }
            });
        }
    }

    private void doVibrator() {
        if (mChatMode != CHAT_MODE.MODE_COUNTDOWN)
            return;
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 500, 100, 500, 100, 500};
        vibrator.vibrate(pattern, -1);
    }

    private void reWildCat() {
        EventBus.getDefault().post(new WildCatCallingEvent(false));
        int mode = audioManager.getMode();
        if (mode == AudioManager.MODE_IN_COMMUNICATION) {
            currentState = TING_TONG;
        } else if (mode == AudioManager.MODE_NORMAL) {
            currentState = YANG_SHENG_QI;
        }
        isbreak_mode_countdown = true;
        isbreak_mode_countlimit = true;
        changeViewState(WILDCAT_STATE.MATCHING_STATE);
    }

    private int match_count = 0;
    private long start = System.currentTimeMillis();

    private void changeViewState(WILDCAT_STATE state) {
        wildcat_state = state;
        if (wait_time == 0 && state == WILDCAT_STATE.WAIT_ANSWER_STATE) {
            TcpProxy.getInstance().sendWildcatRequestCallReady();
            wildcat_state = WILDCAT_STATE.WAIT_CALLING_STATE;
        }
        if (wildcat_state == WILDCAT_STATE.MATCHING_STATE) {
            disMissRewardFragment();
            //findViewById(R.id.fl_parent).setBackgroundResource(R.drawable.bg_wildcat_matching);
            findViewById(R.id.wildcat_match_success_layout).setVisibility(View.GONE);
            findViewById(R.id.wildcat_matching_layout).setVisibility(View.VISIBLE);
            //wildcat_state_text.setVisibility(View.VISIBLE);


            tv_reputation_tips.setText("喜欢就点赞");
            iv_reputation.setEnabled(true);

            //ct_tips.setVisibility(View.GONE);
            tv_prompt.clearAnimation();
            view_wild_tag.setVisibility(View.GONE);
            findViewById(R.id.iv_close).setVisibility(View.GONE);
            findViewById(R.id.layout_finishit).setVisibility(View.VISIBLE);
            findViewById(R.id.iv_close).setVisibility(View.GONE);
            wildcat_state_text.setText(R.string.wild_cat_matching);
            tv_def_tips.setVisibility(View.VISIBLE);
            tv_hint_tips.setVisibility(View.VISIBLE);
            ll_action_img_container.setVisibility(View.GONE);
            ll_reputation_tips_parent.setVisibility(View.GONE);
            findViewById(R.id.tv_add_friend).setTag(null);
            startAnim();
        } else if (wildcat_state == WILDCAT_STATE.WAIT_ANSWER_STATE) {
            findViewById(R.id.wildcat_match_success_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.wildcat_matching_layout).setVisibility(View.GONE);
            //wildcat_state_text.setVisibility(View.VISIBLE);
            ll_action_img_container.setVisibility(View.GONE);
            wildcat_state_text.setVisibility(View.GONE);
            wildcat_tags_layout.setVisibility(View.VISIBLE);

            findViewById(R.id.iv_push_top).setVisibility(View.GONE);


            stopAnim();
            wildcat_avatar_image.setImageResource(R.drawable.ic_default_avatar);

            wildcat_time_text.setText("--:--");
        } else if (wildcat_state == WILDCAT_STATE.WAIT_CALLING_STATE) {
//            vibratorPhone();
            wildcat_state_text.setVisibility(View.VISIBLE);

            findViewById(R.id.layout_finishit).setVisibility(View.GONE);
            findViewById(R.id.iv_close).setVisibility(View.VISIBLE);
            wildcat_state_text.setText("连接中....");
        } else if (wildcat_state == WILDCAT_STATE.CALLING_STATE) {
            //findViewById(R.id.fl_parent).setBackgroundResource(R.drawable.bg_wildcat_matched);
            findViewById(R.id.wildcat_match_success_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.wildcat_matching_layout).setVisibility(View.GONE);
            wildcat_state_text.setVisibility(View.GONE);
            tv_def_tips.setVisibility(View.GONE);
            tv_hint_tips.setVisibility(View.GONE);
            //ct_tips.setVisibility(View.VISIBLE);
            wildcat_tags_layout.setVisibility(View.VISIBLE);
            ll_action_img_container.setVisibility(View.VISIBLE);
            ll_reputation_tips_parent.setVisibility(View.VISIBLE);
            findViewById(R.id.iv_close).setVisibility(View.GONE);
            findViewById(R.id.layout_finishit).setVisibility(View.VISIBLE);
            findViewById(R.id.iv_close).setVisibility(View.VISIBLE);
            stopAnim();
            findViewById(R.id.iv_push_top).setVisibility(View.VISIBLE);

            match_count++;
            HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
            if (hourGlassAgent.getStatistics() && hourGlassAgent.getK29() == 0) {
                hourGlassAgent.setK29(1);
                PeiwoApp app = (PeiwoApp) getApplicationContext();
                long temp = (System.currentTimeMillis() - start) / 1000;
                app.postKV("k29", String.valueOf(temp));
            }
        }
    }


    private void vibratorPhone() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 500, 100, 500};
        vibrator.vibrate(pattern, -1);
    }

    private void setUserData() {
        if (userJson != null) {
            String picUrl = userJson.optString("pic");
            ImageLoader.getInstance().displayImage(picUrl, wildcat_avatar_image, ImageUtil.getRoundedOptions());
            String nickname = userJson.optString("nickname");
            final JSONArray tags = userJson.optJSONArray("tags");
            wildcat_name_text.setText(nickname);
            if (tags != null && tags.length() > 0) {
                //int width = findViewById(R.id.wildcat_data_layout).getWidth();
                wildcat_tags_layout.removeAllViews();
                //wildcat_tags_layout.setWidth(width);
                for (int i = 0; i < tags.length(); i++) {
                    TextView box = new TextView(WildCatCallActivity.this);
                    box.setSingleLine();
                    box.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    box.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                    LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    box.setLayoutParams(layoutParams);
                    box.setGravity(Gravity.CENTER);
                    box.setBackgroundResource(R.drawable.bg_callslogan);
                    box.setText("#" + tags.optString(i));
                    box.setEnabled(false);
                    box.setTextColor(Color.WHITE);
                    wildcat_tags_layout.addView(box);
                }
//                wildcat_tags_layout.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (WildCatCallActivity.this.isFinishing()) {
//                            return;
//                        }
//                        int width = findViewById(R.id.wildcat_data_layout).getWidth();
//                        wildcat_tags_layout.removeAllViews();
//                        wildcat_tags_layout.setWidth(width);
//                        for (int i = 0; i < tags.length(); i++) {
//                            TextView box = new TextView(WildCatCallActivity.this);
//                            box.setSingleLine();
//                            box.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
//                            box.setEllipsize(TextUtils.TruncateAt.MIDDLE);
//                            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//                            box.setLayoutParams(layoutParams);
//                            box.setGravity(Gravity.CENTER);
//                            box.setBackgroundResource(R.drawable.bg_callslogan);
//                            box.setText("#" + tags.optString(i));
//                            box.setEnabled(false);
//                            box.setTextColor(Color.WHITE);
//                            wildcat_tags_layout.addView(box);
//                        }
//                    }
//                }, 2000);
            }
        }
    }


    @Override
    protected void reChargeMoney() {
        Intent intent = new Intent(this, ChargeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        runInBackground();
    }

    public void click(View v) {
        Resources res = getResources();
        if (PWUtils.isMultiClick())
            return;
        switch (v.getId()) {
            case R.id.iv_wild_tag:
                animWildTag(v);
                break;
            case R.id.tv_dasahng:
                //type	y	number	1: 匿名聊， 2: 普通电话
                sendIntentRewardMessage(mTargetUid, "1", null);
                break;
            case R.id.tv_add_friend:
                if (mChatMode == CHAT_MODE.MODE_COUNTDOWN) {
                    showToast(this, "互相喜欢后才可以申请");
                } else {
                    sendAddFriendAction(v);
                }
                break;
            case R.id.tv_voice_mode:
                // 音频模式
                setAudioMode();
                break;
            case R.id.view_finishit:
                isactionforme = true;
                endCallActivity(DfineAction.WILDCAT_STOP_CALL_EXIT);
                if (hourGlassAgent.getStatistics() && hourGlassAgent.getK28() == 0) {
                    hourGlassAgent.setK28(1);
                    PeiwoApp app = (PeiwoApp)getApplicationContext();
                    app.postK("k28");
                }

                break;
            case R.id.iv_refreshcat:
                wildcat_tags_layout.removeAllViews();
                substitutionUser(DfineAction.WILDCAT_STOP_CALL_NORMAL);
                break;
            case R.id.iv_reputation:
                // 点赞
                if (TcpProxy.getInstance().isLoginStauts()) {
                    TcpProxy.getInstance().sendWildcatReputationMessage();
                    iv_reputation.setEnabled(false);
                    tv_reputation_tips.setText("对方也点赞,可无限时通话.");
                } else {
                    showToast(this, res.getString(R.string.network_not_stable));
                }
                break;
            case R.id.iv_report_user:
                // doReportUser();
                doNewReportUser();
                //改成直接举报，用户无感知
                break;
            case R.id.iv_close:
                isactionforme = true;
                endCallActivity(DfineAction.WILDCAT_STOP_CALL_EXIT);
                HourGlassAgent hourGlass = HourGlassAgent.getInstance();
                hourGlass.setStatistics(false);
                break;
            case R.id.iv_report:

                break;
            case R.id.iv_push_top:
                runInBackground();
                break;


            default:
                break;
        }
    }

    private void animWildTag(View iv_wild_tag) {
        int offset = view_wild_tag.getMeasuredWidth() - iv_wild_tag.getMeasuredWidth();
        //CustomLog.i("view_wild_tag getTranslationX == " + view_wild_tag.getTranslationX());
        if (view_wild_tag.getTranslationX() == 0) {
            ObjectAnimator.ofFloat(view_wild_tag, "translationX", 0f, offset).start();
        } else {
            ObjectAnimator.ofFloat(view_wild_tag, "translationX", offset, 0f).start();
        }
    }


    private void sendAddFriendAction(View v) {
        if (v.getTag() != null) {
            showToast(this, "已申请");
            return;
        }
        v.setTag("tag");
        if (TcpProxy.getInstance().isLoginStauts()) {
            JSONObject jobj = new JSONObject();
            TcpProxy.getInstance().wildcatRequestFriend(mTargetUid, jobj);
//            long currentTime = System.currentTimeMillis() + SharedPreferencesUtil.getLongExtra(PeiwoApp.getApplication(), AsynHttpClient.KEY_CC_CURRENT_TIME, 0);
//            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String currentDate = df.format(new Date(currentTime));
//            long randomCount = (long) (Math.random() * 10000) * 1000000000l;
//            currentTime %= 1000000000l;
//            currentTime += randomCount;
//            MessageModel msgModel = new MessageModel();
//            String msg_content = "我是" + userJson.optString("my_nickname") + "向你发起实名申请。";
//            msgModel.content = msg_content;
//            msgModel.uid = mTargetUid;
//            msgModel.dialog_type = MessageModel.DIALOG_TYPE_IM;
//            msgModel.msg_id = 0;
//            msgModel.send_status = MessageModel.SEND_STATUS_SUCCESS;
//
//            msgModel.update_time = currentDate;
//            msgModel.dialog_id = -currentTime;
//            msgModel.type = 0;
//            msgModel.is_hide = 1;
//            try {
//                msgModel.details = new JSONObject().put("msg", msgModel.content).toString();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            PWUserModel otherModel = new PWUserModel();
//            otherModel.uid = mTargetUid;
//            msgModel.id = MessageUtil.insertMessage(this, msgModel, otherModel);
//
//            //PWUserModel meModel = UserManager.getPWUser(mContext);
//            msgModel.content = msg_content;
//            TcpProxy.getInstance().sendImTextMessage(mTargetUid, msgModel, 0x04);
            /***************/
//            String saveKey = "wildcat_call_requested_ids" + UserManager.getUid(mContext);
//            String hasRequested = SharedPreferencesUtil.getStringExtra(mContext, saveKey, "");
//            String[] requestedIds = null;
//            if (!TextUtils.isEmpty(hasRequested)) {
//                requestedIds = hasRequested.split("-");
//            }
//            if (requestedIds != null && requestedIds.length >= 6) {
//                for (int i = 0; i < requestedIds.length - 1; i++) {
//                    requestedIds[i] = requestedIds[i + 1];
//                }
//                requestedIds[requestedIds.length - 1] = model._id;
//                StringBuffer buff = new StringBuffer();
//                for (int i = 0; i < requestedIds.length; i++) {
//                    if (i != 0) {
//                        buff.append("-");
//                    }
//                    buff.append(requestedIds[i]);
//                }
//                hasRequested = buff.toString();
//            } else {
//                if (TextUtils.isEmpty(hasRequested)) {
//                    hasRequested = model._id;
//                } else {
//                    hasRequested = hasRequested + "-" + model._id;
//                }
//            }
//            SharedPreferencesUtil.putStringExtra(mContext, saveKey, hasRequested);
            showToast(this, "发送成功");
        } else {
            showToast(this, "当前未连接服务器，请检查网络连接");
        }
    }


    private void setAudioMode() {
        if (audioManager.isWiredHeadsetOn()) {
            // 插耳机状态下，只改变图标，不改变播放模式
            if (tv_voice_mode.isSelected()) {
                tv_voice_mode.setSelected(false);
            } else {
                tv_voice_mode.setSelected(true);
            }
            return;
        }
        if (audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            // 当前为听筒模式
            setVoiceModeNomal();
        } else {
            // 当前为扬声器模式
            setVoiceModeCommunication();
        }
    }

    /**
     * 听筒模式
     * <p>
     * audioManager
     */
    private void setVoiceModeCommunication() {
        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(false);
        }
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, (int) (maxVolume * 0.9), 0);
        tv_voice_mode.setSelected(true);
    }

    /**
     * 扬声器模式
     * <p>
     * audioManager
     */
    private void setVoiceModeNomal() {
        audioManager.setMode(AudioManager.MODE_NORMAL);
        if (!audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(true);
        }
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (maxVolume * 0.8), 0);
        tv_voice_mode.setSelected(false);
    }

    /**
     * needFinish 标记是否需要在此Finish界面
     */
    private void endCallActivity(int reason) {
        if (wildcat_state == WILDCAT_STATE.WAIT_CALLING_STATE
                || wildcat_state == WILDCAT_STATE.CALLING_STATE) {
            TcpProxy.getInstance().stopCallForMe(reason);
        } else if (wildcat_state == WILDCAT_STATE.WAIT_ANSWER_STATE) {
            TcpProxy.getInstance().sendWildcatRequestCancel();
            TcpProxy.getInstance().closeRTC();
        }
        TcpProxy.getInstance().sendStopWildcatMessage();
        finish();
    }

    /**
     * 换人
     */
    private void substitutionUser(int reason) {
        if (wildcat_state == WILDCAT_STATE.WAIT_CALLING_STATE
                || wildcat_state == WILDCAT_STATE.CALLING_STATE) {
            TcpProxy.getInstance().substitutionUser(reason);
        }
        if (getString(R.string.calling_other_net_bad).equals(tv_net_status.getText().toString())) {
            tv_net_status.setText("");
        }
        reWildCat();
    }

    private void doNewReportUser() {
        Resources res = getResources();
        if (PeiwoApp.getApplication().getNetType() == NetUtil.NO_NETWORK) {
            showToast(this, res.getString(R.string.network_not_stable));
            return;
        }
        if (mTargetUid == 0)
            return;
        doReport(1);
        substitutionUser(DfineAction.WILDCAT_STOP_CALL_REPORT);
    }

    private void doReportUser() {
        Resources res = getResources();
        if (PeiwoApp.getApplication().getNetType() == NetUtil.NO_NETWORK) {
            showToast(this, res.getString(R.string.network_not_stable));
            return;
        }
        if (mTargetUid == 0)
            return;
        new AlertDialog.Builder(this)
                .setTitle("讨厌此人")
                .setNegativeButton(res.getString(R.string.cancel), null)
                .setPositiveButton(res.getString(R.string.ok), (dialog, which) -> {
                    doReport(1);
                    substitutionUser(DfineAction.WILDCAT_STOP_CALL_REPORT);
                })
                .create().show();
//        String[] items = new String[]{"色情狂", "乱骂人", "神经病", "取消"};
//        new AlertDialog.Builder(this).setTitle("举报用户")
//                .setItems(items, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (which == 3) {
//                            return;
//                        }
//                        if (which == 0) {
//                            which = 1;
//                        } else if (which == 1) {
//                            which = 5;
//                        } else if (which == 2) {
//                            which = 6;
//                        }
//                        doReport(which);
//                        substitutionUser(DfineAction.WILDCAT_STOP_CALL_REPORT);
//                    }
//                }).create().show();
    }

    public void doReport(int reason) {
        showToast(this, "此人已被花式吊打");
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(AsynHttpClient.KEY_REASON, String.valueOf(reason)));
        params.add(new BasicNameValuePair(AsynHttpClient.KEY_REPORT_WILDCAT, "1"));
        params.add(new BasicNameValuePair(AsynHttpClient.KEY_TUID, String.valueOf(mTargetUid)));
        params.add(new BasicNameValuePair("msg", MsgDBCenterService.getInstance().getRequestLogByUid(mTargetUid)));
        ApiRequestWrapper.openAPIGET(this, params,
                AsynHttpClient.API_REPORT_SEND, new MsgStructure() {
                    @Override
                    public void onReceive(JSONObject data) {
                    }

                    @Override
                    public void onError(int error, Object ret) {
                    }
                });
    }

    @Override
    protected void onDestroy() {
        stopAnim();
        if (wildCatTimer != null)
            wildCatTimer.cancel();
        if (wildCatHintTimer != null)
            wildCatHintTimer.cancel();
        cancelListenerSystemCallState();
        DfineAction.CURRENT_CALL_STATUS = DfineAction.CURRENT_CALL_NOT;
        EventBus.getDefault().unregister(this);
        PeiwoApp.getApplication().wildCatCallNotification = false;
        PeiwoApp.getApplication().wildCatCallNotificationReStart = false;
        mHandler.removeCallbacksAndMessages(null);
        releaseSoundPool();
        unregisterReceiver(headSetPlugReceiver);
        EventBus.getDefault().post(new WildCatCallingEvent(false));
        super.onDestroy();
    }

    private void releaseSoundPool() {
        try {
            if (mSoundPool != null) {
                mSoundPool.release();
                mSoundPool = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        System.out.println("WildCatCallActivity finish");
        if (wildCatTimer != null)
            wildCatTimer.cancel();
        if (wildCatHintTimer != null)
            wildCatHintTimer.cancel();
        isbreak_mode_countdown = true;
        isbreak_mode_countlimit = true;
        mNotifyMgr.cancel(Constans.NOTIFY_ID_WILDCAT_BACKGROUND);
        TcpProxy.getInstance().closeRTC();
        PeiwoApp.getApplication().wildCatCallNotification = false;
        PeiwoApp.getApplication().setCalling(false, PeiwoApp.CALL_TYPE.CALL_NONE);

        setAudioModeForNomal();

        if (getIntent().getIntExtra(START_MAIN, -1) == 1) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("action", Constans.ACTION_FLAG_WILDCAT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
        if (needAlertShareDialog) {
            sendBroadcast(new Intent(PWActionConfig.ACTION_ALERT_SHARE));
        }

        overridePendingTransition(0, 0);
        currentState = 10000;
//        PWUtils.uploadRecordFiles(this);
        EventBus.getDefault().post(new WildCatCallingEvent(false));
        super.finish();
    }

//    private void stopWildcatAnim(ImageView iv) {
//        Animatable animatable = (Animatable) iv.getDrawable();
//        if (animatable.isRunning())
//            return;
//        animatable.stop();
//    }
//
//    private void startWildcatAnim(ImageView iv) {
//        Animatable animatable = (Animatable) iv.getDrawable();
//        if (animatable.isRunning())
//            return;
//        animatable.start();
//    }

    private void runInBackground() {
        moveTaskToBack(true);
    }

    public void notificationCall() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name)).setTicker("限时聊")
                .setContentText("限时聊").setWhen(System.currentTimeMillis())
                .setAutoCancel(true).setOngoing(true);

        Intent intent = new Intent(this, WildCatCallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        // resultIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                intent, Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        mBuilder.setContentIntent(resultPendingIntent);
        mNotifyMgr.notify(Constans.NOTIFY_ID_WILDCAT_BACKGROUND, mBuilder.build());
        PeiwoApp.getApplication().wildCatCallNotification = true;
    }

    @Override
    public void onResume() {
        PeiwoApp.getApplication().sStartTime = System.currentTimeMillis();
        LinphoneManager.getInstance().startProximitySensorForActivity(WildCatCallActivity.this);
        PeiwoApp.getApplication().wildCatCallNotification = false;
        PeiwoApp.getApplication().wildCatCallNotificationReStart = false;
        mNotifyMgr.cancel(Constans.NOTIFY_ID_WILDCAT_BACKGROUND);
        slideView.reset();
        super.onResume();
    }


    private class MyPhoneListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    //响铃状态
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //接听状态
                    if (isFinishing()) return;
                    endCallActivity(DfineAction.WILDCAT_STOP_CALL_SYSTEM_PHONE);
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }


    class HeadSetPlugReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")) {
                if (intent.getIntExtra("state", 0) == 0) {
                    //耳机拔出
                    if (tv_voice_mode.isSelected()) {
                        /*在插耳机的情况下，用户手动关了免提，拔出耳机后，声音为听筒模式；*/
                        setVoiceModeCommunication();
                    } else {
                        setVoiceModeNomal();
                    }
                    return;
                } else if (intent.getIntExtra("state", 0) == 1) {
                    //耳机插入
                    setVoiceModeCommunication();
                    /*如果用户插了耳机，则只从耳机听到声音，外放图标的状态不变。用户拔出耳机后，声音为外放模式。*/
                    tv_voice_mode.setSelected(false);        //虽然为外放图标，但是是听筒模式
                }
            }
        }
    }

    private void startAnim() {
        wildcat_anim_image1.setVisibility(View.VISIBLE);
        wildcat_anim_image2.setVisibility(View.VISIBLE);
        initAnimation(wildcat_anim_image1, 0);
        wildcat_anim_image2.postDelayed(animRunnable, 2000);
    }

    private Runnable animRunnable = new Runnable() {
        @Override
        public void run() {
            if (wildcat_state == WILDCAT_STATE.MATCHING_STATE) {
                initAnimation(wildcat_anim_image2, 0);
            }
        }
    };

    private void initAnimation(ImageView animImage, int startOffset) {
        if (animImage.getAnimation() != null) {
            animImage.getAnimation().start();
            return;
        }
        AnimationSet localAnimationSet = new AnimationSet(true); // true:使用相同的加速器
        ScaleAnimation localScaleAnimation = new ScaleAnimation(1, 6, 1, 6,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setFillBefore(true);
        alphaAnimation.setRepeatCount(-1);

        localScaleAnimation.setRepeatCount(-1); // 动画重复

        localAnimationSet.addAnimation(localScaleAnimation);
        localAnimationSet.addAnimation(alphaAnimation);  //将两种动画效果添加进去

        // 设置相关属性
        localAnimationSet.setDuration(4000); // 持续时间
        localAnimationSet.setStartOffset(startOffset); // 动画效果推迟ltime秒钟后启动
//		localAnimationSet.setFillBefore(true); // 控件保持在动画开始之前
//		localAnimationSet.setFillEnabled(true);
        localAnimationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        // 将动画集合设置进去
        animImage.setAnimation(localAnimationSet);
        animImage.startAnimation(localAnimationSet);// 开启动画
    }

    private void stopAnim() {
        if (wildcat_anim_image1.getAnimation() != null) {
            wildcat_anim_image1.getAnimation().cancel();
        }
        wildcat_anim_image1.setVisibility(View.GONE);

        if (wildcat_anim_image2.getAnimation() != null) {
            wildcat_anim_image2.getAnimation().cancel();
        }
        wildcat_anim_image2.setVisibility(View.GONE);
        wildcat_anim_image2.removeCallbacks(animRunnable);
    }


    public void onEventMainThread(WildCatMessageEvent event) {
        if (event == null)
            return;

        Intent intent = event.intent;
        int type = intent.getIntExtra("type", CoreService.HANDLE_REAL_UI_CALL_RESPONSE);
        Message message = mHandler.obtainMessage();
        message.what = type;
        Bundle b = new Bundle();

        if (intent.hasExtra("code")) {
            b.putInt("code", intent.getIntExtra("code", 0));
        }
        if (intent.hasExtra("msg")) {
            b.putString("msg", intent.getStringExtra("msg"));
        }
        if (intent.hasExtra("data")) {
            b.putString("data", intent.getStringExtra("data"));
        }
        if (intent.hasExtra("notify")) {
            b.putString("notify", intent.getStringExtra("notify"));
        }
        if (intent.hasExtra("user")) {
            b.putString("user", intent.getStringExtra("user"));
        }
        if (intent.hasExtra("reports")) {
            b.putString("reports", intent.getStringExtra("reports"));
        }
        if (intent.hasExtra("wait_time")) {
            b.putInt("wait_time", intent.getIntExtra("wait_time", 5));
        }

        message.setData(b);
        mHandler.sendMessage(message);
    }

    public void onEventMainThread(CallStateEvent event) {
        if (event == null)
            return;
        if (event.type == 1) {
            Message message = mHandler.obtainMessage();
            message.what = HANDLE_NET_STATUS_CHANGE;
            message.obj = event;
            mHandler.sendMessage(message);
        } else if (event.type == 2) {
            showToast(this, "语音连接超时,已挂断");
            substitutionUser(DfineAction.WILDCAT_STOP_CALL_WEBRTC_TIMEOUT);
        }
    }

    public void onEventMainThread(WildCatMatchStateEvent event) {
        if (event == null)
            return;
        Message message = mHandler.obtainMessage();
        message.what = HANDLE_WILD_CAT_MATCH_STATE;
        message.obj = event;
        mHandler.sendMessage(message);
    }


    private void updateWildcatHint() {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        MsgStructure structure = new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
            }

            @Override
            public boolean onInterceptRawData(String rawStr) {
                try {
                    JSONObject rootObject = new JSONObject(rawStr);
                    String data = rootObject.optString("data");
                    SharedPreferencesUtil.putStringExtra(WildCatCallActivity.this, "wildcat_hint", data);
                    startInvalidLoopHint(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public void onError(int error, Object ret) {
                //String resString = ret.toString();
            }
        };
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_USERINFO_WILDCAT_HINT, structure);
    }

    private void startInvalidLoopHint(boolean needUpdate) {
        if (wildCatHintTimer != null) {
            return;
        }
        String hintText = SharedPreferencesUtil.getStringExtra(this, "wildcat_hint", "");
        if (TextUtils.isEmpty(hintText)) {
            if (needUpdate) {
                updateWildcatHint();
            }
            return;
        }
        try {
            final JSONArray array = new JSONArray(hintText);
            if (array.length() == 0) {
                if (needUpdate) {
                    updateWildcatHint();
                }
                return;
            }
            wildCatHintTimer = new Timer();
            wildCatHintTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!WildCatCallActivity.this.isFinishing() && wildcat_state == WILDCAT_STATE.MATCHING_STATE) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                int index = (int) (System.currentTimeMillis() % array.length());
                                tv_hint_tips.setText(array.optString(index));
                            }
                        });
                    }
                }
            }, 500, 3 * 1000);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (needUpdate) {
            updateWildcatHint();
        }
    }

    @Override
    protected WILDCAT_STATE getWildState() {
        return wildcat_state;
    }
}
