package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import butterknife.Bind;
import com.qiniu.android.http.ResponseInfo;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.callback.DownloadCallback;
import me.peiwo.peiwo.callback.UploadCallback;
import me.peiwo.peiwo.net.*;
import me.peiwo.peiwo.service.RecorderWorker;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.FileManager;
import me.peiwo.peiwo.util.Md5Util;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.widget.LazyVoiceLayout;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LazyGuyActivity extends BaseActivity implements View.OnTouchListener, MediaRecorder.OnErrorListener {
    private ImageView mic_btn;
    private ImageView guide_state;
    @Bind(R.id.layout_rerecorder)
    RelativeLayout layout_rerecorder;
    @Bind(R.id.layout_replay)
    RelativeLayout layout_replay;
    @Bind(R.id.guide_audition)
    ImageView guide_audition;
    @Bind(R.id.iv_play_btn)
    ImageView iv_play_btn;
    private Chronometer chronometer;
    private String mVoiceFileName;
    private String mVoiceFilePath = "";
    private LazyVoiceLayout mLazyLayout;
    private static final int RECORDER_TIME_AT_LEAST = 2;
    private static final int RECORDER_TIME_AT_MOST = 6;

    //Server定义的没有上传过懒人录音的code值
    public static final int HAVE_NOT_UPLOAD_YET = 20005;
    private static final String LAZY_VOICE = "lazy_voice_";
    public static final String NEED_FOCUS_USER = "need_focus_user";
    private Handler mHandler;
    private RecorderTimer mRecorderTimer;
    private String server_lazy_voice_url;
    private boolean hasLazyVoice;
    private MediaPlayer mPlayer;
    private RecorderWorker mRecorder;

    enum State {
        IDLE, PLAYING, SPEAKING, PAUSE, RECORDER_DONE, PLAY_DONE, NONE
    }

    private State mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_lazy_guy);
        init();
    }

    private void init() {
        createPlayer();
        mHandler = new Handler();
        mRecorderTimer = new RecorderTimer();
        mic_btn = (ImageView) findViewById(R.id.iv_mic_btn);
        guide_state = (ImageView) findViewById(R.id.iv_guide_state);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        mic_btn.setOnTouchListener(this);
        mLazyLayout = (LazyVoiceLayout) findViewById(R.id.lazy_voice_layout);
        showAnimLoading();
        ApiRequestWrapper.openAPIGET(this, new ArrayList<NameValuePair>(), AsynHttpClient.API_USERINFO_LAZY_VOICE, new MsgStructure() {

            @Override
            public void onReceive(JSONObject data) {
                hasLazyVoice = true;
                server_lazy_voice_url = data.optString("voice_url");
                Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                    updateStatus(State.IDLE);
                    dismissAnimLoading();
                });
            }

            @Override
            public void onError(int error, Object ret) {
                if (error == HAVE_NOT_UPLOAD_YET) {
                    Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                        updateStatus(State.IDLE);
                        dismissAnimLoading();
                    });
                } else {
                    CustomLog.e("lazy voice API return error : " + error);
                }
            }
        });
    }

    private void updateStatus(State state) {
        CustomLog.d("update status. mVoice local path is : " + mVoiceFilePath);
        mHandler.removeCallbacks(mRecorderTimer);
        setState(state);
        switch (mState) {
            case IDLE:
                iv_play_btn.setClickable(true);
                guide_state.setVisibility(View.VISIBLE);
                guide_state.setImageResource(R.drawable.guide_tap_to_speak);
                layout_rerecorder.setVisibility(View.GONE);
                iv_play_btn.setImageResource(R.drawable.icon_play);
                guide_audition.setImageResource(R.drawable.last_lazy_voice);
                if (hasLazyVoice) {
                    layout_replay.setVisibility(View.VISIBLE);
                } else {
                    layout_replay.setVisibility(View.GONE);
                }
                mic_btn.setImageResource(R.drawable.icon_lazy_speak);
                chronometer.setVisibility(View.GONE);
                break;
            case PLAYING:
                iv_play_btn.setClickable(false);
                iv_play_btn.setImageResource(R.drawable.icon_voic_pause);
                break;
            case SPEAKING:
                layout_replay.setVisibility(View.INVISIBLE);
                guide_state.setVisibility(View.VISIBLE);
                guide_state.setImageResource(R.drawable.guide_recording);
                chronometer.setVisibility(View.VISIBLE);
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                mHandler.postDelayed(mRecorderTimer, RECORDER_TIME_AT_MOST * 1000);
                break;
            case RECORDER_DONE:
                iv_play_btn.setClickable(true);
                guide_state.setVisibility(View.GONE);
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.stop();
                chronometer.setVisibility(View.GONE);
                layout_replay.setVisibility(View.VISIBLE);
                layout_rerecorder.setVisibility(View.VISIBLE);
                guide_audition.setImageResource(R.drawable.guide_audition);
                iv_play_btn.setImageResource(R.drawable.icon_play);
//                mic_btn.setImageResource(R.drawable.icon_complete);
                mic_btn.setImageResource(R.drawable.ic_lazy_save);
                break;
            case PLAY_DONE:
                iv_play_btn.setClickable(true);
                guide_state.setVisibility(View.GONE);
                chronometer.setVisibility(View.GONE);
                layout_rerecorder.setVisibility(View.VISIBLE);
                iv_play_btn.setImageResource(R.drawable.icon_play);
//                mic_btn.setImageResource(R.drawable.icon_complete);
                mic_btn.setImageResource(R.drawable.ic_lazy_save);
                break;
            default:
                break;
        }
    }

    class RecorderTimer implements Runnable {
        @Override
        public void run() {
            if (isSpeakingState()) {
                stopRecorder(false);
                updateStatus(State.RECORDER_DONE);
            }
        }
    }

    public void click(View v) {
        mLazyLayout.pauseMediaPlayer();
        switch (v.getId()) {
            case R.id.v_back:
                finish();
                break;
            case R.id.iv_play_btn:
                if (!isPlaying()) {
                    if (isIdleState()) {
                        playLastLazyVoice();
                    } else {
                        playRecorderVoice();
                    }
                    updateStatus(State.PLAYING);
                }
                break;
            case R.id.iv_rerecorder:
                resetPlayer();
                updateStatus(State.IDLE);
                deleteRecorderFile(new File(mVoiceFilePath));
                resetPlayer();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (v.getId() == R.id.iv_mic_btn) {
                    if (isIdleState()) {
                        updateStatus(State.SPEAKING);
                        startRecorder();
                    }
                    if (isSpeakingState()) {
                        mLazyLayout.pauseMediaPlayer();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (v.getId() == R.id.iv_mic_btn) {
                    CustomLog.d("ACTION_UP, mState is : " + mState);
                    if (isSpeakingState()) {
                        stopRecorder(true);
                        if (getFileDuration() < RECORDER_TIME_AT_LEAST) {
                            updateStatus(State.IDLE);
                            showToast(this, getResources().getString(R.string.tap_is_too_short));
                            return true;
                        }
                        updateStatus(State.RECORDER_DONE);
                    } else if (isRecordDone() || isPlayDone()) {
                        if (getFileDuration() >= RECORDER_TIME_AT_LEAST) {
                            showAlertDialog();
                        }
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void showAlertDialog() {
//        Resources res = getResources();
//        boolean needFocus = getIntent().getBooleanExtra(NEED_FOCUS_USER, false);
//        String alert_str = res.getString(R.string.confirm_to_save_lazy_guy_recorder);
//        String confirm_str = res.getString(R.string.save);
//        if (needFocus) {
//            confirm_str = res.getString(R.string.cc);
//            alert_str = res.getString(R.string.cc_lazy_voice_and_focus);
//        }
//        new AlertDialog.Builder(this).setTitle(alert_str)
//                .setNegativeButton(res.getString(R.string.cancel), null)
//                .setPositiveButton(confirm_str, (dialog, which) -> {
//                        boolean netAvailable = PWUtils.isNetWorkAvailable(LazyGuyActivity.this);
//                        if (netAvailable) {
//                            doUploadVoiceFile();
//                        } else {
//                            showToast(LazyGuyActivity.this, getResources().getString(R.string.umeng_common_network_break_alert));
//                        }
//                    }
//                ).create().show();

        boolean netAvailable = PWUtils.isNetWorkAvailable(LazyGuyActivity.this);
        if (netAvailable) {
            doUploadVoiceFile();
        } else {
            showToast(LazyGuyActivity.this, getResources().getString(R.string.umeng_common_network_break_alert));
        }
    }

    private void startRecorder() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return;
        }
        mVoiceFileName = String.valueOf(System.currentTimeMillis());
        CustomLog.d("begin recorder, time is : " + mVoiceFileName);
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File peiwoFolder = new File(sdcardPath);
        if (!peiwoFolder.exists()) {
            if (!peiwoFolder.mkdirs())
                return;
        }
        File voiceFolder = new File(peiwoFolder, FileManager.VOICE_FOLDER);
        if (!voiceFolder.exists()) {
            if (!voiceFolder.mkdirs())
                return;
        }
        mVoiceFilePath = voiceFolder + "/" + LAZY_VOICE + mVoiceFileName + ".amr";
        CustomLog.d("mVoicePath is : " + mVoiceFilePath);
        try {
            mRecorder = new RecorderWorker();
            mRecorder.setMaxDuration(RECORDER_TIME_AT_MOST * 1000);
            mRecorder.start(mVoiceFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecorder(boolean mannual) {
        CustomLog.d("stop recorder. time is : " + System.currentTimeMillis());
        if (mannual && mRecorder != null)
            mRecorder.stop();
        CustomLog.d("getFileDuration. duration is : " + getFileDuration());
        if (getFileDuration() == 0 && !isIdleState()) {
            updateStatus(State.IDLE);
        }
    }

    private void releaseRecorder() {
        if (mRecorder != null) {
            mRecorder.releaseRecorder();
            mRecorder = null;
        }
    }

    private void createPlayer() {
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setOnPreparedListener(mp -> {
                // 装载完毕回调
                mPlayer.start();
            });
            mPlayer.setOnCompletionListener(mp -> {
                if (layout_rerecorder.getVisibility() == View.GONE) {
                    updateStatus(State.IDLE);
                } else {
                    updateStatus(State.PLAY_DONE);
                }
            });
            mPlayer.setOnErrorListener((mp, what, extra) -> {
                updateStatus(State.IDLE);
                releasePlayer();
                return false;
            });
            mPlayer.setOnSeekCompleteListener(mp -> mPlayer.start());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void resetPlayer() {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.reset();
        }
    }

    public void playCommand(String path) {
        if (mPlayer == null) {
            createPlayer();
        }
        try {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.reset();
            mPlayer.setDataSource(path);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // 通过异步的方式装载媒体资源
            mPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void stopPlaying() {
        if (layout_rerecorder.getVisibility() == View.GONE) {
            if (isPlaying()) {
                resetPlayer();
                updateStatus(State.IDLE);
            }
        } else {
            if (isPlaying()) {
                resetPlayer();
                updateStatus(State.PLAY_DONE);
            }
        }
    }

    private void doUploadVoiceFile() {
        showAnimLoading();
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(PWUploader.K_UPLOAD_TYPE, PWUploader.UPLOAD_TYPE_VOICE));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_QINIU_TOKEN, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("upload, onReceive. data is : " + data);
                String voice_url = data.optString("url");
                String token = data.optString("token");
                String key = data.optString("key");

                PWUploader uploader = PWUploader.getInstance();
                uploader.add(mVoiceFilePath, key, token, new UploadCallback() {

                    @Override
                    public void onComplete(String key, ResponseInfo responseInfo, JSONObject jsonObject) {
                        Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                            handleVoice(key, voice_url);
                            dismissAnimLoading();
                        });
                    }

                    @Override
                    public void onFailure(String key, ResponseInfo responseInfo, JSONObject jsonObject) {
                        Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                            dismissAnimLoading();
                        });
                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("onError. ret is : " + ret);
            }

        });
    }

    private void handleVoice(String key, String voice_url) {
        final File file = new File(mVoiceFilePath);
        int length = getFileDuration();
        if (file.length() == 0 || length == 0) {
            updateStatus(State.IDLE);
        }
        //send focus message through TCP.
        JSONObject jobj = new JSONObject();
        int msg_from = 1;
        final int uid = getIntent().getIntExtra("uid", 0);
        String md5str = "";
        try {
            md5str = Md5Util.md5Hex(file);
            jobj.put("from", msg_from);
            jobj.put("voice_url", voice_url);
            jobj.put("key", key);
            jobj.put("md5_code", md5str);
            jobj.put("length", length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final boolean needFocus = getIntent().getBooleanExtra(NEED_FOCUS_USER, false);
        if (needFocus) {
            TcpProxy.getInstance().focusUser(uid, jobj);
        }

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("filename", mVoiceFileName));
        params.add(new BasicNameValuePair("voice_url", voice_url));
        params.add(new BasicNameValuePair("length", String.valueOf(length)));
        params.add(new BasicNameValuePair("md5_code", md5str));
        ApiRequestWrapper.openAPIPOST(LazyGuyActivity.this, params, AsynHttpClient.API_USERINFO_LAZY_VOICE, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("lazy onReceive. data is : " + data);
                Resources res = getResources();
                String toast_str = res.getString(R.string.upload_lazy_voice_success);
                if (needFocus) {
                    toast_str = res.getString(R.string.apply_success);
                }
                showToast(LazyGuyActivity.this, toast_str);
                Intent intent = getIntent().putExtra("uid", uid);
                setResult(RESULT_OK, intent);
                LazyGuyActivity.this.finish();
                deleteRecorderFile(file);
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("lazy onError. ret is : " + ret);
                showToast(LazyGuyActivity.this, getResources().getString(R.string.upload_lazy_voice_failed));
                deleteRecorderFile(file);
            }
        });
    }

    private int getFileDuration() {
        MediaPlayer player = new MediaPlayer();
        try {
            player.reset();
            player.setDataSource(mVoiceFilePath);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //四舍五入
        int duration = (int) Math.rint(player.getDuration() / 1000.0);
        if (duration > RECORDER_TIME_AT_MOST || duration == 0) {
            showToast(this, getResources().getString(R.string.recorder_file_error));
            return 0;
        }
        return duration;
    }

    private void playRecorderVoice() {
        playLocalVoice(mVoiceFilePath);
    }

    private void playLastLazyVoice() {
        preparePlayVoice(server_lazy_voice_url);
    }

    private void preparePlayVoice(String voice_url) {
        File voiceFile = new File(FileManager.getVoicePath(), Md5Util.getMd5code(voice_url));
        PWDownloader downloader = PWDownloader.getInstance();
        downloader.add(voice_url, voiceFile, new DownloadCallback() {
            @Override
            public void onComplete(String path) {
                playLocalVoice(voiceFile.getAbsolutePath());
            }

            @Override
            public void onFailure(String path, IOException e) {
            }
        });
    }

    private void playLocalVoice(String file_path) {
        playCommand(file_path);
    }

    public void deleteRecorderFile(File file) {
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    private void setState(State state) {
        mState = state;
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        try {
            if (mr != null) {
                mr.reset();
                mr.release();
            }
        } catch (Exception e) {
            CustomLog.e("error is : " + e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseRecorder();
        releasePlayer();
        if (mVoiceFilePath != null && mVoiceFilePath.contains(".amr")) {
            deleteRecorderFile(new File(mVoiceFilePath));
        }
    }

    private boolean isIdleState() {
        return mState == State.IDLE;
    }

    private boolean isSpeakingState() {
        return mState == State.SPEAKING;
    }

    private boolean isRecordDone() {
        return mState == State.RECORDER_DONE;
    }

    private boolean isPlayDone() {
        return mState == State.PLAY_DONE;
    }

    private boolean isPlaying() {
        return mState == State.PLAYING;
    }
}
