package me.peiwo.peiwo.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import com.tencent.android.tpush.*;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.ResultActivity;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.PWActionConfig;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.MessagePushEvent;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.SharedPreferencesUtil;
import me.peiwo.peiwo.util.UserManager;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dong Fuhai on 2014-07-29 12:28. 信鸽的推送
 *
 * @modify:
 */
public class XGReceiver extends XGPushBaseReceiver {

    @Override
    public void onRegisterResult(Context context, int i,
                                 XGPushRegisterResult xgPushRegisterResult) {
        if (context == null) {
            return;
        }
        // Trace.i("onRegisterResult token == " +
        // XGPushConfig.getToken(context));
        if (UserManager.isLogin(context)) {
            PeiwoApp app = (PeiwoApp) context.getApplicationContext();
//            app.reportXGToken(XGPushConfig.getToken(context));
            app.reportPushToken(XGPushConfig.getToken(context), Constans.PLATFORM_XG);
        } else {
            SharedPreferencesUtil.putStringExtra(context,
                    Constans.SP_KEY_XGTOKEN, XGPushConfig.getToken(context));
        }
    }

    @Override
    public void onUnregisterResult(Context context, int i) {

    }

    @Override
    public void onSetTagResult(Context context, int i, String s) {

    }

    @Override
    public void onDeleteTagResult(Context context, int i, String s) {

    }

    @Override
    public void onTextMessage(Context context,
                              XGPushTextMessage xgPushTextMessage) {

        // 消息透传
        // String text = "收到消息:" + xgPushTextMessage.toString();
        // 获取自定义key-value
        // a=1(打开app),m=1(消息类型，需要刷新消息界面)content(内容)
        String content_res = xgPushTextMessage == null ? "" : xgPushTextMessage.getCustomContent();
        String content = xgPushTextMessage == null ? "" : xgPushTextMessage.getContent();
        if (!TextUtils.isEmpty(content_res) && context != null) {
            try {
                JSONObject obj = new JSONObject(content_res);
                int m = obj.has("m") ? obj.getInt("m") : 0;
                int c = obj.has("c") ? obj.getInt("c") : 0;
                int a = obj.has("a") ? obj.getInt("a") : 0;
                EventBus.getDefault().post(new MessagePushEvent(a, m, c));
                
				if (!TextUtils.isEmpty(content)) {
					notifition(context, content);
				}
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void notifition(Context context, String content)
            throws JSONException {
        // a=1(打开app),m=1(消息类型，需要刷新消息界面)content(内容)
    	//c 申请
        
        if (PWUtils.isOnForeground(context))
            return;
        PWUserModel model = UserManager.getPWUserForService(context);
        if (model == null || model.uid <= 0 || TextUtils.isEmpty(model.session_data) || model.state == 0) {
            return;
        }
        Intent resultIntent = new Intent(context, ResultActivity.class);
        resultIntent.setAction(PWActionConfig.ACTION_NOTICATION);

        String key = SharedPreferencesUtil.getStringExtra(context, Constans.SP_KEY_PUSH_STR, "");
        if (!PWUtils.isNeedPush(context)) {
        	return;
        }
        int notifiy_defults = Notification.DEFAULT_LIGHTS; //开启灯光
        if (!TextUtils.isEmpty(key)) {
            JSONObject object = new JSONObject(key);
            boolean sound = object.optBoolean("sound");
            boolean vibrate = object.optBoolean("vibrate");
            if (sound) {
                notifiy_defults = notifiy_defults | Notification.DEFAULT_SOUND; // 设置了声音
            }
            if (vibrate) {
                notifiy_defults = notifiy_defults | Notification.DEFAULT_VIBRATE;// 开了震动
            }
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setTicker(content).setContentText(content).setAutoCancel(true)
                .setDefaults(notifiy_defults);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (resultPendingIntent == null)
            return;
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotifyMgr = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(Constans.NOTIFY_ID_MESSAGE, mBuilder.build());
    }

    @Override
    public void onNotifactionClickedResult(Context context,
                                           XGPushClickedResult xgPushClickedResult) {
        // if (context == null || xgPushClickedResult == null) {
        // return;
        // }

    }

    @Override
    public void onNotifactionShowedResult(Context context,
                                          XGPushShowedResult xgPushShowedResult) {
        //Trace.i("xgPushShowedResult == " + xgPushShowedResult);
    }
}
