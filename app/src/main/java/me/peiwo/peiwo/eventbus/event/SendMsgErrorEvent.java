package me.peiwo.peiwo.eventbus.event;

import android.content.Intent;


public class SendMsgErrorEvent {
	public Intent  errorMsgIntent;
	public SendMsgErrorEvent(Intent  errorMsgIntent) {
		this.errorMsgIntent = errorMsgIntent;
	}
}
