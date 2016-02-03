package me.peiwo.peiwo.widget;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.ExpressionPanelAdapter;
import me.peiwo.peiwo.callback.ExpressionDeleteEmotionListener;
import me.peiwo.peiwo.callback.ExpressionItemClickListener;
import me.peiwo.peiwo.fragment.ExpressionPanelFragment;
import me.peiwo.peiwo.model.ExpressionBaseModel;

/**
 * Created by fuhaidong on 15/11/18.
 */
public class ExpressionPanelView extends LinearLayout implements ViewPager.OnPageChangeListener, View.OnClickListener, ExpressionItemClickListener {
    private static final int MAX_PAGE = 2;
    private View v_indicator_emotion;
    private View v_indicator_gif;
    private ViewPager vp_expression;
    private ExpressionItemClickListener listener;
    private ExpressionDeleteEmotionListener deleteEmotionListener;

    public ExpressionPanelView(Context context) {
        super(context);
        init();
    }

    public ExpressionPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExpressionPanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_expression_panel, this, true);
        vp_expression = (ViewPager) findViewById(R.id.vp_expression);
        v_indicator_emotion = findViewById(R.id.v_indicator_emotion);
        v_indicator_gif = findViewById(R.id.v_indicator_gif);
        v_indicator_emotion.setOnClickListener(this);
        v_indicator_gif.setOnClickListener(this);
        View v_delete_emotion = findViewById(R.id.v_delete_emotion);
        v_delete_emotion.setOnClickListener(this);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            if (vp_expression.getAdapter() == null) {
                prepareData(vp_expression);
                vp_expression.addOnPageChangeListener(this);
            }
        }
    }

    private void prepareData(ViewPager vp_expression) {
        vp_expression.setOffscreenPageLimit(MAX_PAGE);
        Context context = getContext();
        if (context instanceof AppCompatActivity) {
            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            vp_expression.setAdapter(new ExpressionPanelAdapter(fragmentManager, MAX_PAGE));
            setOnExpressionListener(fragmentManager);
        }
    }

    private void setOnExpressionListener(FragmentManager fragmentManager) {
        post(() -> {
            for (int i = 0; i < MAX_PAGE; i++) {
                String tag = makeFragmentName(vp_expression.getId(), i);
                Fragment f = fragmentManager.findFragmentByTag(tag);
                if (f instanceof ExpressionPanelFragment) {
                    ((ExpressionPanelFragment) f).setOnExpressionItemClickListener(this);
                }
            }
        });
    }

    private String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            v_indicator_emotion.setBackgroundColor(getResources().getColor(R.color.c_gray));
            v_indicator_gif.setBackgroundColor(getResources().getColor(R.color.transparent));
        } else if (position == 1) {
            v_indicator_gif.setBackgroundColor(getResources().getColor(R.color.c_gray));
            v_indicator_emotion.setBackgroundColor(getResources().getColor(R.color.transparent));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.v_indicator_emotion:
                vp_expression.setCurrentItem(0);
                break;

            case R.id.v_indicator_gif:
                vp_expression.setCurrentItem(1);
                break;
            case R.id.v_delete_emotion:
                if (this.deleteEmotionListener != null) {
                    this.deleteEmotionListener.onExpressionDeleteEmotion();
                }
                break;
        }
    }

    public void setOnExpressionItemClickListener(ExpressionItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnExpressionDeleteEmotionListener(ExpressionDeleteEmotionListener deleteEmotionListener) {
        this.deleteEmotionListener = deleteEmotionListener;
    }

    @Override
    public void onExpressionItemClick(ExpressionBaseModel model) {
        if (this.listener != null) {
            this.listener.onExpressionItemClick(model);
        }
    }


}
