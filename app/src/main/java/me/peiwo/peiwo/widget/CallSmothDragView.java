package me.peiwo.peiwo.widget;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
<<<<<<< HEAD
import android.util.Log;
=======
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
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
<<<<<<< HEAD
    private boolean enableDrag = false;
    public static final int OUTSIDE = 3;
    public static final int INSIDE = 4;
=======
    public static final int OUTSIDE = 1;
    public static final int INSIDE = 2;
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198

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
<<<<<<< HEAD

            @Override
            public void onViewDragStateChanged(int state) {
                if (CallSmothDragView.this.listener != null)
                    CallSmothDragView.this.listener.onDrag(state);
            }

=======
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
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
<<<<<<< HEAD
                if (top >= 0 || !enableDrag) return 0;
=======
                if (top >= 0) return 0;
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
                return top;
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                invalidate();
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
<<<<<<< HEAD
                Log.i("drag", "xvel==" + xvel + "--yvel==" + yvel);
=======
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
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

<<<<<<< HEAD
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

=======
>>>>>>> 565f4dfcc21fd4710896162e9996805d0bed5198
    public interface CallDragListener {
        void onDrag(int state);
    }
}
