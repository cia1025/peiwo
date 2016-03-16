package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import butterknife.OnClick;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.util.HourGlassAgent;

public class LogOnProblemActivity extends BaseActivity {
    private static final int REQUEST_CODE_FORGETPWD = 5001;
    public static final int SOCIAL_TYPE_PHONE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_on_problem);
    }

    @OnClick(R.id.tv_call_kefu)
    void callKeFu() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:400-6869-520"));
        startActivity(intent);
    }

    @OnClick(R.id.btn_forget_password)
    void forgetPassword() {
        startActivityForResult(new Intent(this, FillPhonenoActivity.class), REQUEST_CODE_FORGETPWD);
        HourGlassAgent hourGlassAgent = HourGlassAgent.getInstance();
        if (hourGlassAgent.getStatistics() && hourGlassAgent.getK34() == 0) {
            hourGlassAgent.setK34(1);
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.postK("k34");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FORGETPWD:
                    Intent result = new Intent();
                    String phoneno = data.getStringExtra(Constans.SP_KEY_OPENID);
                    String pwd = data.getStringExtra(Constans.SP_KEY_OPENTOKEN);
                    String pcode = data.getStringExtra(Constans.SP_KEY_PCODE);
                    result.putExtra(Constans.SP_KEY_OPENID, phoneno);
                    result.putExtra(Constans.SP_KEY_OPENTOKEN, pwd);
                    result.putExtra(Constans.SP_KEY_PCODE, pcode);
                    result.putExtra(Constans.SP_KEY_SOCIALTYPE, WelcomeActivity.SOCIAL_TYPE_PHONE);
                    setResult(RESULT_OK, result);
                    finish();
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
