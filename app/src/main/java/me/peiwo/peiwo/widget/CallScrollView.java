package me.peiwo.peiwo.widget;

import me.peiwo.peiwo.util.PWUtils;
import net.simonvt.numberpicker.Scroller;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by fuhaidong on 14-9-17.
 */
public class CallScrollView extends LinearLayout {
    private Scroller mScroller;
    private GestureDetector mGestureDetector;
    private int s_h;
    private OnScrollUpFinishListener mListener;
    private boolean isScroll = true;
    public void setOnScrollUpFinishListener(OnScrollUpFinishListener mListener) {
        this.mListener = mListener;
    }

    public CallScrollView(Context context) {
        super(context);
        init(context);
    }

    public CallScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

//    public CallScrollView(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        init(context);
//    }


	public void setIsScroller(boolean isScroll) {
		this.isScroll = isScroll;
	}
	
    private void init(Context context) {
        mScroller = new Scroller(context);
        mGestureDetector = new GestureDetector(context, new MyGestureDetector());
        s_h = PWUtils.getWindowHeight((Activity) context) / 3;
    }


    public void reset() {
        scrollTo(0, 0);
        postInvalidate();
    }

    class MyGestureDetector implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float finger_originY = e2.getY() - e1.getY();
            if (finger_originY > 0 && getScrollY() <= 0) {
                scrollTo(0, 0);
                return false;
            }
            scrollBy(0, (int) distanceY);
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
    	if (isScroll) {
    		mGestureDetector.onTouchEvent(event);
    	}
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                int scrolly = getScrollY();
                if (scrolly > s_h) {
                    mScroller.startScroll(getScrollX(),
                            getScrollY(), 0, 3 * s_h - scrolly, 1000);
                    if (mListener != null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mListener.onScrollUpFinish();
                            }
                        }, 1000);
                    }
                } else {
                    //Toast.makeText(this, "还没滑到1/3", 1000).show();
                    mScroller.startScroll(getScrollX(),
                            getScrollY(), 0, -scrolly, 500);
                }
                invalidate();
                break;
        }
        return super.onInterceptTouchEvent(event);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }


    public interface OnScrollUpFinishListener {
        void onScrollUpFinish();
    }
}
