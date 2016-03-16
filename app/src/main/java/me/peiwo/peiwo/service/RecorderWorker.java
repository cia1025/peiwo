package me.peiwo.peiwo.service;

import android.media.MediaRecorder;
import android.view.View;
import me.peiwo.peiwo.util.CustomLog;

import java.io.IOException;

public class RecorderWorker{
    private MediaRecorder mMediaRecorder;
    private int max_duration;

    public RecorderWorker() {
        createRecorder();
    }

    private void createRecorder() {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setOnErrorListener((mr, what, extra) -> {
            releaseRecorder();
        });
    }

    public void start(String filepath) {
        if (mMediaRecorder == null) {
            createRecorder();
        }
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            if (max_duration > 0)
                mMediaRecorder.setMaxDuration(max_duration);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setOutputFile(filepath);
            mMediaRecorder.setPreviewDisplay(null);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            mMediaRecorder.stop();
        } catch (Exception e) {

        }
    }

    public void setMaxDuration(int duration) {
        max_duration = duration;
    }

    public void releaseRecorder() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.release();
            } catch (IllegalStateException e) {
                CustomLog.e("error is : " + e);
            }
            mMediaRecorder = null;
        }
    }
}
