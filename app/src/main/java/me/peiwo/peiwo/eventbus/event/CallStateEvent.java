package me.peiwo.peiwo.eventbus.event;

import me.peiwo.peiwo.service.CoreService.RealCallState;

public class CallStateEvent {
	public int type;//0--显示自己网络状态，WebRTC状态， //1--显示自己和对方的网络好坏  //2--异常情况需要挂断电话
	public boolean nTCPState;//true连接中，false断开中
	public int nWebRTCState;
	public RealCallState nCallState;
	
	public int heart_lost_count;
	public int remote_user_state;
}
