package me.peiwo.peiwo.fragment;

import java.io.File;
import java.io.IOException;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.AgoraWildCallActivity;
import me.peiwo.peiwo.activity.WildCatCallActivity;
import me.peiwo.peiwo.util.FileManager;
import me.peiwo.peiwo.widget.RoundProgressBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * Created by chenhao on 2014-10-21 下午3:42.
 *
 * @modify:
 */
public class RecordingFragment extends PPBaseFragment {
    public static final int WHAT_RECORD_COMPLETE = 1000;
    public static final int WHAT_PLAY_COMPLETE = 1001;

    public static RecordingFragment newInstance() {
        return new RecordingFragment();
    }

    private TextView tv_record;
    private Button btn_start_record;
    private RoundProgressBar pb_record;
    private int progress = 0;
    private MyHanlder mHandler;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private File audioFile = new File(FileManager.getTempFilePath(), "audio_file.amr");
    private int tryCount = 0;

    private class MyHanlder extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case WHAT_RECORD_COMPLETE:
                    doRecordComplete();
                    break;
                case WHAT_PLAY_COMPLETE:
                    doPlayComplete();
                    break;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recording, null);
        mHandler = new MyHanlder();
        mRecorder = new MediaRecorder();
        mPlayer = new MediaPlayer();
        setAudioVolume();
        initView(v);
        return v;
    }

    private void setAudioVolume() {
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                (int) (maxVolume * 0.8), 0);
    }

    private void initView(View v) {
        tv_record = (TextView) v.findViewById(R.id.tv_record);
        btn_start_record = (Button) v.findViewById(R.id.btn_start_record);
        pb_record = (RoundProgressBar) v.findViewById(R.id.pb_record);
        btn_start_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_start_record.setVisibility(View.GONE);
                pb_record.setVisibility(View.VISIBLE);
                startRcord();
            }
        });

    }

    private void startRcord() {
        mRecorder.reset();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // 设置MediaRecorder的音频源为麦克风
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        // 设置MediaRecorder录制的音频格式
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(audioFile.getAbsolutePath());
        try {
            mRecorder.prepare();
            mRecorder.start();
            drwaProgress(true);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void stopRecord() {
        if (mRecorder != null) {
            try {
                mRecorder.stop();
            } catch (Exception e) {
                return;
            }
        }
    }

    private void initPlay() {
        try {
            mPlayer.reset();
            mPlayer.setDataSource(audioFile.getAbsolutePath());
            mPlayer.prepare();
            mPlayer.start();
            drwaProgress(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlay() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
        }
    }


    private void drwaProgress(final boolean isRecord) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (progress < 100) {
                    progress += 1;
                    pb_record.setProgress(progress);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(isRecord ? WHAT_RECORD_COMPLETE : WHAT_PLAY_COMPLETE);  //进度画完了，现在在子线程，更新界面
                }
            }
        }).start();
    }

    private void doRecordComplete() {
//        ((SoundRecordActivity) getActivity()).setPage(2);
        stopRecord();
        tv_record.setText("录音完毕,正在播放刚才录的音");
        progress = 0;
        initPlay();
    }


    private void doPlayComplete() {
        stopPlay();
        showDialog();
    }

    private void showDialog() {
        if (tryCount < 2) {
            new AlertDialog.Builder(getActivity()).setMessage("是否能听见刚刚的录音？").setNegativeButton("不能，重试", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    tv_record.setText("正在录音...");
                    progress = 0;
                    tryCount++;
                    startRcord();
                }
            }).setPositiveButton("能，去限时聊", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getActivity(), AgoraWildCallActivity.class);
                    intent.putExtra(WildCatCallActivity.START_MAIN, 1);
                    startActivity(intent);
                    getActivity().finish();
                }
            }).create().show();
        } else {
            new AlertDialog.Builder(getActivity()).setMessage("一直无声音?请加反馈qq群：224153630").setPositiveButton("确定", null).create().show();
        }
    }

    @Override
    public void onDetach() {
        progress = 100;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected String getPageName() {
        return "通话调试录音";
    }
}
