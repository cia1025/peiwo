package me.peiwo.peiwo.eventbus.event;

import android.content.Intent;

public class ServiceMessageEvent {
	public Intent  intent;
	public ServiceMessageEvent(Intent  intent) {
		this.intent = intent;
	}
}
