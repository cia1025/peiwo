package me.peiwo.peiwo.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.TimeUtil;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.CallWaitingView;
<<<<<<< HEAD
import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

=======
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

>>>>>>> ef0fd79745cfc7f2142d30f43876e23d0762bef7
/**
 * Created by fuhaidong on 14/11/25.
 * 打电话前的base activity，如果有activity存在需要校验权限跟价格的，需要继承这个父类
 */
public class PWPreCallingActivity extends BaseActivity {

    private AtomicBoolean isckeckPermissioning = new AtomicBoolean(false);
    private CallWaitingView callWaitingView;
    private static final int HAS_CALL_PERMISSION = 1;
    private static final int HAS_NOT_CALL_PERMISSION = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public interface OnCallPreparedListener {
        void onCallPreparedSuccess(int permission, float price);

        void onCallPreparedError(int error, Object ret);
    }

    public void prepareCalling(int mUid, int tUid, final int srcPermission, final float src_price, final Intent callIntent, final OnCallPreparedListener onCallPreparedListener, final boolean isHotValueTips) {
        if (isckeckPermissioning.get())
            return;
        isckeckPermissioning.set(true);
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        if (app.getIsCalling()) {
            showToast(this, "您当前正在通话");
            return;
        }
        callWaitingView = new CallWaitingView(this);
        callWaitingView.show();
        ApiRequestWrapper.getPermission(this, mUid, tUid, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                isckeckPermissioning.set(false);
                hideAnimDialog();
                // {"permission":1,"price":0}
                if (data != null) {
                    if (onCallPreparedListener != null) {
                        int permission = data.optInt("permission");
                        float price = Float.valueOf(String.format("%.1f", data.optDouble("price", 0)));
                        onCallPreparedListener.onCallPreparedSuccess(permission, price);
                        makeCall(permission, price, callIntent, isHotValueTips, false);
                    }
                } else {
                    if (onCallPreparedListener != null) {
                        onCallPreparedListener.onCallPreparedError(-1, null);
                        makeCall(srcPermission, src_price, callIntent, isHotValueTips, false);
                    }
                }

            }

            @Override
            public void onError(int error, Object ret) {
                isckeckPermissioning.set(false);
                hideAnimDialog();
                if (onCallPreparedListener != null) {
                    onCallPreparedListener.onCallPreparedError(-1, null);
                    makeCall(srcPermission, src_price, callIntent, isHotValueTips, false);
                }
            }
        });
    }

    public void prepareCalling(int mUid, int tUid, final int srcPermission, final float src_price, final Intent callIntent, final OnCallPreparedListener onCallPreparedListener) {
        if (isckeckPermissioning.get())
            return;
        isckeckPermissioning.set(true);
        PeiwoApp app = (PeiwoApp) getApplicationContext();
        if (app.getIsCalling()) {
            showToast(this, "您当前正在通话");
            return;
        }
        callWaitingView = new CallWaitingView(this);
        callWaitingView.show();
        ApiRequestWrapper.getPermission(this, mUid, tUid, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                isckeckPermissioning.set(false);
                hideAnimDialog();
                // {"permission":1,"price":0}
                if (data != null) {
                    if (onCallPreparedListener != null) {
                        int permission = data.optInt("permission");
                        float price = Float.valueOf(String.format("%.1f", data.optDouble("price", 0)));
                        onCallPreparedListener.onCallPreparedSuccess(permission, price);
                        makeCall(permission, price, callIntent, false, false);
                    }
                } else {
                    if (onCallPreparedListener != null) {
                        onCallPreparedListener.onCallPreparedError(-1, null);
                        makeCall(srcPermission, src_price, callIntent, false, false);
                    }
                }
            }

            @Override
            public void onError(int error, Object ret) {
                isckeckPermissioning.set(false);
                hideAnimDialog();
                if (onCallPreparedListener != null) {
                    onCallPreparedListener.onCallPreparedError(-1, null);
                    makeCall(srcPermission, src_price, callIntent, false, false);
                }
            }
        });
    }

    private void hideAnimDialog() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (callWaitingView != null && callWaitingView.isShowing()) {
                    callWaitingView.dismiss();
                }
            }
        });
    }

    private void makeCall(int permission, final float price, final Intent callIntent, boolean isHotValueTips, final boolean isForceCall) {
        if (permission == HAS_NOT_CALL_PERMISSION) { //无权限

//        		if(isHotValueTips){
//        			showToast(this, "集满热度或相互关注才能打电话哦!");
//        		}else{
//        			showToast(this, "你们取消了通话权限，无法进行通话");
//        		}
            showToast(this, "你们取消了通话权限，无法进行通话");
            return;
        }
        String money = UserManager.getPWUser(this).money;
        money = TextUtils.isEmpty(money) ? "0" : money;
        if (price > Float.valueOf(money)) {
            if (!isFinishing()) {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.your_balance_is_not_enough_for_calling)
                        .setNegativeButton("取消", null)
                        .setPositiveButton("充值", (dialog, which) -> {
                            startActivity(new Intent(PWPreCallingActivity.this, ChargeActivity.class));
                        }).create().show();
            }
            return;
        }
        if (price > 0) {
            if (!isFinishing()) {
                new AlertDialog.Builder(this)
                        .setMessage(String.format(Locale.getDefault(),
                                "接听后将按照%.1f元/分钟计费,是否继续呼叫?", price))
                        .setNegativeButton("取消", null)
<<<<<<< HEAD
                        .setPositiveButton("呼叫", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                realCall(callIntent, price);
                            }
=======
                        .setPositiveButton("呼叫", (dialog, which) -> {
                            realCall(callIntent, price, channel, channel_id);
>>>>>>> ef0fd79745cfc7f2142d30f43876e23d0762bef7
                        }).create().show();
            }
        } else {
            realCall(callIntent, price);
        }
    }

    private void realCall(Intent callIntent, float price) {
        if (callIntent != null) {
            callIntent.putExtra("price", price);
            startActivity(callIntent);
        }
    }
}