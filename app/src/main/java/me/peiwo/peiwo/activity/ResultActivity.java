package me.peiwo.peiwo.activity;

import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.PWActionConfig;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by fuhaidong on 14-10-14.
 * 点击进这个activity，分发intent
 */
public class ResultActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent result = getIntent();
        if (result != null) {
            if (PWActionConfig.ACTION_MAIN.equals(result.getAction())) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            } else if (PWActionConfig.ACTION_MAIN_WILD.equals(result.getAction())) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("action", Constans.ACTION_FLAG_WILDCAT);
                startActivity(intent);
            } else if (PWActionConfig.ACTION_NOTICATION.equals(result.getAction())) {
                //islogin 无需判断，已经在xgreceiver中判断了，只要有通知一定是已经登录了
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("action", Constans.ACTION_FLAG_MESSAGE);
                intent.setAction(PWActionConfig.ACTION_NOTICATION);
                startActivity(intent);
                //startService(new Intent(this, CoreService.class));
                sendBroadcast(new Intent(PWActionConfig.ACTION_FINISH_ALL));
            }
//            else if (ReceiverActionConfig.ACTION_REALLCALL.equals(result.getAction())) {
//                Intent intent = new Intent(this, RealCallActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                startActivity(intent);
//            } else if (ReceiverActionConfig.ACTION_WILDCAT.equals(result.getAction())) {
//                Intent intent = new Intent(this, WildCatCallActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                startActivity(intent);
//            }
        }

        finish();
    }

}