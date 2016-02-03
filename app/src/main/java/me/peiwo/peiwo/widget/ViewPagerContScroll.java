package me.peiwo.peiwo.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by fuhaidong on 14-3-15.
 */
public class ViewPagerContScroll extends ViewPager implements View.OnTouchListener {
    public ViewPagerContScroll(Context context) {
        super(context);
        setOnTouchListener(this);
    }

    public ViewPagerContScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }
}
