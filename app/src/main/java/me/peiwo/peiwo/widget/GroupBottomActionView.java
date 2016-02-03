package me.peiwo.peiwo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import me.peiwo.peiwo.R;

/**
 * Created by fuhaidong on 15/12/9.
 */
public class GroupBottomActionView extends FrameLayout {
    private ExpressionPanelView v_express_panel;
    private ImageQuickSwitchView v_image_quick_switch_panel;

    private boolean consuming = false;

    public GroupBottomActionView(Context context) {
        super(context);
        init();
    }

    public GroupBottomActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GroupBottomActionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        post(() -> {
            v_express_panel = (ExpressionPanelView) findViewById(R.id.v_express_panel);
            v_image_quick_switch_panel = (ImageQuickSwitchView) findViewById(R.id.v_image_quick_switch_panel);
        });
    }

    public ImageQuickSwitchView getImageQuickSwitchView() {
        return v_image_quick_switch_panel;
    }

    public ExpressionPanelView getExpressionPanelView() {
        return v_express_panel;
    }


    public void showExpressionView() {
        if (v_express_panel != null) {
            v_express_panel.setVisibility(VISIBLE);
        }
        if (v_image_quick_switch_panel != null) {
            v_image_quick_switch_panel.setVisibility(GONE);
        }
    }

    public void showImageQuickSwitchView() {
        if (v_image_quick_switch_panel != null) {
            v_image_quick_switch_panel.setVisibility(VISIBLE);
        }
        if (v_express_panel != null) {
            v_express_panel.setVisibility(GONE);
        }
    }

    public boolean isExpressionShown() {
        return this.getVisibility() == VISIBLE && v_express_panel.getVisibility() == VISIBLE && v_image_quick_switch_panel.getVisibility() == GONE;
    }

    public void setConsuming(boolean b) {
        consuming = b;
    }

    //消费了事件，不向下传递
    public boolean consumingEvents() {
        return consuming;
    }

    public boolean isImageQuickSwitchShown() {
        return this.getVisibility() == VISIBLE && v_image_quick_switch_panel.getVisibility() == VISIBLE && v_express_panel.getVisibility() == GONE;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            if (v_image_quick_switch_panel != null) {
                v_image_quick_switch_panel.setVisibility(GONE);
            }
            if (v_express_panel != null) {
                v_express_panel.setVisibility(GONE);
            }
        }
    }
}
