package me.peiwo.peiwo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import com.jakewharton.rxbinding.view.RxView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.presenter.AgoraCallInPresenter;
import rx.android.schedulers.AndroidSchedulers;

import java.util.concurrent.TimeUnit;

public class AgoraCallInActivity extends AgoraCallActivity {
    public static final String K_CALLED_EVENT = "called";

    private AgoraCallInPresenter presenter;


    @Bind(R.id.btn_answer)
    Button btn_answer;
    @Bind(R.id.btn_reject)
    Button btn_reject;
    @Bind(R.id.btn_hungup)
    Button btn_hungup;
    @Bind(R.id.tv_caller)
    TextView tv_caller;
    @Bind(R.id.tv_callee)
    TextView tv_callee;
    @Bind(R.id.btn_audio_mode)
    Button btn_audio_mode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agora_call_in);
        presenter = new AgoraCallInPresenter(this);
        init();
        presenter.init();
    }


    private void init() {
        setUpView();
        RxView.clicks(btn_answer).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> {
            answerCall();
        });
        RxView.clicks(btn_reject).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> {
            rejectCall();
        });
        RxView.clicks(btn_hungup).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> {
            presenter.hungUp(true);
        });
        RxView.clicks(btn_audio_mode).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> {
            Object tag = btn_audio_mode.getTag();
            presenter.handleAudioMode(tag);
        });
    }


    private void setUpView() {
        btn_answer.setVisibility(View.VISIBLE);
        btn_reject.setVisibility(View.VISIBLE);
        btn_hungup.setVisibility(View.GONE);
        btn_audio_mode.setText("现在是扬声器模式");
        btn_audio_mode.setEnabled(false);
    }

    private void rejectCall() {
        presenter.rejectCall();
    }

    private void answerCall() {
        presenter.answerCall();
    }


    @Override
    public void left_click(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        presenter.hungUp(true);
        //super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestory();
        super.onDestroy();
    }

    public void setCallerText(String text) {
        tv_caller.setText(text);
    }

    public void setCalleeText(String text) {
        tv_callee.setText(text);
    }

    public void setHungUpViewEnable(boolean enable) {
        btn_hungup.setEnabled(enable);
    }


    public void setAudioTag(Object o) {
        btn_audio_mode.setTag(o);
    }

    public void setAudioModeText(String text) {
        btn_audio_mode.setText(text);
    }

    public void changeViewWithAnswer() {
        btn_reject.setVisibility(View.GONE);
        btn_answer.setVisibility(View.GONE);
        btn_hungup.setVisibility(View.VISIBLE);
    }

    public void setAudioViewEnable(boolean enable) {
        btn_audio_mode.setEnabled(enable);
    }
}
