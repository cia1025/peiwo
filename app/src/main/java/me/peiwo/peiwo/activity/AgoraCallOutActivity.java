package me.peiwo.peiwo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import com.jakewharton.rxbinding.view.RxView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.presenter.AgoraCallOutPresenter;
import rx.android.schedulers.AndroidSchedulers;

import java.util.concurrent.TimeUnit;

public class AgoraCallOutActivity extends AgoraCallActivity {
    public static final String K_CALLEE_ID = "callee_id";
    public static final String K_CHANNEL = "channel";
    public static final String K_CHANNEL_ID = "channel_id";
    public static final String K_PRICE = "price";

    private AgoraCallOutPresenter presenter;

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
        setContentView(R.layout.activity_call_out);
        presenter = new AgoraCallOutPresenter(this);
        init();
        presenter.init();
    }

    private void init() {
        setUpInitView();
    }

    private void setUpInitView() {
        setAudioModeText("现在是扬声器模式");
        setAudioViewEnable(false);
        RxView.clicks(btn_hungup).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> {
            presenter.hungUp(true);
        });
        RxView.clicks(btn_audio_mode).throttleFirst(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> {
            Object tag = btn_audio_mode.getTag();
            presenter.handleAudioMode(tag);
        });
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

    public void setAudioModeText(String text) {
        btn_audio_mode.setText(text);
    }

    public void setAudioViewEnable(boolean enable) {
        btn_audio_mode.setEnabled(enable);
    }

    public void setHungUpViewEnable(boolean enable) {
        btn_hungup.setEnabled(enable);
    }


    public void setAudioTag(Object o) {
        btn_audio_mode.setTag(o);
    }
}
