package me.peiwo.peiwo.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.widget.CallWaitingView;
<<<<<<< HEAD
<<<<<<< HEAD
=======
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
import org.json.JSONObject;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import java.util.ArrayList;
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
    private CompositeSubscription mCompositeSubscription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCompositeSubscription = new CompositeSubscription();
    }

    public interface OnCallPreparedListener {
        void onCallPreparedSuccess(int permission, float price);

        void onCallPreparedError(int error, Object ret);
    }

//    public void prepareCalling(int mUid, int tUid, final int srcPermission, final float src_price, final Intent callIntent, final OnCallPreparedListener onCallPreparedListener, final boolean isHotValueTips) {
//        if (isckeckPermissioning.get())
//            return;
//        isckeckPermissioning.set(true);
//        PeiwoApp app = (PeiwoApp) getApplicationContext();
//        if (app.getIsCalling()) {
//            showToast(this, "您当前正在通话");
//            return;
//        }
//        callWaitingView = new CallWaitingView(this);
//        callWaitingView.show();
//        ApiRequestWrapper.getPermission(this, mUid, tUid, new MsgStructure() {
//            @Override
//            public void onReceive(JSONObject data) {
//                isckeckPermissioning.set(false);
//                Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(object -> {
//                    hideAnimDialog();
//                    // {"permission":1,"price":0}
//                    if (object != null) {
//                        //{"im_image_permission":1,"permission":1,"im_price":0,"anonymous_call_permission":0,"price":0.5,"im_permission":1}
//                        int channel = object.optInt("channel", 1);
//                        if (onCallPreparedListener != null) {
//                            int permission = object.optInt("permission");
//                            float price = Float.valueOf(String.format("%.1f", object.optDouble("price", 0)));
//                            onCallPreparedListener.onCallPreparedSuccess(permission, price);
//                            makeCall(permission, price, callIntent, isHotValueTips, false, channel);
//                        }
//                    } else {
//                        if (onCallPreparedListener != null) {
//                            onCallPreparedListener.onCallPreparedError(-1, null);
//                            makeCall(srcPermission, src_price, callIntent, isHotValueTips, false, 0);
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void onError(int error, Object ret) {
//                isckeckPermissioning.set(false);
//                Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
//                    hideAnimDialog();
//                    if (onCallPreparedListener != null) {
//                        onCallPreparedListener.onCallPreparedError(-1, null);
//                        makeCall(srcPermission, src_price, callIntent, isHotValueTips, false, 0);
//                    }
//                });
//            }
//        });
//    }

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
        Subscription subscription = Observable.combineLatest(getCallPermission(tUid), getCallChannel(tUid), (object, object2) -> {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject();
                jsonObject.put("permission", object);
                jsonObject.put("channel", object2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<JSONObject>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                deliverError(e, onCallPreparedListener, callIntent, srcPermission, src_price);
            }

            @Override
            public void onNext(JSONObject object) {
                deliverNext(object, onCallPreparedListener, callIntent, srcPermission, src_price);
            }
        });
        mCompositeSubscription.add(subscription);
    }

    private void deliverError(Throwable e, OnCallPreparedListener onCallPreparedListener, Intent callIntent, int srcPermission, float src_price) {
        isckeckPermissioning.set(false);
        hideAnimDialog();
        if (onCallPreparedListener != null) {
            onCallPreparedListener.onCallPreparedError(-1, null);
            makeCall(srcPermission, src_price, callIntent, false, false, 0, null);
        }
    }

    private void deliverNext(JSONObject _object, OnCallPreparedListener onCallPreparedListener, Intent callIntent, int srcPermission, float src_price) {
        isckeckPermissioning.set(false);
        hideAnimDialog();
        //{"channel_id":"1005_441","im_image_permission":1,"im_permission":1,"permission":1,"im_price":0,"anonymous_call_permission":0,"price":0,"channel":1}
        if (_object != null) {
            JSONObject channelObject = _object.optJSONObject("channel");
            JSONObject permissionObject = _object.optJSONObject("permission");
            int channel = channelObject.optInt("channel", DfineAction.CALL_CHANNEL_NORMAL);
            String channel_id = channelObject.optString("channel_id", null);
            if (onCallPreparedListener != null) {
                int permission = permissionObject.optInt("permission");
                float price = Float.valueOf(String.format("%.1f", permissionObject.optDouble("price", 0)));
                onCallPreparedListener.onCallPreparedSuccess(permission, price);
                makeCall(permission, price, callIntent, false, false, channel, channel_id);
            }
        } else {
            if (onCallPreparedListener != null) {
                onCallPreparedListener.onCallPreparedError(-1, null);
                makeCall(srcPermission, src_price, callIntent, false, false, 0, null);
            }
        }
    }

    private Observable<JSONObject> getCallChannel(int tuid) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("tuid", String.valueOf(tuid)));
        return ApiRequestWrapper.apiGetJson(this, params, AsynHttpClient.API_CALL_CHANNEL);
    }

    private Observable<JSONObject> getCallPermission(int tuid) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("tuid", String.valueOf(tuid)));
        return ApiRequestWrapper.apiGetJson(this, params, AsynHttpClient.API_USERINFO_PERMISSION);
    }

    private void hideAnimDialog() {
        if (callWaitingView != null && callWaitingView.isShowing()) {
            callWaitingView.dismiss();
        }
    }

    private void makeCall(int permission, final float price, final Intent callIntent, boolean isHotValueTips, final boolean isForceCall, int channel, String channel_id) {
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
                                realCall(callIntent, price, channel, channel_id);
                            }
=======
                        .setPositiveButton("呼叫", (dialog, which) -> {
                            realCall(callIntent, price, channel, channel_id);
>>>>>>> ef0fd79745cfc7f2142d30f43876e23d0762bef7
                        }).create().show();
            }
        } else {
            realCall(callIntent, price, channel, channel_id);
        }
    }

    private void realCall(Intent callIntent, float price, int channel, String channel_id) {
        if (callIntent != null) {
            callIntent.putExtra("price", price);
            if (channel == 1) {
                //agora
                dispatchAgoraCall(callIntent, price, channel, channel_id);
            } else {
                startActivity(callIntent);
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (mCompositeSubscription != null) {
            mCompositeSubscription.unsubscribe();
        }
    }

    private void dispatchAgoraCall(Intent callIntent, float price, int channel, String channel_id) {
//        Bundle bundle = callIntent.getExtras();
//        Set<String> keys = bundle.keySet();
//        for (String key : keys){
//
//        }
        Intent intent = new Intent(this, AgoraCallOutActivity.class);
        intent.putExtra(AgoraCallOutActivity.K_CALLEE_ID, callIntent.getIntExtra("tid", 0));
        intent.putExtra(AgoraCallOutActivity.K_CHANNEL, channel);
        intent.putExtra(AgoraCallOutActivity.K_CHANNEL_ID, channel_id);
        intent.putExtra(AgoraCallOutActivity.K_PRICE, Double.valueOf(price * 100).intValue());
        startActivity(intent);
    }
}