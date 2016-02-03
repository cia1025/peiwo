package me.peiwo.peiwo.activity;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.CallStateEvent;
import me.peiwo.peiwo.eventbus.event.MessagePushEvent;
import me.peiwo.peiwo.eventbus.event.RealCallMessageEvent;
import me.peiwo.peiwo.net.NetUtil;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.service.CoreService;
import me.peiwo.peiwo.service.CoreService.RealCallState;
import me.peiwo.peiwo.service.PlayerService;
import me.peiwo.peiwo.util.*;
import me.peiwo.peiwo.widget.CallScrollView;
import me.peiwo.peiwo.widget.InputBoxView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * 拨打电话界面
 *
 * @author kevin
 */

public class RealCallActivity extends BaseCallActivity implements
        CallScrollView.OnScrollUpFinishListener {
    /**
     * 拒绝
     */
    public static final int WEBRTC_CODE_REJECT = 40006;
    /**
     * 繁忙
     */
    public static final int WEBRTC_CODE_BUSY = 40005;
    /**
     * 没有权限拨打
     */
    public static final int WEBRTC_CODE_NO_PERMISSION = 40003;
    /**
     * 被封用户
     */
    public static final int ANSWER_CLOSURE = 20003;
    /**
     * 超时
     */
    public static final int ANSWER_TIMEOUT = 2;
    /**
     * 拒绝
     */
    public static final int CALLEE_REJECT = 1;
    /**
     * 忙
     */
    public static final int CALL_CALLEE_BUSY = 40005;
    /**
     * 离线
     */
    public static final int CALL_CALLEE_OFFLINE = 40004;
    /**
     * 拨打的人钱不够
     */
    public static final int CALL_CALLER_MONEY_LOW = 40001;
    /**
     * android用户设置不推送，并且在后台时
     */
    public static final int CALL_CALLEE_BACKGROUND = 4;


    public static final int HANDLE_RECONNECTION_TCP_EVENT = 0x10001;
    public static final int HANDLE_TCP_CONNECT_TIMEOUT = 0x10002;

    public static final int HANDLE_NET_STATUS_CHANGE = 0x11000;
    //protected TextView tv_calling;
    //protected TextView tv_callstate;
    //protected ImageView iv_userface;
    // TextView tv_age_gender;
    //protected GenderWithAgeView v_gender_age;
    //protected TextView tv_address;
    // protected Button btn_call_over;
    //protected View ll_call_over;
    //protected TextView tv_counting;
    //    protected View ll_screen_bg;
    //protected View ll_call_switch;

    private TextView tv_username;
    private TextView tv_slogan;
    private ImageView iv_duration_option;
    private TextView tv_network_tips;

    private View tv_voice_mode;

    private TextView tv_callstate;
    /**
     * 外放模式下select为true 听筒模式下select为false
     */
    //private ImageView iv_mianti_tingtong;
    //private LinearLayout iv_mianti_tingtong_layout;
    private int mTargetUid;

    private TextView tv_call_duration;

    protected static final int YANGSHENGQI_MODE = 0;
    protected static final int TINGTONG_MODE = 1;
    protected int currentAudioMode;


    protected String mFaceShowUrl;
    //protected ImageView iv_blur;
    //protected TextView tv_uname;
    protected boolean noalert = false;
    protected boolean noring = false;

    protected AudioManager audioManager = null;

    //protected SoundPool mSoundPool;
    //protected int m_CallStop;
    protected boolean needNotify = true;

    protected CallScrollView slideView;

//    protected TextView tv_newprice;
//    protected TextView tv_net_status;

    protected int mGneder = 1; // 根据自己的声音，判断提示音，扯

    protected PhoneStateListener phoneListener;

    private NotificationManager mNotifyMgr;
    private LinearLayout click_area_layout;

    /**
     * 标识来电或去电    CoreService.OUTGOING_CALL为去电      CoreService.INCOMMING_CALL为来电
     */
    private int mCallFalg = DfineAction.OUTGOING_CALL;
    private MyHandler mHandler = new MyHandler(RealCallActivity.this);

    /**
     * 循环拨打定时器
     */
    private Timer mCallTimer = null;
    private StringBuilder mCountingDuration;
    private String call_id;


    private static class MyHandler extends Handler {
        WeakReference<RealCallActivity> activity_ref;

        public MyHandler(RealCallActivity activity) {
            activity_ref = new WeakReference<RealCallActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final RealCallActivity theActivity = activity_ref.get();
            if (theActivity == null || theActivity.isFinishing()) {
                return;
            }
            Resources res = theActivity.getResources();
            int what = msg.what;
            switch (what) {
                case DfineAction.IntentRewardResponseMessage:
                    //收到打赏的回执
                    theActivity.handleIntentRewardResponseMessage(theActivity.call_id, theActivity.mTargetUid, theActivity.mGneder, msg.getData());
                    break;
                case DfineAction.PayRewardResponseMessage:
                    theActivity.hanlePayRewardResponseMessage(msg.getData());
                    break;
                case DfineAction.RewardedMessage:
                    theActivity.handleRewardedMessage(theActivity.mTargetUid, theActivity.mFaceShowUrl, UserManager.getRealName(theActivity.mTargetUid, theActivity.mIncommingName, theActivity), "2", msg.getData());
                    break;
                case HANDLE_RECONNECTION_TCP_EVENT:
                    if (TcpProxy.getInstance().isLoginStauts()) {
                        theActivity.mHandler.removeMessages(HANDLE_TCP_CONNECT_TIMEOUT);
                        theActivity.tv_call_duration.setText(res.getString(R.string.is_calling));
                        theActivity.callUser();
                    } else {
                        theActivity.mHandler.sendEmptyMessageDelayed(HANDLE_RECONNECTION_TCP_EVENT, 2000);
                    }
                    break;
                case HANDLE_TCP_CONNECT_TIMEOUT:
                    theActivity.showToast(theActivity, res.getString(R.string.network_not_stable));
                    theActivity.endCallActivity(1000);
                    break;
                case CoreService.HANDLE_REAL_UI_CALL_RESPONSE:
                    theActivity.doCallResponse(msg);
                    break;
                case CoreService.HANDLE_REAL_UI_CALL_READY:
                    Bundle b = msg.getData();
                    if (b != null) {
                        int code = b.getInt("code");
                        theActivity.call_id = b.getString("call_id");
                        if (WEBRTC_CODE_REJECT == code) {
                            theActivity.showToast(theActivity, res.getString(R.string.reject_by_other));
                            theActivity.endCallActivity(500);
                            return;
                        }
                    }
                    break;
                case CoreService.STOP_CALL:
                    // 挂断
                    if (!theActivity.isHangupForMe) {
                        Bundle stop_bundle = msg.getData();
                        if (stop_bundle.containsKey("data")) {
                            theActivity.alertUser(stop_bundle.getString("data"));
                        }
                        theActivity.hangupCall(false, DfineAction.REAL_STOP_CALL_NORMAL);
                    }
                    break;
                case CoreService.CALL_BEGIN_RESPONSE:
                    TextView tv_charge_free_guide = (TextView) theActivity.findViewById(R.id.tv_charge_free_guide);
                    Observable.timer(5, TimeUnit.SECONDS, AndroidSchedulers.mainThread()).map(aLong -> {
                        tv_charge_free_guide.setVisibility(View.GONE);
                        return null;
                    }).subscribe();
                /*开始30S录音*/
//            	getRecPermission();
                    if (theActivity.click_area_layout != null)
                        theActivity.click_area_layout.setClickable(true);
                    if (theActivity.mCallFalg == DfineAction.OUTGOING_CALL) {
//                        theActivity.tv_call_duration.setVisibility(View.GONE);
                    } else {
                        theActivity.tv_call_duration.setText("");
                        theActivity.mHandler.postDelayed(() -> {
                            theActivity.tv_network_tips.animate().alpha(0.0f).start();
                        }, 3000);
                    }
                    theActivity.iv_duration_option.setVisibility(View.VISIBLE);
                    theActivity.iv_duration_option.setSelected(false);
                    theActivity.tv_call_duration.setText(res.getString(R.string.is_speaking));
                    theActivity.tv_call_duration.setVisibility(View.VISIBLE);
                    theActivity.findViewById(R.id.ll_answer_call).setVisibility(View.GONE);
                    theActivity.countTime();
                    theActivity.findViewById(R.id.view_action_cetener).setVisibility(View.VISIBLE);
                    ImageView iv_start_userinfo = (ImageView) theActivity.findViewById(R.id.iv_start_userinfo);
                    ImageLoader.getInstance().displayImage(theActivity.mFaceShowUrl, iv_start_userinfo);
                    theActivity.findViewById(R.id.iv_push_top).setVisibility(View.VISIBLE);
                    //iv_mianti_tingtong_layout.setVisibility(View.VISIBLE);
                    //xxxTcpProxy.getInstance().releaseMediaPlayer();
                    theActivity.resetPlayer();
                    int outCallUid = theActivity.getIntent().getIntExtra("tid", 0);
                    int inCallUid = theActivity.getTuid();
                    int tUid = inCallUid > 0 ? inCallUid : outCallUid;
                    SharedPreferencesUtil.putBooleanExtra(theActivity, "called_with" + tUid, true);
                    theActivity.setAudioModeWithBrand();
                    break;
                case CoreService.CALL_STATE_CHANGE: {
                    CallStateEvent event = (CallStateEvent) msg.obj;
                    if (event.nTCPState) {
                        if (event.nCallState == RealCallState.DIAL) {
                        /*tv_callstate.setText("等待对方应答");*/
                        } else {
                            if (theActivity.isCalling) {
                                if (event.nWebRTCState == CoreService.WEBRTC_STATE_ONLINE) {
                                    theActivity.tv_callstate.setText("           ");
                                } else if (event.nWebRTCState == CoreService.WEBRTC_STATE_OFFLINE) {
                                    theActivity.tv_callstate.setText("当前无网络连接");
                                } else if (event.nWebRTCState == CoreService.WEBRTC_STATE_LOST) {
                                    theActivity.tv_callstate.setText("对方当前网络连接不稳定");
                                } else if (event.nWebRTCState == CoreService.WEBRTC_STATE_CONNECTING) {
                                    theActivity.tv_callstate.setText("当前网络连接不稳定");
                                } else if (event.nWebRTCState == CoreService.WEBRTC_STATE_EXCHANGINGSD) {
                                    theActivity.tv_callstate.setText("网络异常，通话重连中...");
                                }
                            }
                        }
                    } else {
                        theActivity.tv_callstate.setText("           ");
                    }
                }
                break;
                case HANDLE_NET_STATUS_CHANGE: {
                    CallStateEvent event = (CallStateEvent) msg.obj;
                    if (event.heart_lost_count > 0) {//自己丢包
                        //tv_net_status.setText(R.string.calling_me_net_bad);
                        theActivity.tv_callstate.setText(R.string.calling_me_net_bad);
                    } else {
                        if (event.remote_user_state > 0) {
                            //tv_net_status.setText(R.string.calling_other_net_bad);
                            theActivity.tv_callstate.setText(R.string.calling_other_net_bad);
                        } else {
                            //tv_net_status.setText("");
                            theActivity.tv_callstate.setText("");
                        }
                    }
                }
                break;
            }
        }
    }

    private void resetPlayer() {
//        Intent intent = new Intent(this, PlayerService.class);
//        intent.setAction(PlayerService.PLAY_ACTION_RESET);
//        startService(intent);
        //AudioPlayerUtil.resetPlayer(this);
        PlayerService.getInstance().resetPlayerCommand();
    }

    private void setAudioModeWithBrand() {
//        if ("dazen".equals(Build.BRAND)) {
//            //酷派大神手机,开始设置为听筒导致音频模式设置失败，所以不设置听筒模式
//        } else {
//        }
        //经验证是代码中重复设置setAudioMode导致，正常设置是没有问题的
        setAudioMode();
    }

    private HeadSetPlugReceiver headSetPlugReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (finishIfNeed()) {
            //System.err.println("onCreate return finishIfNeed is true");
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        View root = getLayoutInflater().inflate(R.layout.activity_real_call, null);
        slideView = new CallScrollView(this);
        slideView.addView(root, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        slideView.setOnScrollUpFinishListener(this);
        setContentView(slideView);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mCallFalg = getIntent().getIntExtra("flag", 0);
        if (mCallFalg != DfineAction.OUTGOING_CALL && mCallFalg != DfineAction.INCOMMING_CALL) {
            finish();
            return;
        }
        DfineAction.CURRENT_CALL_STATUS = DfineAction.CURRENT_CALL_REAL;
        EventBus.getDefault().register(this);
        init();
        //mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        //m_CallStop = mSoundPool.load(this, R.raw.call_stop, 1);
        PeiwoApp.getApplication().setCalling(true, PeiwoApp.CALL_TYPE.CALL_REAL);
        listenerSystemCallState();
        mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PeiwoApp.getApplication().realCallNotification = false;
        PeiwoApp.getApplication().realCallNotificationReStart = false;
        headSetPlugReceiver = new HeadSetPlugReceiver();
        registerReceiver(headSetPlugReceiver, new IntentFilter("android.intent.action.HEADSET_PLUG"));
        mGneder = UserManager.getGender(this);

        if (mCallFalg == DfineAction.OUTGOING_CALL) {
            outCall();
        } else if (mCallFalg == DfineAction.INCOMMING_CALL) {
            inCall();
        }
        //doBlurBackground();
    }


    /**
     * 定时发送通话
     */
    private void addCallSchedule(final int callee, final String payload) {
        if (mCallTimer != null) {
            mCallTimer.cancel();
        }
        mCallTimer = new Timer();
        mCallTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                CustomLog.i(DfineAction.TCP_TAG, "call msg");
                if (TcpProxy.getInstance().isLoginStauts()) {
                    TcpProxy.getInstance().callUser(callee, payload, true);
                }
            }
        }, 6000, 6000);
    }

    // 拨打
    private void outCall() {

        findViewById(R.id.view_action_cetener).setVisibility(View.GONE);


        mFaceShowUrl = getIntent().getStringExtra("face_url");
        //int gender = getIntent().getIntExtra("gender", 1);
        //String age = getIntent().getStringExtra("age");
        //v_gender_age.displayGenderWithAge(gender, age);
        mTargetUid = getIntent().getIntExtra("tid", 0);
        //tv_uname.setText(UserManager.getRealName(getIntent().getIntExtra("tid", 0), getIntent().getStringExtra("uname"), this));
        //tv_address.setText(getIntent().getStringExtra("address"));

        findViewById(R.id.ll_answer_call).setVisibility(View.GONE);
        //ll_call_switch.setVisibility(View.GONE);
        //ll_call_over.setVisibility(View.VISIBLE);

        tv_username.setText(UserManager.getRealName(mTargetUid, getIntent().getStringExtra("uname"), this));
        //tv_slogan.setText(getIntent().getStringExtra("slogan"));

        if (TcpProxy.getInstance().isLoginStauts()) {
            // add resend schedule
            callUser();
        } else {
            TcpProxy.getInstance().connectionTcp();
            mHandler.sendEmptyMessageDelayed(HANDLE_RECONNECTION_TCP_EVENT, 5000);// 延迟等待5s
            mHandler.sendEmptyMessageDelayed(HANDLE_TCP_CONNECT_TIMEOUT, 20000);// 延迟等待20s
        }
        tv_call_duration.setText(getResources().getString(R.string.is_calling));
        //xxxTcpProxy.getInstance().playMusic(R.raw.call_ring, true);
        playMusicByPath("call_music/call_ring.mp3", true);
    }

    private void playMusicByPath(String path, boolean loop) {
        resetAudioMode();
        /******/
//        Intent intent = new Intent(this, PlayerService.class);
//        intent.setAction(PlayerService.PLAY_ACTION_PLAY);
//        intent.putExtra(PlayerService.KEY_PLAY_PATH, path);
//        intent.putExtra(PlayerService.KEY_LOOP, loop);
//        startService(intent);
        //AudioPlayerUtil.playAudioByAssetsPath(this, path, loop);
        PlayerService playerService = PlayerService.getInstance();
        playerService.playAssetFileCommand(playerService.getMusicAssetPath(this, path), loop);
    }

    private void resetAudioMode() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            if (!audioManager.isSpeakerphoneOn()) {
                audioManager.setSpeakerphoneOn(true);
            }
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (maxVolume * 0.8), 0);
        }
    }

    private void callUser() {
        int callee = getIntent().getIntExtra("tid", 0);

        StringBuilder sb = new StringBuilder();
        sb.append(System.currentTimeMillis());
        String payload = Md5Util.getMd5code(sb.toString());
        addCallSchedule(callee, payload);

        TcpProxy.getInstance().callUser(callee, payload, false);
    }

    // 来电
    private void inCall() {
        try {
            tv_call_duration.setText(getResources().getString(R.string.waiting_response));
            findViewById(R.id.view_action_cetener).setVisibility(View.GONE);

            // 如果在设置中关闭了声音,就不播放声音
            if (needPlayMusic()) {
                //xxxTcpProxy.getInstance().playMusic(R.raw.call_ring, true);
                playMusicByPath("call_music/call_ring.mp3", true);
            }
            //ll_call_switch.setVisibility(View.VISIBLE);
            //ll_call_over.setVisibility(View.GONE);

            JSONObject data = new JSONObject(getIntent().getStringExtra("data"));
            JSONObject user = data.optJSONObject("user");
            CustomLog.d("inCall user is : " + user);
            mFaceShowUrl = user.optString("avatar");
            mIncommingName = user.optString("name");
            //tv_calling.setText("来电了");
            int gender = user.optInt("gender");
            if (gender == 1) {
                //对方是男的不能打赏,全面开放
                //findViewById(R.id.tv_dasahng).setVisibility(View.GONE);
            }
            //v_gender_age.displayGenderWithAge(gender, TimeUtil.getAgeByBirthday(user.optString("birthday")));
            String province = user.optString("province");
            String city = user.optString("city");
            //tv_uname.setText(UserManager.getRealName(user.optInt("uid"), mIncommingName, this));
            mTargetUid = user.optInt("uid");
            tv_username.setText(UserManager.getRealName(mTargetUid, mIncommingName, this));

            JSONArray tagsArray = user.optJSONArray("tags");
            if (tagsArray != null && tagsArray.length() > 0)
                tv_slogan.setText("#" + tagsArray.getString(0));


            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(province) && !"null".equalsIgnoreCase(province)) {
                sb.append(province).append(" ");
            }
            if (!TextUtils.isEmpty(city) && !"null".equalsIgnoreCase(city)) {
                sb.append(city);
            }
            //tv_address.setText(sb.toString());
            switch (PeiwoApp.getApplication().getNetType()) {
                case NetUtil.WIFI_NETWORK:
//                    tv_network_tips.setText(getString(R.string.calling_incoming_wifi));
                    break;
                case NetUtil.G2_NETWORK:
                    tv_network_tips.setText(getString(R.string.calling_incoming_2g));
                    break;
                default:
                    tv_network_tips.setText(getString(R.string.calling_incoming_3g));
            }
            if (!TextUtils.isEmpty(tv_network_tips.getText())) {
                tv_network_tips.setBackgroundResource(R.drawable.bg_callslogan);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected boolean finishIfNeed() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return true;
        } else if (!PWUtils.isNetWorkAvailable(this)) {
            showToast(this, "网络不可用");
            finish();
            return true;
        }
        return false;
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


    protected void doCallResponse(Message msg) {
        if (mCallTimer != null) {
            mCallTimer.cancel();
        }

        try {
            Bundle b = msg.getData();
            if (b == null)
                return;
            int code = b.getInt("code", -1);
            String data = b.getString("data");
            if (!TextUtils.isEmpty(data)) {
                JSONObject o = new JSONObject(data);
                JSONObject jsonUser = o.getJSONObject("user");

                JSONArray tagsArray = jsonUser.optJSONArray("tags");
                if (tagsArray != null && tagsArray.length() > 0)
                    tv_slogan.setText("#" + tagsArray.getString(0));
                mIncommingName = jsonUser.optString("name");

                int gender = jsonUser.optInt("gender");
                if (gender == 1) {
                    //对方是男的不能打赏,全面开放
                    //findViewById(R.id.tv_dasahng).setVisibility(View.GONE);
                }

                float price = PWUtils.getPriceByJsonValue(jsonUser.optDouble("price"));
                float mSrcPrice = getIntent().getFloatExtra("price", 0);
                if (price != mSrcPrice) {
                    if (price <= 0) {
                        //tv_newprice.setText("免费");
                    } else {
                        //tv_newprice.setText(String.format("%.1f元/分钟", price));
                    }
                }
            }
            switch (code) {
                case WEBRTC_CODE_NO_PERMISSION:
                    showToast(this, "您与对方没有通话权限，请重新申请");
                    endCallActivity(2000);
                    break;
                case ANSWER_CLOSURE:
                    //xxxTcpProxy.getInstance().releaseMediaPlayer();
                    resetPlayer();
                    showToast(this, "呼叫失败，此账号已被封禁");
                    endCallActivity(2000);
                    break;
                case CALL_CALLER_MONEY_LOW:
                    callerMoneyTooLow();
                    break;
                /******/
                case WEBRTC_CODE_BUSY:
                    //showToast(this, "对方正在通话中，请稍后再拨。");
                    endCallActivity(2000);
                    break;
                case CALL_CALLEE_OFFLINE:
                    //showToast(this, "对方不在线，我会转告他的。");
                    endCallActivity(2000);
                    break;
                case CALLEE_REJECT:
                    //showToast(this, "对方不方便接听电话，不如先发文字吧。");
                    endCallActivity(2000);
                    break;
                case ANSWER_TIMEOUT:
                    //showToast(this, "对方暂时无法接听电话，等会再试试吧。");
                    endCallActivity(2000);
                    break;
                /******/
            }
            if (code != -1 && code != 0) {
                ringMusic(code, true); // 这个里面是不应该播放音效的，soundpool那个
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void ringMusic(int state, boolean nosound) {
        // Trace.i(String.format("stop state == %d", state));
        //setAudioMode();
        if (state == CALLEE_REJECT) {
            // 对方拒绝
            //xxxTcpProxy.getInstance().playMusic(mGneder == 2 ? R.raw.reject1 : R.raw.reject2, false);
            if (mGneder == 2) {
                playMusicByPath("call_music/reject1.mp3", false);
            } else {
                playMusicByPath("call_music/reject2.mp3", false);
            }
            showToast(this, "对方不方便接听电话，不如先发文字吧。");
        } else if (state == CALL_CALLEE_BUSY) {
            // 对方正在通话中
            //xxxTcpProxy.getInstance().playMusic(mGneder == 2 ? R.raw.busy1 : R.raw.busy2, false);
            if (mGneder == 2) {
                playMusicByPath("call_music/busy1.mp3", false);
            } else {
                playMusicByPath("call_music/busy2.mp3", false);
            }
            showToast(this, "对方正在通话中，请稍后再拨。");
        } else if (state == CALL_CALLEE_OFFLINE
                || state == CALL_CALLEE_BACKGROUND) {
            // 不在线
            //xxxTcpProxy.getInstance().playMusic(mGneder == 2 ? R.raw.offline1 : R.raw.offline2, false);
            if (mGneder == 2) {
                playMusicByPath("call_music/offline1.mp3", false);
            } else {
                playMusicByPath("call_music/offline2.mp3", false);
            }
            showToast(this, "对方不在线，我会转告他的。");
        } else if (state == ANSWER_TIMEOUT) {
            // 超时 对方暂时无法接听电话，请稍后再拨！
            //xxxTcpProxy.getInstance().playMusic(mGneder == 2 ? R.raw.timeout1 : R.raw.timeout2, false);
            if (mGneder == 2) {
                playMusicByPath("call_music/timeout1.mp3", false);
            } else {
                playMusicByPath("call_music/timeout2.mp3", false);
            }
            showToast(this, "对方暂时无法接听电话，等会再试试吧。");
        } else if (state == ANSWER_CLOSURE) {
//			m_BinderCoreService.playMusic(mGneder == 2 ? R.raw.offline1 : R.raw.offline2,
//					false);
        } else if (state == CALLEE_REJECT) {
            //xxxTcpProxy.getInstance().releaseMediaPlayer();
//            if (mSoundPool != null && !noring && !nosound) {
//                mSoundPool.play(m_CallStop, 1.0f, 1.0f, 0, 0, 1.0f);
//            }
            if (!noring && !nosound) {
                playMusicByPath("call_music/call_stop.ogg", false);
            }
        } else {
            //xxxTcpProxy.getInstance().releaseMediaPlayer();
//            if (mSoundPool != null && !noring && !nosound) {
//                mSoundPool.play(m_CallStop, 1.0f, 1.0f, 0, 0, 1.0f);
//            }
            if (!noring && !nosound) {
                playMusicByPath("call_music/call_stop.ogg", false);
            }
        }
    }

    private boolean isHangupForMe = false;

    /**
     * 主动挂断电话
     */
    private void hangupCall(boolean isMe, int reason) {
        // 结束通话
        if (isMe) {
            isHangupForMe = true;
        }

        if (mCallFalg == DfineAction.OUTGOING_CALL) {
            // 拨打
            if (isMe) {
                if (reason == DfineAction.REAL_STOP_CALL_NORMAL) {
                    if (isCalling) {
                        reason = DfineAction.REAL_STOP_CALL_CALLING_NORMAL;
                    }
                }
                TcpProxy.getInstance().stopCallForMe(reason);
            }
            UmengStatisticsAgent.onEvent(this, UMEventIDS.UMECALLEND);
        } else if (mCallFalg == DfineAction.INCOMMING_CALL) {
            // 被呼叫
            if (CoreService.callState == RealCallState.INCOMING) {
                TcpProxy.getInstance().rejectCall(null);
                showToast(this, "已挂断");
                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMECALLREFUSE);
            } else {
                if (isMe) {
                    TcpProxy.getInstance().stopCallForMe(reason);
                }
                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMECALLEND);
            }
        }
        endCallActivity(1000);
    }

    private void answerCall() {
        // 听筒模式
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMECALLANSWER);
        //xxxTcpProxy.getInstance().releaseMediaPlayer();
        resetPlayer();
        // setAudioMode(TINGTONG_MODE);
        TcpProxy.getInstance().answerCall();
        //ll_call_switch.setVisibility(View.GONE);
        //ll_call_over.setVisibility(View.VISIBLE);
    }

    private void endCallActivity(long delay) {
        if (mCallTimer != null) {
            mCallTimer.cancel();
        }
        needNotify = false;
        noalert = true;
        noring = true;

        mHandler.removeCallbacksAndMessages(null);
        Observable.timer(delay, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(o -> {
            finish();
        });
    }

    protected void callerMoneyTooLow() {
        new AlertDialog.Builder(this)
                .setTitle("您的余额不足，请充值")
                .setPositiveButton("取消", (dialog, which) -> {
                            endCallActivity(500);
                        }
                )
                .setNegativeButton("去充值", (dialog, which) -> {
                            startActivity(new Intent(RealCallActivity.this, ChargeActivity.class));
                            endCallActivity(0);
                        }
                ).create().show();
    }

/*	protected void finishDelay(final String errorMsg) {
        needNotify = false;
		if (m_HandlerCoreServiceMsg != null) {
			m_HandlerCoreServiceMsg.postDelayed(new Runnable() {
                @Override
				public void run() {
					if (!TextUtils.isEmpty(errorMsg)) {
						showToast(RealCallActivity.this, errorMsg);
					}
                    System.out.println("finishDelay finish()");
                    finish();
                }
            }, 1000);
		} else {
			System.err.println("finishDelay m_HandlerCoreServiceMsg == null");
		}
    }*/

    protected int counttimeH;
    protected int counttimeM;
    protected int counttimeS;
    protected boolean isbreak = false;

    boolean isCalling = false;

    protected void countTime() {
        tv_call_duration.setVisibility(View.VISIBLE);

        isCalling = true;
        startCountTimeThread();
    }

    private void startCountTimeThread() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isbreak) {
                    mHandler.postDelayed(this, 1000);
                    counttimeS++;
                    mCountingDuration = new StringBuilder();
                    if (counttimeS == 60) {
                        counttimeS = 0;
                        counttimeM++;
                    }
                    if (counttimeM == 60) {
                        counttimeM = 0;
                        counttimeH++;
                    }
                    if (counttimeH > 0 && counttimeH < 10) {
                        mCountingDuration.append("0").append(counttimeH).append(":");
                    } else if (counttimeH > 10) {
                        mCountingDuration.append(counttimeH).append(":");
                    }
                    if (counttimeM < 10) {
                        mCountingDuration.append("0");
                    }
                    mCountingDuration.append(counttimeM).append(":");
                    if (counttimeS < 10) {
                        mCountingDuration.append("0");
                    }
                    mCountingDuration.append(counttimeS);
                    if (iv_duration_option.isSelected()) {
                        tv_call_duration.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                        tv_call_duration.setText(mCountingDuration.toString());
                    }
                }
            }
        });
    }

    protected String mIncommingName;

    protected void init() {
        requestAudioFocus();
        tv_voice_mode = findViewById(R.id.tv_voice_mode);
        tv_username = (TextView) findViewById(R.id.tv_username);
        tv_slogan = (TextView) findViewById(R.id.tv_slogan);
        tv_callstate = (TextView) findViewById(R.id.tv_callstate);
        tv_network_tips = (TextView) findViewById(R.id.tv_network_tips);
        //tv_uname = (TextView) findViewById(R.id.tv_uname);
        //iv_blur = (ImageView) findViewById(R.id.iv_blur);
        //ll_call_over = findViewById(R.id.ll_call_over);
        //iv_mianti_tingtong = (ImageView) findViewById(R.id.iv_mianti_tingtong);
        //iv_mianti_tingtong_layout = (LinearLayout) findViewById(R.id.iv_mianti_tingtong_layout);
        //ll_call_switch = findViewById(R.id.ll_call_switch);
        tv_call_duration = (TextView) findViewById(R.id.tv_call_duration);
        iv_duration_option = (ImageView) findViewById(R.id.iv_duration_option);
        click_area_layout = (LinearLayout) findViewById(R.id.click_area_layout);
        click_area_layout.setClickable(false);
        //tv_calling = (TextView) findViewById(R.id.tv_calling);
        //tv_callstate = (TextView) findViewById(R.id.tv_callstate);
        //iv_userface = (ImageView) findViewById(R.id.iv_userface);
        //v_gender_age = (GenderWithAgeView) findViewById(R.id.v_gender_age);
        //tv_address = (TextView) findViewById(R.id.tv_address);
        //tv_counting = (TextView) findViewById(R.id.tv_counting);
//        tv_newprice = (TextView) findViewById(R.id.tv_newprice);
//        tv_net_status = (TextView) findViewById(R.id.tv_net_status);

    }

    protected boolean requestAudioFocus() {
        return audioManager.requestAudioFocus(focusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            // Trace.i("focusChange == " + focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    // do somthing
                    // if (getIntent() != null
                    // && getIntent().getIntExtra("flag", 0) ==
                    // CoreService.OUTGOING_CALL) {
                    // playerSound(R.raw.outgoing);
                    // } else {
                    // playerSound(R.raw.incomming);
                    // }
                    break;
                case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // do somthing
                    audioManager.abandonAudioFocus(focusChangeListener);
                    // releaseMediaPlayer();
                    break;
            }
        }
    };

/*	private void countTime30S() {
        recoredSecond = 30;
		m_HandlerCoreServiceMsg.post(new Runnable() {
			@Override
			public void run() {
				if (recoredSecond < 0) {
					stopRecord();
					return;
				}
				m_HandlerCoreServiceMsg.postDelayed(this, 1000);
				recoredSecond--;
			}
		});
	}*/

    /**
     * 录音功能，v1.6.3屏蔽。
     */
/*	private void getRecPermission(){
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		ApiRequestWrapper.openAPIGET(getApplicationContext(), params, AsynHttpClient.API_GETUPLOADCONFIG, new MsgStructure() {
			@Override
			public void onReceive(JSONObject data) {
				String permission = data.optString("switch");
				if("1".equals(permission)){
					startRecord();
				}
			}
			
			@Override
			public void onError(int error, Object ret) {
				
			}
		});
	}*/
    @Override
    protected int getTargetUid() {
        return mTargetUid;
    }

    @Override
    protected void reChargeMoney() {
        Intent intent = new Intent(this, ChargeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        runInBackground();
    }

    public void click(View v) {
        if (PWUtils.isMultiClick()) {
            return;
        }
        int id = v.getId();
        switch (id) {
            case R.id.tv_dasahng:
                //type	y	number	1: 匿名聊， 2: 普通电话
                sendIntentRewardMessage(mTargetUid, "2", null);
                break;
            case R.id.view_start_userinfo:
                //needNotify = false;
                Intent userinfoIntent = new Intent(this, UserInfoActivity.class);
                userinfoIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                userinfoIntent.putExtra(UserInfoActivity.TARGET_UID, mTargetUid);
                startActivity(userinfoIntent);
                runInBackground();
                break;
//            case R.id.view_call_jujue:
//                hangupCall(true, DfineAction.REAL_STOP_CALL_NORMAL);
//                break;
            case R.id.view_answer_call:
                answerCall();
                break;
            case R.id.view_call_jujue:
                if (mCallFalg == DfineAction.INCOMMING_CALL && !isCalling) {
                    answerMessage();
                    return;
                }
                hangupCall(true, DfineAction.REAL_STOP_CALL_NORMAL);
                break;
            case R.id.tv_voice_mode:
                setAudioMode();
                break;
            case R.id.iv_push_top:
                // 收起
                runInBackground();
                break;
//            case R.id.iv_call_sendmsg:
//                // 快速回复
//                answerMessage();
//                UmengStatisticsAgent.onEvent(this, UMEventIDS.UMEMESSAGEREPLY);
//                break;
            case R.id.click_area_layout:
                boolean isSelect = iv_duration_option.isSelected();
                updateDurationView(isSelect);
                iv_duration_option.setSelected(!isSelect);
                break;
            default:
                break;
        }
    }

    private void updateDurationView(boolean isSelect) {
        if (isSelect) {
            iv_duration_option.setImageResource(R.drawable.icon_hide_duration);
            tv_call_duration.setText(getResources().getString(R.string.is_speaking));
            tv_call_duration.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tv_call_duration.getLayoutParams();
            lp.rightMargin = 0;
        } else {
            iv_duration_option.setImageResource(R.drawable.icon_show_duration);
            tv_call_duration.setText(mCountingDuration);
            tv_call_duration.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tv_call_duration.getLayoutParams();
            lp.rightMargin = PWUtils.getPXbyDP(this, 5);
        }
    }

    protected void answerMessage() {
        String[] items = getResources().getStringArray(R.array.call_reject_arrays);
        new AlertDialog.Builder(this)
                .setTitle("挂断来电并信息回复")
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                            sendAnswerMessage(items[which]);
                            break;
                        case 5:
                            alertSendMessage();
                            break;
                        default:
                            break;
                    }
                })
                .create().show();
    }

    protected void sendAnswerMessage(String message) {
        noalert = true;
        //xxxTcpProxy.getInstance().releaseMediaPlayer();
        resetPlayer();
        message = message == null ? "" : message;
        TcpProxy.getInstance().rejectCall(message);
    }

    protected void alertSendMessage() {
        noalert = true;
        InputBoxView boxView = InputBoxView.newInstance();
        boxView.show(getSupportFragmentManager(), "boxview");
        boxView.setOnInputConfirmListener(rst -> TcpProxy.getInstance().rejectCall(rst));
    }

    protected void runInBackground() {
        moveTaskToBack(true);
    }

    protected void notifyOnBackgroundCall() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setTicker("后台打电话").setContentText("后台打电话")
                .setWhen(System.currentTimeMillis()).setAutoCancel(true)
                .setOngoing(true);
        Intent resultIntent = new Intent(this, RealCallActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        mBuilder.setContentIntent(resultPendingIntent);
        mNotifyMgr.notify(Constans.NOTIFY_ID_CALL_BACKGROUND, mBuilder.build());
        PeiwoApp.getApplication().realCallNotification = true;
    }

    @Override
    public void onPause() {
        LinphoneManager.getInstance().stopProximitySensorForActivity(this);
        super.onPause();
        if (needNotify) {
            notifyOnBackgroundCall();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onResume() {
        // Trace.i("real call activity onresume");
        PeiwoApp.getApplication().sStartTime = System.currentTimeMillis();
        LinphoneManager.getInstance().startProximitySensorForActivity(RealCallActivity.this);
        PeiwoApp.getApplication().realCallNotification = false;
        PeiwoApp.getApplication().realCallNotificationReStart = false;
        mNotifyMgr.cancel(Constans.NOTIFY_ID_CALL_BACKGROUND);
        slideView.reset();
        super.onResume();
    }


    public void onStop() {
        super.onStop();
        if (needNotify) {
            try {
                ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
                if (runningTaskInfos != null) {
                    String topString = (runningTaskInfos.get(0).topActivity).toString();
                    if (!TextUtils.isEmpty(topString) && !topString.contains("me.peiwo")) {
                        PeiwoApp.getApplication().realCallNotificationReStart = true;
                    } else {
                        PeiwoApp.getApplication().realCallNotificationReStart = false;
                    }
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    protected long exitTime = 0;

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                Toast.makeText(this, "再按一次退出通话", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
                return true;
            }
            hangupCall(true, DfineAction.REAL_STOP_CALL_NORMAL);
        }
        return super.onKeyDown(keyCode, event);
    }


    // @Override
    // protected void onStop() {
    // if (!isActiveBackgroundCall) {
    // Trace.i("setOnBackgroundCall true");
    // isActiveBackgroundCall = false;
    // setOnBackgroundCall(true);
    // }
    // super.onStop();
    // }

    @Override
    public void finish() {
        mHandler.removeCallbacksAndMessages(null);
        //xxxTcpProxy.getInstance().releaseMediaPlayer();
        PeiwoApp.getApplication().realCallNotification = false;
        PeiwoApp.getApplication().realCallNotificationReStart = false;
        isbreak = true;
        EventBus.getDefault().post(new MessagePushEvent());
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        app.setCalling(false, PeiwoApp.CALL_TYPE.CALL_NONE);
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void releasePlayer() {
//        Intent intent = new Intent(this, PlayerService.class);
//        intent.setAction(PlayerService.PLAY_ACTION_RELEASE);
//        startService(intent);
        //AudioPlayerUtil.releasePlayer(this);
        PlayerService.getInstance().releasePlayerCommand();
    }

    protected void clearBackgroundNotification() {
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(Constans.NOTIFY_ID_CALL_BACKGROUND);
    }


//    protected void releaseSoundPool() {
//        try {
//            if (mSoundPool != null) {
//                mSoundPool.release();
//                mSoundPool = null;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//    }
//
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        //Trace.i("event.values[0] == " + event.values[0]);
//        //Trace.i("getMaximumRange() == " + sProximity.getMaximumRange());
//        //暂时去掉该功能
//        if (event.values[0] < sProximity.getMaximumRange()) {
//            if (ll_screen_bg.getVisibility() == View.GONE)
//                ll_screen_bg.setVisibility(View.VISIBLE);
//        } else {
//            if (ll_screen_bg.getVisibility() == View.VISIBLE)
//                ll_screen_bg.setVisibility(View.GONE);
//
//        }
//    }

    @Override
    protected void onDestroy() {
        releasePlayer();
        resetAudioMode();
        clearBackgroundNotification();
        DfineAction.CURRENT_CALL_STATUS = DfineAction.CURRENT_CALL_NOT;
        cancelListenerSystemCallState();
        EventBus.getDefault().unregister(this);
        if (headSetPlugReceiver != null)
            unregisterReceiver(headSetPlugReceiver);
        PeiwoApp.getApplication().realCallNotification = false;
        PeiwoApp.getApplication().realCallNotificationReStart = false;
        mHandler.removeCallbacksAndMessages(null);
        //releaseSoundPool();
        super.onDestroy();
    }


    protected class MyPhoneListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    //响铃状态
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //接听状态
                    if (isFinishing()) return;
                    hangupCall(true, DfineAction.REAL_STOP_CALL_SYSTEM_PHONE);
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    private class HeadSetPlugReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")) {
                if (intent.getIntExtra("state", 0) == 0) {
                    // 耳机拔出
                    if (tv_voice_mode.isSelected()) {
                        setVoiceModeNomal();
                    } else {
                        setVoiceModeCommunication();
                    }
                    return;
                } else if (intent.getIntExtra("state", 0) == 1) {
                    // 耳机插入
                    setVoiceModeCommunication();
                }
            }
        }
    }

    /**
     * 听筒模式
     * <p>
     * audioManager
     */
    private void setVoiceModeCommunication() {
        tv_voice_mode.setSelected(true);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(false);
        }
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, (int) (maxVolume * 0.9), 0);
        UmengStatisticsAgent.onEvent(this, UMEventIDS.UMECALLHANDSFREE);
        // tv_voice_mode.setText("免提");
    }

    /**
     * 扬声器模式
     * <p>
     * audioManager
     */
    private void setVoiceModeNomal() {
        tv_voice_mode.setSelected(false);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        if (!audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(true);
        }
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (maxVolume * 0.8), 0);
    }

    private void setAudioMode() {
        //Log.i("setaudio", "call set audio mode");
        if (audioManager.isWiredHeadsetOn()) {
            //插耳机状态下，只改变图标，不改变播放模式
            if (tv_voice_mode.isSelected()) {
                tv_voice_mode.setSelected(false);
            } else {
                tv_voice_mode.setSelected(true);
            }
            return;
        }

        if (audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            //当前为听筒模式
            setVoiceModeNomal();
        } else {
            setVoiceModeCommunication();
        }
    }

    protected void alertUser(String data) {
        try {
            JSONObject oo = new JSONObject(data);
            String result = null;
            int state = -1;
            if (oo.has("data")) {
                JSONObject o = oo.getJSONObject("data");
                result = o.optString("msg");
                state = o.optInt("state", -1);
            }
            if (!noalert) {
                if (!TextUtils.isEmpty(result)) {
                    showToast(this, result);
                } else {
                    showToast(this, "对方已挂断!");
                }
            }
            ringMusic(state, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected boolean needPlayMusic() {
        try {
            String push_str = SharedPreferencesUtil.getStringExtra(this, Constans.SP_KEY_PUSH_STR, "");
            if (!TextUtils.isEmpty(push_str)) {
                JSONObject o = new JSONObject(push_str);
                if (o.has("sound")) {
                    return o.optBoolean("sound", true);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onScrollUpFinish() {
        runInBackground();
    }

    public int getTuid() {
        try {
            String data = getIntent().getStringExtra("data");
            if (TextUtils.isEmpty(data)) return 0;
            JSONObject json = new JSONObject(data);
            JSONObject user = json.optJSONObject("user");
            String uid = user.optString("uid");
            return Integer.parseInt(uid);
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void onEventMainThread(RealCallMessageEvent event) {
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
        message.setData(b);
        mHandler.sendMessage(message);
    }

    public void onEventMainThread(CallStateEvent event) {
        if (event == null)
            return;
        if (event.type == 0) {
            Message message = mHandler.obtainMessage();
            message.what = CoreService.CALL_STATE_CHANGE;
            message.obj = event;
            mHandler.sendMessage(message);
        } else if (event.type == 1) {
            Message message = mHandler.obtainMessage();
            message.what = HANDLE_NET_STATUS_CHANGE;
            message.obj = event;
            mHandler.sendMessage(message);
        } else if (event.type == 2) {
            showToast(this, "语音连接超时,已挂断");
            hangupCall(true, DfineAction.REAL_STOP_CALL_WEBRTC_TIMEOUT);
        }
    }
}
