package me.peiwo.peiwo.widget;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.PWUtils;

/**
 * Created by fuhaidong on 15/9/15.
 */
public class FloatCallView extends ImageView {
    private int mWidth;
    private int mHeight;

    public FloatCallView(Context context) {
        super(context);
        screenWidth = PWUtils.getWindowWidth(context);
        screenHeight = PWUtils.getWindowHeight(context);
    }

    public FloatCallView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatCallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private WindowManager.LayoutParams wmParams;
    private WindowManager wm;
    private int screenWidth;
    private int screenHeight;

    public void setParams(WindowManager windowManager, WindowManager.LayoutParams params) {
        this.wm = windowManager;
        this.wmParams = params;
    }

    private OnClickListener listener;

    @Override
    public void setOnClickListener(OnClickListener l) {
        this.listener = l;
    }

    private int lastX, lastY;
    private int paramX, paramY;
    private long lastUptimeMillis;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mWidth == 0 || mHeight == 0) {
            mWidth = getWidth();
            mHeight = getHeight();
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                paramX = wmParams.x;
                paramY = wmParams.y;
                lastUptimeMillis = SystemClock.uptimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) event.getRawX() - lastX;
                int dy = (int) event.getRawY() - lastY;
                wmParams.x = paramX + dx;
                wmParams.y = paramY + dy;

                if (wmParams.x < 0) {
                    wmParams.x = 0;
                }
                if (wmParams.y < 0) {
                    wmParams.y = 0;
                }
                if ((wmParams.x + mWidth) > screenWidth) {
                    wmParams.x = screenWidth - mWidth;
                }
                CustomLog.d("moving, pos_y is : "+wmParams.y);
                if ((wmParams.y + mHeight) > screenHeight) {
                    wmParams.y = screenHeight - mHeight;
                }
                // Update suspended window position
                wm.updateViewLayout(this, wmParams);
                break;
            case MotionEvent.ACTION_UP:
                if (isClick(SystemClock.uptimeMillis() - lastUptimeMillis)) {
                    if (listener != null) {
                        listener.onClick(this);
                        Log.i("tag", "onClick(this)");
                    }
                }
                break;
        }
        return true;
    }

    private boolean isClick(long uptimeMillis) {
        Log.i("tag", "uptimeMillis=" + uptimeMillis);
        //触摸时间在170ms之内认为是点击事件
        return uptimeMillis < 170;
    }


}
