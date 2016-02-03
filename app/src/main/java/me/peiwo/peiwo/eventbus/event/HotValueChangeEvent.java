package me.peiwo.peiwo.eventbus.event;

import android.content.Intent;

public class HotValueChangeEvent {
	public Intent intent;
	public HotValueChangeEvent(Intent intent) {
		this.intent = intent;
	}
}
