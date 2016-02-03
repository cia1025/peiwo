package me.peiwo.peiwo.service;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;
import me.peiwo.peiwo.BuildConfig;

import java.io.IOException;
import java.lang.ref.SoftReference;

public class PlayerService {


    private boolean auto_stop = false;
    private MediaPlayer mPlayer;

    private SoftReference<MediaPlayer.OnCompletionListener> playCompletionRef;
    private SoftReference<MediaPlayer.OnErrorListener> playErrorRef;

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private onPreparedCompleteListener mOnPrepareCompleteListener;

    private PlayerService() {
        createPlayer();
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener playCompletionListener) {
        releasePlayCompletionRef();
        playCompletionRef = new SoftReference<>(playCompletionListener);
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener playErrorListener) {
        releasePlayErrorRefRef();
        playErrorRef = new SoftReference<>(playErrorListener);
    }

    public void setOnPrepareCompletedListener(onPreparedCompleteListener listener) {
        mOnPrepareCompleteListener = listener;
    }

    private static class SingletonHolder {
        public static final PlayerService INSTANCE = new PlayerService();
    }

    public static PlayerService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void playPauseCommand() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    public void playResumeCommand() {
        if (mPlayer != null && !mPlayer.isPlaying()) {
            int position = mPlayer.getCurrentPosition();
            mPlayer.seekTo(position);
        }
    }

    public void playAssetFileCommand(AssetFileDescriptor assetFileDescriptor, boolean loop) {
        if (DEBUG)
            Log.i("player", assetFileDescriptor.toString());
        playMusic(assetFileDescriptor, null, loop);
    }

    public void playCommand(String path, boolean loop) {
        if (DEBUG)
            Log.i("player", path);
        if (!TextUtils.isEmpty(path)) {
            playMusic(null, path, loop);
        }
    }

    public void resetPlayerCommand() {
        if (DEBUG)
            Log.i("player", "command resetPlayer");
        resetPlayer();
    }

    public void releasePlayerCommand() {
        if (DEBUG)
            Log.i("player", "command release");
        if (mPlayer != null && mPlayer.isPlaying() && !mPlayer.isLooping()) {
            stopOnPlayComplete();
        } else {
            releasePlayer();
        }
    }

    public boolean isPlaying() {
        if(mPlayer == null)
            return false;
        return mPlayer.isPlaying();
    }

    public void releaseIgnoreCaseCommand() {
        releasePlayer();
    }


    private void stopOnPlayComplete() {
        auto_stop = true;
    }


    private void resetPlayer() {
        if (DEBUG)
            Log.i("player", "reset");
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.reset();
        }
    }

    private void releasePlayer() {
        if (DEBUG)
            Log.i("player", "release");
        releasePlayCompletionRef();
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void releasePlayCompletionRef() {
        if (playCompletionRef != null) {
            playCompletionRef.clear();
            playCompletionRef = null;
        }
    }

    private void releasePlayErrorRefRef() {
        if (playErrorRef != null) {
            playErrorRef.clear();
            playErrorRef = null;
        }
    }

    public AssetFileDescriptor getMusicAssetPath(Context context, String path) {
        try {
            return context.getAssets().openFd(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void playMusic(AssetFileDescriptor assetFileDescriptor, String path, boolean loop) {
        if (assetFileDescriptor == null && TextUtils.isEmpty(path)) {
            return;
        }
        if (mPlayer == null) {
            createPlayer();
        }
        try {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.reset();
            if (assetFileDescriptor != null) {
                mPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                assetFileDescriptor.close();
            } else {
                mPlayer.setDataSource(path);
            }
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setLooping(loop);
            // 通过异步的方式装载媒体资源
            mPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createPlayer() {
        try {
            mPlayer = new MediaPlayer();
            //mPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            //assetFileDescriptor.close();
            //mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // 通过异步的方式装载媒体资源
            //mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(mp -> {
                // 装载完毕回调
                mPlayer.start();
                if(mOnPrepareCompleteListener != null) {
                    mOnPrepareCompleteListener.onPreparedComplete();
                }
            });
            mPlayer.setOnCompletionListener(mp -> {
                //releasePlayer();
                notifyPlayCompletion();
                if (auto_stop) {
                    releasePlayer();
                }
            });
            mPlayer.setOnErrorListener((mp, what, extra) -> {
                releasePlayer();
                notifyPlayError(mp, what, extra);
                return false;
            });
            mPlayer.setOnSeekCompleteListener(mp -> mPlayer.start());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyPlayCompletion() {
        if (DEBUG)
            Log.i("tab", "notifyPlayCompletion");
        if (playCompletionRef != null) {
            MediaPlayer.OnCompletionListener listener = playCompletionRef.get();
            if (listener != null) {
                listener.onCompletion(mPlayer);
                if (DEBUG)
                    Log.i("tab", "playCompletionRef Completion");
            }
        }
    }


    private void notifyPlayError(MediaPlayer mp, int what, int extra) {
        if (playErrorRef != null) {
            MediaPlayer.OnErrorListener listener = playErrorRef.get();
            if (listener != null) {
                listener.onError(mPlayer, what, extra);
            }
        }
    }

    public interface onPreparedCompleteListener {
        void onPreparedComplete();
    }

}
