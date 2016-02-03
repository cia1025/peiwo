package me.peiwo.peiwo.receiver;

import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.util.CustomLog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 心跳超时，一直未收到服务器的心跳
 * @author kevin 
 * 需要重新连接Tcp
 */
public class HeartBeatTimeOutReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		CustomLog.i(DfineAction.TCP_TAG, "user operation tcp connect");
		TcpProxy.getInstance().connectionTcp();
	}
}
