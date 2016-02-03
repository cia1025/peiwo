package me.peiwo.peiwo.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.BaseActivity;
import me.peiwo.peiwo.activity.LazyGuyActivity;
import me.peiwo.peiwo.callback.DownloadCallback;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.PWDownloader;
import me.peiwo.peiwo.service.PlayerService;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.FileManager;
import me.peiwo.peiwo.util.Md5Util;
import me.peiwo.peiwo.util.PWUtils;
import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoxiang on 2015/10/19.
 */
public class LazyVoiceLayout extends RelativeLayout implements View.OnClickListener, MediaPlayer.OnErrorListener {
    private final Context mContext;
    private ImageView iv_play_btn;
    private Chronometer chronometer_time;
    private State mState;
    private static final int WAVELINE_COUNTS = 40;
    private static final int ANIMATION_DURATION = 400;
    private MyHandler mHandler;
    private static final int LOAD_LAZY_VOICE = 0x10;
    private static final int LOAD_LAZY_VOICE_ERROR = 0x11;
    private Chronometer tv_voice_total_time;
    private List<Float> mLinesHeightList;
    private List<View> mLinesList;
    private String mFilePath;

    enum State {
        IDLE, PLAYING, PAUSE, RESUME, PLAYDONE
    }

    public LazyVoiceLayout(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public LazyVoiceLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public LazyVoiceLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        mState = State.IDLE;
        mLinesHeightList = new ArrayList<>();
        mLinesList = new ArrayList<>();
        //setBackgroundColor(getResources().getColor(R.color.recorder_blue_color));
        iv_play_btn = new ImageView(mContext);
        chronometer_time = new Chronometer(mContext);
        tv_voice_total_time = new Chronometer(mContext);
        iv_play_btn.setOnClickListener(this);
        iv_play_btn.setImageResource(R.drawable.icon_play_lazy);
        RelativeLayout.LayoutParams lp_play_btn = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams lp_chronometer = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams lp_time = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp_play_btn.addRule(CENTER_VERTICAL);
        lp_play_btn.leftMargin = PWUtils.getPXbyDP(mContext, 14);
        lp_play_btn.rightMargin = PWUtils.getPXbyDP(mContext, 22);
        lp_time.addRule(ALIGN_PARENT_RIGHT | ALIGN_PARENT_TOP);
        lp_time.rightMargin = PWUtils.getPXbyDP(mContext, 12);
        lp_time.topMargin = PWUtils.getPXbyDP(mContext, 6);
        tv_voice_total_time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        lp_chronometer.addRule(ALIGN_PARENT_RIGHT | ALIGN_PARENT_TOP);
        lp_chronometer.rightMargin = PWUtils.getPXbyDP(mContext, 42);
        lp_chronometer.topMargin = PWUtils.getPXbyDP(mContext, 6);
        chronometer_time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        chronometer_time.setTextColor(getResources().getColor(R.color.lazy_guy_voice_time_color));
        tv_voice_total_time.setTextColor(getResources().getColor(R.color.lazy_guy_voice_time_color));
        addView(iv_play_btn, lp_play_btn);
        addView(tv_voice_total_time, lp_time);
        addView(chronometer_time, lp_chronometer);
        addWaveLineViews();
        addBaseLine();
        int padding = PWUtils.getPXbyDP(mContext, 35);
        setPadding(0, 0, 0, padding);
        boolean netAvailable = PWUtils.isNetWorkAvailable(mContext);
        if (netAvailable) {
            fetchDefaultVoice();
        } else {
            if (mContext instanceof BaseActivity)
                ((BaseActivity) mContext).showToast(mContext, getResources().getString(R.string.umeng_common_network_break_alert));
        }
        mHandler = new MyHandler();
//        mMediaPlayer.setOnErrorListener(this);
    }

    @Override
    public void onClick(View view) {
        if(mContext instanceof LazyGuyActivity){
            ((LazyGuyActivity) mContext).stopPlaying();
        }
        switch (mState) {
            case IDLE:
//                if (mMediaPlayer != null && (mMediaPlayer.getDuration() / 1000) > 0) {
//                    CustomLog.d("2media player duration is : " + (mMediaPlayer.getDuration() / 1000));
                    updateState(State.PLAYING, true);
//                } else {
//                    boolean netAvailable = PWUtils.isNetWorkAvailable(mContext);
//                    if (netAvailable) {
//                        fetchDefaultVoice();
//                    } else {
//                        if (mContext instanceof BaseActivity)
//                            ((BaseActivity) mContext).showToast(mContext, getResources().getString(R.string.umeng_common_network_break_alert));
//                    }
//                }
                break;
            case PLAYING:
                updateState(State.PAUSE, false);
                break;
            case PAUSE:
                updateState(State.RESUME, false);
                break;
            case RESUME:
                updateState(State.PAUSE, false);
                break;
            case PLAYDONE:
                updateState(State.PLAYING, true);
                break;
            default:
                break;
        }
    }

    private void updateState(State state, boolean restartTimer) {
        mState = state;
        long baseTime;
        if (restartTimer) {
            baseTime = SystemClock.elapsedRealtime();
        } else {
            baseTime = convertStrToLong(chronometer_time.getText().toString());
        }
        chronometer_time.setBase(baseTime);
        switch (mState) {
            case IDLE:
                iv_play_btn.setImageResource(R.drawable.icon_play_lazy);
                chronometer_time.stop();
                break;
            case PLAYING:
                iv_play_btn.setImageResource(R.drawable.icon_pause_lazy);
                startVoiceAnimation();
//                playLazyVoice();
                playLocalVoice();
                chronometer_time.start();
                break;
            case PAUSE:
                iv_play_btn.setImageResource(R.drawable.icon_play_lazy);
//                mMediaPlayer.pause();
                PlayerService.getInstance().playPauseCommand();
                chronometer_time.stop();
                break;
            case RESUME:
                iv_play_btn.setImageResource(R.drawable.icon_pause_lazy);
                startVoiceAnimation();
                PlayerService.getInstance().playResumeCommand();
                chronometer_time.start();
                break;
            case PLAYDONE:
                iv_play_btn.setImageResource(R.drawable.icon_play_lazy);
                chronometer_time.stop();
//                stopPlaying();
                break;
            default:
                break;
        }
    }

    private long convertStrToLong(String strTime) {
        String[] timeArray = strTime.split(":");
        long longTime = 0;
        //Time format is MM:SS
        if (timeArray.length == 2) {
            longTime = Integer.parseInt(timeArray[0]) * 1000 * 60 + Integer.parseInt(timeArray[1]) * 1000;
        }
        return SystemClock.elapsedRealtime() - longTime;
    }

    private void fetchDefaultVoice() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        ApiRequestWrapper.openAPIGET(mContext, params, AsynHttpClient.API_SETTING_SYSTEM, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                String lazy_voice_url = data.optString("default_voice_url");
                File target = new File(FileManager.getVoicePath(), Md5Util.getMd5code(lazy_voice_url));
                mFilePath = target.getAbsolutePath();
                CustomLog.d("lazy_voice_url is : " + lazy_voice_url);
                if (target.exists() && target.length() > 0) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = LOAD_LAZY_VOICE;
                    mHandler.sendMessage(msg);
                    CustomLog.d("file is exist.");
                } else {
                    CustomLog.d("file is not exist.");

                    PWDownloader downloader = PWDownloader.getInstance();
                    downloader.add(lazy_voice_url, target, new DownloadCallback() {
                        @Override
                        public void onComplete(String path) {
                            Message msg = mHandler.obtainMessage();
                            msg.what = LOAD_LAZY_VOICE;
                            mHandler.sendMessage(msg);
                        }

                        @Override
                        public void onFailure(String path, IOException e) {

                        }
                    });
                }
            }

            @Override
            public void onError(int error, Object ret) {

            }
        });
    }

    public void loadLazyVoice() {
        MediaPlayer player = new MediaPlayer();
        try {
//            if (mMediaPlayer == null) {
//                mMediaPlayer = new MediaPlayer();
//            }

            player.reset();
            player.setDataSource(mFilePath);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int length = player.getDuration();
        CustomLog.d("voice file length is : " + length);
        String ms = PWUtils.FormatHMS(length / 1000);
        tv_voice_total_time.setText("/" + ms);
    }

    private void playLocalVoice() {
        PlayerService playerService = PlayerService.getInstance();
        playerService.setOnCompletionListener(mp -> {
            CustomLog.d("playLocalVoice. onComplete.");
            updateState(State.IDLE, true);
        });
        playerService.setOnErrorListener((mp, what, extra) -> {
            CustomLog.d("onError. peiwo mediaplayer meet error");
            return false;
        });
        playerService.setOnPrepareCompletedListener(() -> {
            CustomLog.d("playLocalVoice ready to play");
        });
        if(!TextUtils.isEmpty(mFilePath)){
            playerService.playCommand(mFilePath, false);
        }
    }

    private void addBaseLine() {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.height = PWUtils.getPXbyDP(mContext, 1);
        lp.leftMargin = PWUtils.getPXbyDP(mContext, 63);
        lp.addRule(ALIGN_PARENT_BOTTOM);
        ImageView baseLine = new ImageView(mContext);
        baseLine.setBackgroundColor(getResources().getColor(R.color.c_de2));
        addView(baseLine, lp);
    }

    private void addWaveLineViews() {
        for (int i = 0; i < WAVELINE_COUNTS; i++) {
            ImageView lineView = createLineView(i);
            float toPosY = (float) Math.random();
            mLinesHeightList.add(toPosY);
            lineView.setPivotY(lineView.getHeight());
            mLinesList.add(lineView);
            addView(lineView);
//            lineView.setPivotY(lineView.getHeight());
//            ObjectAnimator anim = ObjectAnimator.ofFloat(lineView, "ScaleY", 0, toPosY);
//            anim.start();
        }
    }

    private ImageView createLineView(int i) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        ImageView iv = new ImageView(mContext);
        iv.setImageResource(R.drawable.line);
        lp.leftMargin = PWUtils.getPXbyDP(mContext, 60) + (PWUtils.getPXbyDP(mContext, 6) * i);
        lp.width = PWUtils.getPXbyDP(mContext, 8);
        lp.topMargin = PWUtils.getPXbyDP(mContext, 30);
        iv.setLayoutParams(lp);
        return iv;
    }

    private void startVoiceAnimation() {
        if (!isPlaying() && !isResume()) {
            return;
        }
        int childCount = mLinesList.size();
        for (int i = 0; i < childCount; i++) {
            View child = mLinesList.get(i);
            //用宽度来判断是否是lineView，不太严谨，以后改进
            if (child.getWidth() <= PWUtils.getPXbyDP(mContext, 8)) {
//                CustomLog.d("child("+i+") width is : "+child.getWidth());
                float toPosY = (float) Math.random();
                float fromPosY = mLinesHeightList.get(i);
                mLinesHeightList.remove(i);
                mLinesHeightList.add(i, toPosY);
                child.setPivotY(child.getHeight());
                ObjectAnimator anim = ObjectAnimator.ofFloat(child, "ScaleY", fromPosY, toPosY);
                anim.setDuration(ANIMATION_DURATION).start();
                if (i == childCount - 1) {
                    anim.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            CustomLog.d("onAnimationStart.");
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            startVoiceAnimation();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                }
            }
        }
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_LAZY_VOICE:
                    if (!mFilePath.equals("")) {
                        loadLazyVoice();
                    }
                    break;
                case LOAD_LAZY_VOICE_ERROR:
                    CustomLog.e("load lazy voice error.");
                    break;
                default:
                    break;
            }
        }
    }

    private boolean isIdle() {
        return mState == State.IDLE;
    }

    private boolean isPlaying() {
        return mState == State.PLAYING;
    }

    private boolean isResume() {
        return mState == State.RESUME;
    }

    private boolean isPlayDone() {
        return mState == State.PLAYDONE;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        CustomLog.e("onError, MediaPlayer meet error.");
        releaseMediaPlayer();
        return false;
    }


    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        CustomLog.d("visibility is : " + visibility);
        if (visibility == GONE) {
            releaseMediaPlayer();
        }
    }

    private void releaseMediaPlayer() {
        CustomLog.d("release media player.");
        PlayerService.getInstance().resetPlayerCommand();
    }

    public void pauseMediaPlayer() {
        CustomLog.d("pause media player");
        if(PlayerService.getInstance().isPlaying())
            updateState(State.PAUSE, false);
    }
}
