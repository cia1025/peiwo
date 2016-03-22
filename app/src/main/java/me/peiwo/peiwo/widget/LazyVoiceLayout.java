package me.peiwo.peiwo.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoxiang on 2015/10/19.
 */
public class LazyVoiceLayout extends RelativeLayout implements MediaPlayer.OnErrorListener {
    private final Context mContext;
    private ImageView iv_play_btn;
    private Chronometer chronometer_time;
    private TextView tv_voice_total_time;
    private State mState;
    private static final int WAVELINE_COUNTS = 40;
    private static final int ANIMATION_DURATION = 400;
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

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_lazy_preview, this);
        tv_voice_total_time = (TextView) findViewById(R.id.tv_voice_total_time);
        chronometer_time = (Chronometer) findViewById(R.id.chronometer_time);
        iv_play_btn = (ImageView) findViewById(R.id.iv_play_default_btn);
        mState = State.IDLE;
        mLinesHeightList = new ArrayList<>();
        mLinesList = new ArrayList<>();
        addWaveLineViews();
        boolean netAvailable = PWUtils.isNetWorkAvailable(mContext);
        if (netAvailable) {
            fetchDefaultVoice();
        } else {
            if (mContext instanceof BaseActivity) {
                ((BaseActivity) mContext).showToast(mContext, getResources().getString(R.string.umeng_common_network_break_alert));
            }
        }
        iv_play_btn.setOnClickListener(v -> {
            if (mContext instanceof LazyGuyActivity) {
                ((LazyGuyActivity) mContext).stopPlaying();
            }
            switch (mState) {
                case IDLE:
                    updateState(State.PLAYING, true);
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
        });
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
                    Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                        loadLazyVoice();
                    });
                    CustomLog.d("file is exist.");
                } else {
                    CustomLog.d("file is not exist.");
                    PWDownloader downloader = PWDownloader.getInstance();
                    downloader.add(lazy_voice_url, target, new DownloadCallback() {
                        @Override
                        public void onComplete(String path) {
                            loadLazyVoice();
                        }

                        @Override
                        public void onFailure(String path, IOException e) {

                        }
                    });
                }
            }

            @Override
            public void onError(int error, Object ret) {
                String tips = "failed " + error;
                if (ret instanceof JSONObject) {
                    tips = ((JSONObject) ret).optString("msg");
                }
                Toast.makeText(mContext, tips, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadLazyVoice() {
        MediaPlayer player = new MediaPlayer();
        try {
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
        if (!TextUtils.isEmpty(mFilePath)) {
            playerService.playCommand(mFilePath, false);
        }
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
        lp.bottomMargin = PWUtils.getPXbyDP(mContext, 38);
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
        if (PlayerService.getInstance().isPlaying())
            updateState(State.PAUSE, false);
    }
}
