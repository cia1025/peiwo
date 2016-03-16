package me.peiwo.peiwo.eventbus.event;

public class MessagePushEvent {
	public int a;
	public int m;
	public int c;

	public int getTuid() {
		return tuid;
	}

	public void setTuid(int tuid) {
		this.tuid = tuid;
	}

	public int tuid;

	public MessagePushEvent(int a, int m, int c) {
		this.a = a;
		this.m = m;
		this.c = c;

	}
	
	public MessagePushEvent() {
	}

}
