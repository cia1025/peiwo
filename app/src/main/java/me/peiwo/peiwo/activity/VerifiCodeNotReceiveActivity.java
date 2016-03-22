package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import butterknife.OnClick;
import me.peiwo.peiwo.R;

public class VerifiCodeNotReceiveActivity extends BaseActivity {

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
}
