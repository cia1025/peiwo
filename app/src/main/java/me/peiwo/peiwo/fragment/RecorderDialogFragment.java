package me.peiwo.peiwo.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.qiniu.android.http.ResponseInfo;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.BaseActivity;
import me.peiwo.peiwo.activity.LazyGuyActivity;
import me.peiwo.peiwo.callback.UploadCallback;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.FocusEvent;
import me.peiwo.peiwo.net.*;
import me.peiwo.peiwo.service.RecorderWorker;
import me.peiwo.peiwo.util.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by gaoxiang on 2015/10/9.
 */
public class RecorderDialogFragment extends DialogFragment implements View.OnTouchListener, View.OnClickListener, MediaRecorder.OnErrorListener {
    private static final int REQUEST_UPLOAD_AND_FOCUS = 0x10;
    private ImageView mic_btn;
    private Chronometer chronometer;
    private TextView tv_guide_text;
    private TextView tv_guide_state;
    private ImageView iv_user_avatar;
    private TextView tv_cc_lazy_voice;

    private String mVoiceFilePath = "";
    private ImageView iv_cancel;
    private ImageView iv_send;
    private TextView tv_cancel;
    private TextView tv_send;
    private State mState;
    private final int RECORDER_TIME_AT_LEAST = 2;
    private final int RECORDER_TIME_AT_MOST = 30;
    private UploadListener mUploadListener;
    private RecorderWorker mRecorder;

    private enum State {
        IDLE, SPEAKING, FREEHANDS, SHORTTAP
    }

    public static RecorderDialogFragment newInstance(int uid, String name, String avatar_url, int msgFrom) {
        RecorderDialogFragment fragment = new RecorderDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("msg_from", msgFrom);
        bundle.putInt("uid", uid);
        bundle.putString("name", name);
        bundle.putString("avatar_url", avatar_url);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.layout_recorder_dialog, null);
        iv_user_avatar = (ImageView) v.findViewById(R.id.iv_user_avatar);
        iv_cancel = (ImageView) v.findViewById(R.id.iv_cancel);
        iv_send = (ImageView) v.findViewById(R.id.iv_send);
        mic_btn = (ImageView) v.findViewById(R.id.iv_mic_btn);
        tv_cc_lazy_voice = (TextView) v.findViewById(R.id.tv_cc_lazy_voice);
        chronometer = (Chronometer) v.findViewById(R.id.chronometer);
        tv_guide_text = (TextView) v.findViewById(R.id.tv_guide_text);
        tv_guide_state = (TextView) v.findViewById(R.id.tv_guide_state);
        tv_send = (TextView) v.findViewById(R.id.tv_send);
        tv_cancel = (TextView) v.findViewById(R.id.tv_cancel);
        tv_send.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
        tv_cc_lazy_voice.setOnClickListener(this);
        iv_cancel.setOnTouchListener(this);
        iv_send.setOnTouchListener(this);
        mic_btn.setOnTouchListener(this);
        fillData();
        updateStatus(State.IDLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        EventBus.getDefault().register(this);
        return builder.create();
    }

    private void startRecorder() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return;
        }
        String mVoiceFileName = String.valueOf(System.currentTimeMillis());
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
        mVoiceFilePath = voiceFolder + "/" + mVoiceFileName + ".amr";
        CustomLog.d("mVoicePath is : " + mVoiceFilePath);
        mRecorder = new RecorderWorker();
        mRecorder.setMaxDuration(RECORDER_TIME_AT_MOST * 1000);
        mRecorder.start(mVoiceFilePath);
    }

    private void setState(State state) {
        mState = state;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int viewId = v.getId();
        int[] cancel_location = new int[2];
        iv_cancel.getLocationInWindow(cancel_location);
        int btn_cancel_Y = cancel_location[1];
        int btn_cancel_X = cancel_location[0];
        int[] send_location = new int[2];
        iv_send.getLocationInWindow(send_location);
        int btn_send_Y = send_location[1];
        int btn_send_X = send_location[0];
        float currentRawX = event.getRawX();
        iv_send.setImageResource(R.drawable.icon_luyin);
        iv_cancel.setImageResource(R.drawable.icon_delete);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                CustomLog.d("onTouch ACTION_DOWN, mState is : " + mState);
                if (!isSpeaking() && viewId == R.id.iv_mic_btn) {
                    updateStatus(State.SPEAKING);
                    startRecorder();
                } else {
                    int result = getActivity().getPackageManager().checkPermission("android.permission.RECORD_AUDIO", Constans.APP_PACKEGE_NAME);
                    CustomLog.d("recorder audio permission is : " + result);
                    Toast.makeText(getActivity(), getString(R.string.recorder_authority_access_error), Toast.LENGTH_LONG).show();
                    mCountDownTimer.cancel();
                    dismiss();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //用户挪到到cancel或button上时，图标变大
                if (currentRawX <= btn_cancel_X + iv_cancel.getWidth()) {
                    iv_cancel.setImageResource(R.drawable.icon_delete_press);
                    tv_guide_state.setText(getString(R.string.release_to_cancel));
                } else if (currentRawX >= btn_send_X) {
                    iv_send.setImageResource(R.drawable.icon_luyin_press);
                    tv_guide_state.setText(getString(R.string.release_to_hold));
                } else {
                    tv_guide_state.setText(getString(R.string.release_to_send));
                }
                break;
            case MotionEvent.ACTION_UP:
                if (/*currentRawX >= btn_cancel_X
                        && */currentRawX <= btn_cancel_X + iv_cancel.getWidth()) {
                    stopRecorder(true);
                    mCountDownTimer.cancel();
                    updateStatus(State.IDLE);
                    deleteRecorderFile(new File(mVoiceFilePath));
                    return false;
                } else if (currentRawX >= btn_send_X
                        /*&& currentRawX <= btn_send_X + iv_send.getWidth()*/) {
                    updateStatus(State.FREEHANDS);
                    return false;
                }
                if (viewId == R.id.iv_mic_btn) {
                    stopRecorder(true);
                    int duration = getFileDuration();
                    CustomLog.d("ACTION_UP duration is : "+duration);
                    if (duration < RECORDER_TIME_AT_LEAST) {
                        updateStatus(State.SHORTTAP);
                        deleteRecorderFile(new File(mVoiceFilePath));
                    } else {
                        doUploadVoiceFile();
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    public void deleteRecorderFile(File file) {
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    private void updateStatus(State state) {
        setState(state);
        switch (mState) {
            case IDLE:
                mic_btn.setVisibility(View.VISIBLE);
                tv_guide_text.setTextColor(getResources().getColor(R.color.text_dim_color));
                tv_guide_text.setText(getString(R.string.tap_to_speak));
                tv_guide_text.setVisibility(View.VISIBLE);
                chronometer.setVisibility(View.GONE);
                tv_guide_state.setVisibility(View.INVISIBLE);
                tv_send.setVisibility(View.GONE);
                tv_cancel.setVisibility(View.GONE);
                iv_send.setVisibility(View.INVISIBLE);
                iv_cancel.setVisibility(View.INVISIBLE);
                tv_cc_lazy_voice.setVisibility(View.VISIBLE);
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.stop();
                mCountDownTimer.cancel();
                break;
            case SPEAKING:
                iv_send.setVisibility(View.VISIBLE);
                iv_cancel.setVisibility(View.VISIBLE);
                tv_guide_text.setVisibility(View.INVISIBLE);
                tv_guide_state.setVisibility(View.VISIBLE);
                chronometer.setVisibility(View.VISIBLE);
                tv_cc_lazy_voice.setVisibility(View.INVISIBLE);
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                mCountDownTimer.start();
                break;
            case FREEHANDS:
                mic_btn.setVisibility(View.INVISIBLE);
                tv_cc_lazy_voice.setVisibility(View.INVISIBLE);
                tv_cancel.setVisibility(View.VISIBLE);
                tv_send.setVisibility(View.VISIBLE);
                iv_send.setVisibility(View.GONE);
                iv_cancel.setVisibility(View.GONE);
                tv_guide_state.setText(getString(R.string.recording));
                break;
            case SHORTTAP:
                mic_btn.setVisibility(View.VISIBLE);
                tv_guide_text.setTextColor(getResources().getColor(R.color.voice_alert_pink_color));
                tv_guide_text.setText(getString(R.string.tap_is_too_short));
                tv_guide_text.setVisibility(View.VISIBLE);
                chronometer.setVisibility(View.GONE);
                tv_guide_state.setVisibility(View.INVISIBLE);
                tv_send.setVisibility(View.GONE);
                tv_cancel.setVisibility(View.GONE);
                iv_send.setVisibility(View.INVISIBLE);
                iv_cancel.setVisibility(View.INVISIBLE);
                tv_cc_lazy_voice.setVisibility(View.VISIBLE);
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.stop();
                mCountDownTimer.cancel();
                break;
            default:
                break;
        }
    }

    CountDownTimer mCountDownTimer = new CountDownTimer((RECORDER_TIME_AT_MOST) * 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            stopRecorder(false);
            doUploadVoiceFile();
        }
    };

    private void sendVoiceReqAndFocus(String voice_url, String key, String md5_code, int length) {
        //send focus message through TCP.
        JSONObject jobj = new JSONObject();
        int msg_from = getArguments().getInt("msg_from");
        int uid = getArguments().getInt("uid");
        try {
            jobj.put("from", msg_from);
            jobj.put("voice_url", voice_url);
            jobj.put("key", key);
            jobj.put("md5_code", md5_code);
            jobj.put("length", length);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        TcpProxy.getInstance().focusUser(uid, jobj);
    }

    private void doUploadVoiceFile() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }

        if(getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).showAnimLoading();
        }
        // for Qiniu upload.
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(PWUploader.K_UPLOAD_TYPE, PWUploader.UPLOAD_TYPE_VOICE));
        ApiRequestWrapper.openAPIGET(getActivity(), params, AsynHttpClient.API_QINIU_TOKEN, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("doUploadVoiceFile, onReceive. data is : " + data);
                String voice_url = data.optString("url");
                String token = data.optString("token");
                String key = data.optString("key");
                PWUploader uploader = PWUploader.getInstance();
                uploader.add(mVoiceFilePath, key, token, new UploadCallback() {
                    @Override
                    public void onComplete(String key, ResponseInfo responseInfo, JSONObject jsonObject) {
                        CustomLog.d("doUploadVoiceFile. onComplete key is : " + key);
                        Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                            if (getActivity() instanceof BaseActivity) {
                                ((BaseActivity) getActivity()).dismissAnimLoading();
                            }
                            handleVoice(key, voice_url);
                        });
                    }

                    @Override
                    public void onFailure(String key, ResponseInfo responseInfo, JSONObject jsonObject) {
                        CustomLog.d("doUploadVoiceFile. onFailure key is : " + key + ", response is : " + responseInfo + ", jsonobj is : " + jsonObject);
                        Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                            if (getActivity() instanceof BaseActivity) {
                                ((BaseActivity) getActivity()).dismissAnimLoading();
                                ((BaseActivity)getActivity()).showToast(getActivity(), getString(R.string.apply_failed));
                            }
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

    private int getFileDuration() {
        if(TextUtils.isEmpty(mVoiceFilePath))
            return -1;
        MediaPlayer player = new MediaPlayer();
        try {
            player.reset();
            player.setDataSource(mVoiceFilePath);
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //四舍五入
        int duration  = (int) Math.rint(player.getDuration() / 1000.0);
        //快速点击的适合返回的duration是个很大的常数, 做一下特殊处理
        if(duration > RECORDER_TIME_AT_MOST || duration == 0) {
            if(getActivity() instanceof BaseActivity) {
                ((BaseActivity) getActivity()).showToast(getActivity(), getResources().getString(R.string.recorder_file_error));
            }
            return 0;
        }
        return duration;
    }

    private void handleVoice(String key, String voice_url) {
        if (mUploadListener != null) {
            mUploadListener.onUploadDone();
        }
        int length = getFileDuration();
        if(length == 0) {
            updateStatus(State.IDLE);
            deleteRecorderFile(new File(mVoiceFilePath));
            return;

        }
        File file = new File(mVoiceFilePath);
        if(file.length() == 0) {
            updateStatus(State.IDLE);
            deleteRecorderFile(new File(mVoiceFilePath));
            return;
        }
        String md5_code = "";
        try {
            md5_code = Md5Util.md5Hex(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CustomLog.d("dialog fragment send lazy voice, length is : " + length);
        sendVoiceReqAndFocus(voice_url, key, md5_code, length);
        deleteRecorderFile(file);
    }

    private void stopRecorder(boolean byMannual) {
        CustomLog.d("stop recorder. current time is : " + System.currentTimeMillis());
        CustomLog.d("mState is : " + mState);
        if(byMannual && mRecorder != null)
            mRecorder.stop();

        if(getFileDuration() == 0) {
            updateStatus(State.IDLE);
        }
    }

    private void releaseRecorder() {
        if(mRecorder != null)
            mRecorder.releaseRecorder();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                CustomLog.d("click cancel button.");
                stopRecorder(true);
                updateStatus(State.IDLE);
                break;
            case R.id.tv_send:
                CustomLog.d("click send button.");
                stopRecorder(true);
                mCountDownTimer.cancel();
                if (getFileDuration() < RECORDER_TIME_AT_LEAST) {
                    updateStatus(State.SHORTTAP);
                } else {
                    doUploadVoiceFile();
                }
                break;
            case R.id.tv_cc_lazy_voice:
                sendCCLazyVoice();
                break;
            default:
                break;
        }
    }

    private boolean isIdleState() {
        return mState == State.IDLE;
    }

    private boolean isSpeaking() {
        return mState == State.SPEAKING;
    }

    private void sendCCLazyVoice() {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        ApiRequestWrapper.openAPIGET(getActivity(), params, AsynHttpClient.API_USERINFO_LAZY_VOICE, new MsgStructure() {

            @Override
            public void onReceive(JSONObject data) {
                if (mUploadListener != null) {
                    mUploadListener.onUploadDone();
                }
                CustomLog.d("sendCCLazyVoice, onReceive. data is :" + data);
                String voice_url = data.optString("voice_url");
                String md5_code = data.optString("md5_code");
                int length = data.optInt("length");
                CustomLog.d("sendCCLazyVoice file, length is : " + length);
                String key = voice_url.substring(voice_url.lastIndexOf("/") + 1);
                sendVoiceReqAndFocus(voice_url, key, md5_code, length);
            }

            @Override
            public void onError(int error, Object ret) {
                if (error == LazyGuyActivity.HAVE_NOT_UPLOAD_YET) {
                    Intent it = new Intent(getActivity(), LazyGuyActivity.class);
                    String name = getArguments().getString("name");
                    int uid = getArguments().getInt("uid");
                    String avatar_url = getArguments().getString("avatar_url");
                    it.putExtra("uid", uid);
                    it.putExtra("name", name);
                    it.putExtra("avatar_thumbnail", avatar_url);
                    it.putExtra(LazyGuyActivity.NEED_FOCUS_USER, true);
                    startActivityForResult(it, REQUEST_UPLOAD_AND_FOCUS);
                } else {
                    CustomLog.e("check lazy voice onError, error code is : " + error);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CustomLog.d("request code : " + requestCode + ", result code is : " + resultCode + ", isAdded() ? " + isAdded());
    }

    public void fillData() {
//        String name = getArguments().getString("name");
//        tv_user_name.setText(name);
        String avatar_url = getArguments().getString("avatar_url");
        ImageLoader.getInstance().displayImage(avatar_url, iv_user_avatar, ImageUtil.getRoundedOptionsWithRadius(PWUtils.getPXbyDP(getActivity(), 20) / 2));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releaseRecorder();
        setState(State.IDLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(getActivity());
        releaseVars();
    }

    private void releaseVars() {
        mic_btn = null;
        mRecorder = null;
        chronometer = null;
        tv_guide_text = null;
        tv_guide_state = null;
        iv_user_avatar = null;
        tv_cc_lazy_voice = null;
        mVoiceFilePath = null;
        tv_cancel = null;
        iv_send = null;
        tv_cancel = null;
        tv_send = null;
        mState = null;
    }

    public void onEventMainThread(FocusEvent event) {
        CustomLog.d("onEventMainThread. event.type is : " + event.type);
        int type = event.type;
        if (isAdded() && type == FocusEvent.FOCUS_SUCCESS_EVENT) {
            updateStatus(State.IDLE);
            String msg = getString(R.string.apply_success);
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        try {
            if (mr != null)
                mr.reset();
        } catch (Exception e) {
            CustomLog.e("error is : " + e);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        CustomLog.d("RecorderDialogFragment setUserVisibleHint. is visible : " + isVisibleToUser);
    }

    public void setOnUploadListener(UploadListener listener) {
        mUploadListener = listener;
    }

    public interface UploadListener {
        void onUploadDone();
    }
}
