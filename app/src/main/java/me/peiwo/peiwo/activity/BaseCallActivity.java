package me.peiwo.peiwo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.fragment.RewardDialogFragment;
import me.peiwo.peiwo.fragment.RewardedDialogFragment;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.FloatCallView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fuhaidong on 15/9/15.
 */
public class BaseCallActivity extends BaseActivity {

    private boolean show_rewarded_dialog = true;
    private int mTargetUid;
    private String avatar;
    private String name;
    private String type;
    private Bundle data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public void sendIntentRewardMessage(int mTargetUid, String type, String money) {
        //money 为收到打赏的钱，发给服务器使用
        try {
//        uid	y	number	用户id（所以消息必须项）
//        tuid	y	number	打赏的对象uid
//        type	y	number	1: 匿名聊， 2: 普通电话
            this.type = type;
            JSONObject o = new JSONObject();
            o.put("msg_type", DfineAction.IntentRewardMessage);
            o.put("uid", UserManager.getUid(this));
            o.put("tuid", mTargetUid);
            o.put("type", type);
//            if (!TextUtils.isEmpty(money)) {
//                //money传给服务器String，服务器类型转换错误,只能传int
//                o.put("money", Integer.valueOf(money));
//            }
            TcpProxy.getInstance().sendTCPMessage(o.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void disMissRewardFragment() {
        if (rewarded_fragment != null) {
            rewarded_fragment.dismiss();
            rewarded_fragment = null;
        }
        if (rewardDialogFragment != null) {
            rewardDialogFragment.dismiss();
            rewardDialogFragment = null;
        }
    }

    private RewardedDialogFragment rewarded_fragment;

    protected void handleRewardedMessage(final int mTargetUid, String avatar, String name, final String type, Bundle data) {
        try {
            String jsonStr = data.getString("data");
            final JSONObject o = new JSONObject(jsonStr);
            if (o.has("balance")) {
                //单位 分
                int balance = o.optInt("balance");
                UserManager.updateMoney(this, String.valueOf(balance / 100.00f));
            }
            if (!show_rewarded_dialog) {
                this.mTargetUid = mTargetUid;
                this.avatar = avatar;
                this.name = name;
                this.type = type;
                this.data = data;
                return;
            }
            disMissRewardFragment();
            //收到对方打赏
//        uid	y	number	用户id（所以消息必须项）
//        fuid	y	number	打赏的来源用户uid
//        money	y	number	打赏的金额
//        msg	y	number	打赏的文字消息
//        balance	y	number	用户的余额
            rewarded_fragment = RewardedDialogFragment.newInstance(avatar, name, o.optString("msg"), o.optString("money"));
            rewarded_fragment.show(getSupportFragmentManager(), rewarded_fragment.toString());
            rewarded_fragment.setOnRewardToYouListener(() -> {
                //type	y	number	1: 匿名聊， 2: 普通电话
                sendIntentRewardMessage(mTargetUid, type, o.optString("money"));
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void hanlePayRewardResponseMessage(Bundle data) {
//        code	y	number	0: 正常, 60002: 余额不足, 60001: 其他支付失败的情况
//        tuid	y	number	打赏对象的uid
//        transaction	y	number	事务id
//        money	y	number	打赏的金额
//        balance	y	number	用户余额
        try {
            String jsonStr = data.getString("data");
            JSONObject o = new JSONObject(jsonStr);
            if (o.has("balance")) {
                //单位 分
                int balance = o.optInt("balance");
                UserManager.updateMoney(this, String.valueOf(balance / 100.00f));
            }
            String code = o.optString("code");
            if ("60002".equals(code)) {
                showToast(this, "余额不足");
            } else if ("0".equals(code)) {
                showToast(this, "打赏成功");
                //具体的页面展示
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    RewardDialogFragment rewardDialogFragment;

    protected void handleIntentRewardResponseMessage(String call_id, final int mTargetUid, final int gender, Bundle data) {
        try {
//            code	y	number	0: 正常, 60002: 余额不足
//            transaction	n	number	code为0时存在，事务id
//            balance	n	number	code为60002时存在，用户余额
//            money	y	number	随机的打赏金额
//            msg	y	string	打赏信息
            String jsonStr = data.getString("data");
            if (rewardDialogFragment != null && rewardDialogFragment.getDialog() != null && rewardDialogFragment.getDialog().isShowing()) {
                rewardDialogFragment.refreshStatus(jsonStr);
                return;
            }
            final JSONObject o = new JSONObject(jsonStr);
            rewardDialogFragment = RewardDialogFragment.newInstance(mTargetUid, type, o.optInt("transaction"), o.optString("msg"), o.optString("money"), o.optString("code"), gender);
            rewardDialogFragment.show(getSupportFragmentManager(), getClass().getSimpleName());
            rewardDialogFragment.setOnRewardActionListener((code, tuid, transaction) -> {
                if ("60002".equals(code)) {
                    //余额不足
                    //产品修改需求，女生也能充值
//                        if (gender == 2) {
//                            //女生直接取消
//                            return;
//                        }
                    reChargeMoney();
                } else if ("0".equals(code)) {
                    //打赏
                    sendPayRewardMessage(call_id, tuid, transaction);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void reChargeMoney() {

    }

    protected void sendPayRewardMessage(String call_id, int mTargetUid, int transaction) {
//        uid	y	number	用户id（所以消息必须项）
//        transaction	y	number	需要支付的事务id
//        tuid
        try {
            JSONObject o = new JSONObject();
            o.put("msg_type", DfineAction.PayRewardMessage);
            o.put("uid", UserManager.getUid(this));
            o.put("tuid", mTargetUid);
            o.put("call_id", call_id);
            o.put("transaction", transaction);
            TcpProxy.getInstance().sendTCPMessage(o.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        disMissRewardFragment();
        super.onPause();
    }

    @Override
    protected void onStop() {
        show_rewarded_dialog = false;
        if (skip.getAndIncrement() <= 0) {
            addFloatViewOnScreen();
        }
        super.onStop();
    }

    protected WildCatCallActivity.WILDCAT_STATE getWildState() {
        return WildCatCallActivity.WILDCAT_STATE.NORMAL;
    }

    @Override
    public void onResume() {
        show_rewarded_dialog = true;
        super.onResume();
        if (skip.get() != 0) {
            if (mFloatCallView != null) {
                getWindowManager().removeView(mFloatCallView);
            }
            mFloatCallView = null;
            skip.getAndDecrement();
        }
        if (data != null && (getWildState() == WildCatCallActivity.WILDCAT_STATE.CALLING_STATE || this instanceof RealCallActivity)) {
            handleRewardedMessage(this.mTargetUid, this.avatar, this.name, this.type, this.data);
        }
        this.mTargetUid = -1;
        this.avatar = null;
        this.name = null;
        this.type = null;
        this.data = null;
        Intent intent = new Intent(Constans.ACTION_CLOSE_USERINFO);
        intent.putExtra(UserInfoActivity.TARGET_UID, getTargetUid());
        EventBus.getDefault().post(intent);
    }

    protected int getTargetUid() {
        return -1;
    }

    @Override
    public void finish() {
        if (mFloatCallView != null) {
            getWindowManager().removeView(mFloatCallView);
            mFloatCallView = null;
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        if (mFloatCallView != null) {
            getWindowManager().removeView(mFloatCallView);
            mFloatCallView = null;
        }
        super.onDestroy();
    }

    private FloatCallView mFloatCallView;
    private AtomicInteger skip = new AtomicInteger();

    private void addFloatViewOnScreen() {
        int imageSize = PWUtils.getPXbyDP(this, 66);
        mFloatCallView = new FloatCallView(getApplicationContext());
        mFloatCallView.setImageResource(R.drawable.ic_call_back);
        //ImageLoader.getInstance().displayImage(mFaceShowUrl, iv_avatar, ImageUtil.getRoundedOptionsWithRadius(imageSize / 2));
        mFloatCallView.setOnClickListener(v -> {
            PeiwoApp app = (PeiwoApp) getApplicationContext();
            app.startCallActivity(BaseCallActivity.this.getClass());
        });
        //获取WindowManager
        WindowManager mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        //设置LayoutParams(全局变量）相关参数
        WindowManager.LayoutParams param = new WindowManager.LayoutParams();

        param.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;     // 系统提示类型,重要
        param.format = 1;
        param.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; // 不能抢占聚焦点
        param.flags = param.flags | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        param.flags = param.flags | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS; // 排版不受限制

        param.alpha = 1.0f;

        param.gravity = Gravity.START | Gravity.TOP;
        //以屏幕左上角为原点，设置x、y初始值
        param.x = 0;
        param.y = PWUtils.getWindowHeight(this) / 2;

        //设置悬浮窗口长宽数据
        param.width = imageSize;
        param.height = imageSize;
        mFloatCallView.setParams(mWindowManager, param);
        //显示myFloatView图像
        mWindowManager.addView(mFloatCallView, param);
    }
}
