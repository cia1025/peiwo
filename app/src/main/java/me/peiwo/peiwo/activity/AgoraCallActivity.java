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
<<<<<<< HEAD

    /**
     * 刷新金额
     */
    public void refreshRewardPrice() {

    }

    /**
     * 充值
     */
    public void charge() {

    }

    /**
     * 发送打赏
     *
     * @param transaction 事务
     */
    public void payReward(int transaction) {

    }

    /**
     * 礼尚往来
     */
    public void returnASalute() {

    }

    /**
     * 显示黑色背景
     */
    public void showRangeBlack() {

    }

    /**
     * 隐藏黑色背景
     */
    public void hideRangeBlack() {

    }

    /**
     * 能否拖动
     *
     * @param enable
     */
    public void enableDrag(boolean enable) {

    }
=======
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
}
