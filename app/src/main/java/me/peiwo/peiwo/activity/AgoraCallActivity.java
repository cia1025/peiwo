package me.peiwo.peiwo.activity;

import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by wallace on 16/3/7.
 */
public class AgoraCallActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void toast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }
}
