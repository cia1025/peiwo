package me.peiwo.peiwo.eventbus.event;

public class WildCatCallingEvent {
	boolean isCalling = false;
	
	public WildCatCallingEvent(boolean isCalling) {
		this.isCalling = isCalling;
	}

	public boolean isCalling() {
		return isCalling;
	}
}
