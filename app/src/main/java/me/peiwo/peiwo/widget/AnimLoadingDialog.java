package me.peiwo.peiwo.widget;

import me.peiwo.peiwo.R;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

public class AnimLoadingDialog extends Dialog {

	boolean isCenter = false;
	boolean canWatchOutsideTouch = true;
	boolean dimBehindEnabled = false;

	private ImageView loadingView;
	/**
	 * 设置dialog是否可以响应下面activity的事件
	 * 
	 * @method: setWatchOutsideTouch
	 * @description: TODO
	 * @author: DongFuhai
	 * @param flag
	 *            == true 可以响应，false 不能响应，默认==true
	 * @return: void
	 * @date: 2013-9-18 下午3:46:09
	 */
	public AnimLoadingDialog setWatchOutsideTouch(boolean canWatchOutsideTouch) {
		this.canWatchOutsideTouch = canWatchOutsideTouch;
		return this;
	}

	/**
	 * 设置dialog背景是不是变灰。默认是不变灰的
	 * 
	 * @method: setDimBehindEnabled
	 * @description: TODO
	 * @author: DongFuhai
	 * @param enable
	 * @return: void
	 * @date: 2013-9-18 下午4:09:11
	 */
	public AnimLoadingDialog setDimBehindEnabled(boolean dimBehindEnabled) {
		this.dimBehindEnabled = dimBehindEnabled;
		return this;
	}

	public AnimLoadingDialog setCentered(boolean isCenter) {
		this.isCenter = isCenter;
		return this;
	}

	public AnimLoadingDialog(Context context) {
		super(context, R.style.AnimDialogLoading);
	}

	private AnimLoadingDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public AnimLoadingDialog(Context context, int theme) {
		super(context, R.style.AnimDialogLoading);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (canWatchOutsideTouch) {
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
					WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
					WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
		}

		setContentView(R.layout.animloading);
		setCanceledOnTouchOutside(false);
		loadingView = (ImageView) findViewById(R.id.iv_loading);
	}

	@Override
	public void show() {
		super.show();
		startAnimation();
	}
	
	@Override
	public void dismiss() {
		super.dismiss();
		stopAnimation();
	}
	
	private void startAnimation() {
		AnimationDrawable anim = (AnimationDrawable) loadingView.getDrawable();
		anim.start();
	}
	
	private void stopAnimation() {
		AnimationDrawable anim = (AnimationDrawable) loadingView.getDrawable();
		anim.stop();
	}
	
	@Override
	public void cancel() {
		super.cancel();
		stopAnimation();
	}
}
