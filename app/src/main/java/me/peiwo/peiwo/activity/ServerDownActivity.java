package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.service.CoreService;

public class ServerDownActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_down);
        Intent intent = getIntent();
        showUI(intent.getStringExtra("data"));
    }

    @Override
    public void finish() {
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        app.dissServerDownShowed();
        //Log.i("server", "dissServerDownShowed");
        super.finish();
    }

    private void showUI(String data) {
        findViewById(R.id.btn_ok).setOnClickListener(v -> {
            stopService(new Intent(this, CoreService.class));
            finish();
        });

        //                {
//                    current_time: 1457497665.151139,
//                            code: -1,
//                        data: {content:"陪我系统正在升级，预计升级1小时。", hotline:"400-686-9520", extra:"或私信官方微博：陪我APP获取陪我最新动态。"}
//                }
        //JSONObject object = new JSONObject(s);
        //show ui
    }

}
