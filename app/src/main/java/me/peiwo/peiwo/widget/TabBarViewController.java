package me.peiwo.peiwo.widget;

import me.peiwo.peiwo.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by Dong Fuhai on 2014-06-27 18:15.
 *
 * @modify:
 */
public class TabBarViewController extends FrameLayout implements
        View.OnClickListener {

    private TextView tv_discover;
    // private TextView tv_call;
    private TextView tv_message;
    private TextView tv_me;
    private TextView tv_wildcat;

    public TabBarViewController(Context context) {
        super(context);
        init();
    }

    public TabBarViewController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @SuppressLint("NewApi")
    public TabBarViewController(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_tabbar, this);
        ViewGroup parent = (ViewGroup) getChildAt(0);
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            child.setTag(i);
            child.setOnClickListener(this);
        }
//        parent.getChildAt(0).setBackgroundResource(
//                R.drawable.tab_bg_color_selected);
        tv_discover = (TextView) parent.findViewById(R.id.tv_discover);
        tv_wildcat = (TextView) parent.findViewById(R.id.tv_wildcat);
        tv_message = (TextView) parent.findViewById(R.id.tv_message);
        tv_me = (TextView) parent.findViewById(R.id.tv_me);
        tv_discover.setSelected(true);
    }

    public interface OnTabChangedListener {
        public void onTabChangedListener(int index);
    }

    private OnTabChangedListener onTabChangedListener;

    public void setOnTabChangedListener(
            OnTabChangedListener onTabChangedListener) {
        this.onTabChangedListener = onTabChangedListener;
    }

    @Override
    public void onClick(View v) {
        if (this.onTabChangedListener != null) {
            this.onTabChangedListener
                    .onTabChangedListener((Integer) v.getTag());
        }
    }

    public void changeStatus(int index) {
//        ViewGroup parent = (ViewGroup) getChildAt(0);
//        for (int i = 0; i < parent.getChildCount(); i++) {
//            if (i != index) {
//                parent.getChildAt(i).setBackgroundResource(
//                        R.drawable.tab_bg_color_normal);
//            }
//        }
//        parent.getChildAt(index).setBackgroundResource(
//                R.drawable.tab_bg_color_selected);
        if (index == 0) {
            tv_discover.setSelected(true);
            tv_wildcat.setSelected(false);
            tv_message.setSelected(false);
            tv_me.setSelected(false);
        } else if (index == 1) {
            tv_wildcat.setSelected(true);
            tv_message.setSelected(false);
            tv_discover.setSelected(false);
            tv_me.setSelected(false);
        } else if (index == 2) {
            tv_message.setSelected(true);
            tv_discover.setSelected(false);
            tv_wildcat.setSelected(false);
            tv_me.setSelected(false);
        } else {
            tv_me.setSelected(true);
            tv_message.setSelected(false);
            tv_discover.setSelected(false);
            tv_wildcat.setSelected(false);
        }
    }
    
    public void setRedPonitVisibility(boolean visibility){
		if (visibility) {
    		findViewById(R.id.iv_red_point).setVisibility(View.VISIBLE);
		} else {
    		findViewById(R.id.iv_red_point).setVisibility(View.GONE);
    	}
    	
    }
    
}
