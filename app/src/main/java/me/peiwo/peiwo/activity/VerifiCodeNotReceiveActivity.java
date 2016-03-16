package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import butterknife.OnClick;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.CustomLog;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.util.ArrayList;

public class VerifiCodeNotReceiveActivity extends BaseActivity {
    public static final int CAPTCHA_TYPE_REGISTER = 1;
    public static final int CAPTCHA_TYPE_FORGET_PASSWORD = 2;
    public static final int CAPTCHA_TYPE_BIND_PHONE = 3;
    public static final int CAPTCHA_TYPE_RESET_PHONE = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifi_code_not_receive);
    }

    @OnClick(R.id.tv_call_kefu)
    void callKeFu() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:400-6869-520"));
        startActivity(intent);
    }

    @OnClick(R.id.tv_get_voice_captcha)
    void getVoiceCaptcha() {
        showAnimLoading();
        String phoneNum = getIntent().getStringExtra("phone");
        ArrayList<NameValuePair> param = new ArrayList<>();
        param.add(new BasicNameValuePair("phone", phoneNum));
        param.add(new BasicNameValuePair("captcha_type", String.valueOf(CAPTCHA_TYPE_REGISTER)));
        ApiRequestWrapper.openAPIGET(this, param, AsynHttpClient.API_GET_VOICE_CAPTCHA, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("getVoiceCaptcha() data is : " + data);
                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(jsonObject -> {
                    dismissAnimLoading();
                    showToast(VerifiCodeNotReceiveActivity.this, getString(R.string.voice_captcha_had_been_sent));
                    finish();
                });
            }

            @Override
            public void onError(int error, Object ret) {
                Observable.just(ret).observeOn(AndroidSchedulers.mainThread()).subscribe(jsonObject -> {
                    dismissAnimLoading();
                    String tips = "failed " + error;
                    if (ret instanceof JSONObject) {
                        tips = ((JSONObject) ret).optString("msg");
                    }
                    showToast(VerifiCodeNotReceiveActivity.this, tips);
                });
            }
        });
    }
}
