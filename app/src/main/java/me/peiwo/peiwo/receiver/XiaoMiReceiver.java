package me.peiwo.peiwo.receiver;

import java.util.List;
import java.util.Map;

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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

/**
 * 小米推送
 * 
 * @author zzl
 * 
 */
public class XiaoMiReceiver extends PushMessageReceiver {

	@Override
	public void onReceiveMessage(Context context, MiPushMessage message) {
		String content = message.getDescription();
        // a=1(打开app),m=1(消息类型，需要刷新消息界面)content(内容)
        Map<String, String> map = message.getExtra();
        int m = 0;
        int c = 0;
        int a = 0;
        
        String content_m = map.get("m");
        String content_c = map.get("c");
        String content_a = map.get("a");
        
		if (!TextUtils.isEmpty(content_m)) {
			m = Integer.parseInt(content_m);
		}

		if (!TextUtils.isEmpty(content_c)) {
			c = Integer.parseInt(content_c);
		}
		
		if (!TextUtils.isEmpty(content_a)) {
			a = Integer.parseInt(content_a);
		}
        EventBus.getDefault().post(new MessagePushEvent(a, m, c));
		if (m == 1 || c == 1) {
			try {
				if(!TextUtils.isEmpty(content)){
					notifition(context,content,c);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onCommandResult(Context context, MiPushCommandMessage message) {

		String command = message.getCommand();
		List<String> arguments = message.getCommandArguments();
		String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments
				.get(0) : null);
		if (MiPushClient.COMMAND_REGISTER.equals(command)) {
			MiPushClient.clearNotification(context);
			if (message.getResultCode() == ErrorCode.SUCCESS) {
				// 获取小米RegID
				System.out.println("------小米接收到REGID------"+cmdArg1);
		        if (context == null) {
		            return;
		        }
		        if (UserManager.isLogin(context)) {
		            PeiwoApp app = (PeiwoApp) context.getApplicationContext();
		            app.reportPushToken(cmdArg1, Constans.PLATFORM_XIAOMI);
		        } else {
		            SharedPreferencesUtil.putStringExtra(context,Constans.SP_KEY_XIAOMIREGID, cmdArg1);
		        }
		 
			}
		} 
	}
	
	private void notifition(Context context, String content, int c)
            throws JSONException {
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
}
