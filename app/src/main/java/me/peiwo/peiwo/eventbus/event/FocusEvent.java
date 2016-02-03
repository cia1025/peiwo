package me.peiwo.peiwo.eventbus.event;

public class FocusEvent {
	public static final int FOCUS_SUCCESS_EVENT = 1;
	public static final int UNFOCUS_SUCCESS_EVENT = 2;

	public int type;
	public String err_msg;

	public FocusEvent(int type) {
		this.type = type;
	}

	public FocusEvent(String err_msg) {
		this.err_msg = err_msg;
	}
}
