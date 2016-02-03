package me.peiwo.peiwo.eventbus.event;

import android.content.Intent;

public class WildCatMessageEvent {
	public Intent  intent;
	public WildCatMessageEvent(Intent  intent) {
		this.intent = intent;
	}
}
