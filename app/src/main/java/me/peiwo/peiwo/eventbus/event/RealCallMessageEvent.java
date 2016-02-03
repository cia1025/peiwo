package me.peiwo.peiwo.eventbus.event;

import android.content.Intent;

public class RealCallMessageEvent {
	public Intent  intent;
	
	public RealCallMessageEvent(Intent  intent) {
		this.intent = intent;
	}
}
