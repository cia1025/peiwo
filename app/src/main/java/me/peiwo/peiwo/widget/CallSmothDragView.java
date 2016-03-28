package me.peiwo.peiwo.widget;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by wallace on 16/3/14.
 * 电话界面向上收起使用这个VIEW
 */
public class CallSmothDragView extends FrameLayout {
    private ViewDragHelper dragHelper;
    private View mDragView;
    private boolean callback = false;
    private boolean enableDrag = false;
    public static final int OUTSIDE = 3;
    public static final int INSIDE = 4;

    public CallSmothDragView(Context context) {
        super(context);
        init();
    }

    public CallSmothDragView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CallSmothDragView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        dragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {

            @Override
            public void onViewDragStateChanged(int state) {
                if (CallSmothDragView.this.listener != null)
                    CallSmothDragView.this.listener.onDrag(state);
            }

            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return true;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return super.clampViewPositionHorizontal(child, left, dx);
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                if (top >= 0 || !enableDrag) return 0;
                return top;
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                invalidate();
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                Log.i("drag", "xvel==" + xvel + "--yvel==" + yvel);
                boolean settle;
                if (Math.abs(mDragView.getTop()) > getHeight() / 3) {
                    settle = dragHelper.settleCapturedViewAt(0, -getHeight());
                } else {
                    settle = dragHelper.settleCapturedViewAt(0, 0);
                }
                if (settle) {
                    callback = true;
                    invalidate();
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (dragHelper.continueSettling(true)) {
            invalidate();
        } else {
            if (callback) {
                callback = false;
                callbackDragSate();
            }
        }
    }

    private void callbackDragSate() {
        if (this.listener != null) {
            if (mDragView.getTop() == 0 && mDragView.getLeft() == 0) {
                this.listener.onDrag(INSIDE);
            } else {
                this.listener.onDrag(OUTSIDE);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDragView = getChildAt(0);
    }

    private CallDragListener listener;

    public void setCallDragListener(CallDragListener listener) {
        this.listener = listener;
    }

    private void reset() {
        Log.i("drag", "reset");
        if (mDragView != null && mDragView.getTop() > 0) {
            Log.i("drag", "getTop > 0");
            if (dragHelper.settleCapturedViewAt(0, 0)) {
                Log.i("drag", "settleCapturedViewAt");
                callback = true;
                invalidate();
            }
        }
    }

    public void enableDrag(boolean enable) {
        this.enableDrag = enable;
    }

    public interface CallDragListener {
        void onDrag(int state);
    }
}
