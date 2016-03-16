package me.peiwo.peiwo.widget;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * Created by Mike on 16/3/10.
 * Description:
 */
public class ControllableSwitchCompat extends SwitchCompat {

    private boolean isControlled;
    private String toastContent;
    private Toast myToast = null;

    public ControllableSwitchCompat(Context context) {
        super(context);
    }

    public ControllableSwitchCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ControllableSwitchCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isControlled() && event.getAction() == MotionEvent.ACTION_DOWN) {
//            Toast.makeText(getContext(), getToastContent(), Toast.LENGTH_SHORT).show();
            showToast(getContext(), getToastContent(), Toast.LENGTH_SHORT);
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    public boolean isControlled() {
        return isControlled;
    }

    public void setIsControlled(boolean isControlled) {
        this.isControlled = isControlled;
    }

    public String getToastContent() {
        return toastContent;
    }

    public void setToastContent(String toastContent) {
        this.toastContent = toastContent;
    }

    private void showToast(Context context, String text, int duration) {
        if (myToast != null) {
            myToast.setText(text);
        } else {
            myToast = Toast.makeText(context, text, duration);
        }
        myToast.show();
    }

}
