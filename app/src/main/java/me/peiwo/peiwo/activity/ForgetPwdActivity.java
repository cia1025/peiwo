package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.TitleUtil;
import me.peiwo.peiwo.util.UserManager;
import org.json.JSONObject;

/**
 * Created by fuhaidong on 14-9-25.
 * 找回密码第二步
 */
public class ForgetPwdActivity extends BaseActivity {

    private static final int WHAT_DATA_RECEIVE_RESET_PHONENO = 1000;
    private static final int WHAT_DATA_RECEIVE_SIGNUP_ERROR = 1001;
    private EditText et_pwd;
    private String mPhoneNo;
    private String mPhoneCode;
    private String verifcode;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgetpwd);

        init();
    }

    private void init() {
        Intent data = getIntent();
        mPhoneNo = data.getStringExtra(FillPhonenoActivity.KEY_PHONENO);
        mPhoneCode = data.getStringExtra(FillPhonenoActivity.KEY_PCODE);
        verifcode = data.getStringExtra(FillPhonenoActivity.KEY_VERFI_CODE);
        setTitleBar();
        et_pwd = (EditText) findViewById(R.id.et_pwd);
    }

    private void setTitleBar() {
        TitleUtil.setTitleBar(this, "找回密码", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        }, null);
    }

    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_submit:
                if (check()) {
                    doNextStep();
                }
                break;
        }
    }

    private void doNextStep() {
//        mpost.set(false);
        showAnimLoading("", false, false, false);
        //resetPhoneNo ? CAPTCHA_TYPE_RESETPWD : CAPTCHA_TYPE_REGISTER
        String phoneNumber = PWUtils.getFormatPhoneNo(mPhoneCode, mPhoneNo);
        ApiRequestWrapper.forgetPhone(this, phoneNumber, verifcode, et_pwd.getText().toString(), new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                PWUserModel modle = new PWUserModel(data);
                if (UserManager.saveUser(ForgetPwdActivity.this, modle)) {
                    //mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_RESET_PHONENO);
                    distributeMessage(WHAT_DATA_RECEIVE_RESET_PHONENO, null);
                } else {
                    //mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_SIGNUP_ERROR);
                    distributeMessage(WHAT_DATA_RECEIVE_SIGNUP_ERROR, null);
                }
            }

            @Override
            public void onError(int error, Object ret) {
                //mHandler.sendEmptyMessage(WHAT_DATA_RECEIVE_SIGNUP_ERROR);
                distributeMessage(WHAT_DATA_RECEIVE_SIGNUP_ERROR, null);
            }
        });

    }

    @Override
    protected void handle_message(int message_id, JSONObject obj) {
        dismissAnimLoading();
        switch (message_id) {
            case WHAT_DATA_RECEIVE_RESET_PHONENO:
                saveRegistInfo();
                Intent result = new Intent();
                result.putExtra(Constans.SP_KEY_OPENID, mPhoneNo);
                result.putExtra(Constans.SP_KEY_OPENTOKEN, et_pwd.getText().toString());
                result.putExtra(Constans.SP_KEY_SOCIALTYPE, WelcomeActivity.SOCIAL_TYPE_PHONE);
                setResult(RESULT_OK, result);
                finish();
                break;
            case WHAT_DATA_RECEIVE_SIGNUP_ERROR:
                showToast(this, "请检查验证码是否正确");
                break;
        }
    }

    private void saveRegistInfo() {
        UserManager.saveOpenResultInPreference(this, mPhoneNo, et_pwd.getText().toString(), WelcomeActivity.SOCIAL_TYPE_PHONE);
    }


    private boolean check() {
        if (et_pwd.getText().length() < 6) {
            showToast(this, et_pwd.getHint().toString());
            return false;
        }
        return true;
    }


}