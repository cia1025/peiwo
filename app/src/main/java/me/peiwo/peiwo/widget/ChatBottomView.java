package me.peiwo.peiwo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.util.EmotionInputDetector;
import me.peiwo.peiwo.util.MsgImageKeeper;

/**
 * Created by fuhaidong on 15/12/2.
 */
public class ChatBottomView extends FrameLayout {
    private View face_lay;
    private View image_quick_switch;
    private View txtIndiView;
    private View expressIndiView;
    private View imageQuickIndiView;

    public ChatBottomView(Context context) {
        super(context);
        init();
    }

    public ChatBottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatBottomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        post(() -> {
            face_lay = findViewById(R.id.face_lay);
            image_quick_switch = findViewById(R.id.image_quick_switch);
        });
    }

    public ChatBottomView bindTxtIndiView(View view) {
        this.txtIndiView = view;
        return this;
    }

    public ChatBottomView bindExpressIndiView(View view) {
        this.expressIndiView = view;
        return this;
    }

    public ChatBottomView bindImageQuickIndiView(View view) {
        this.imageQuickIndiView = view;
        return this;
    }


    public boolean isEmotionShown() {
        return face_lay.getVisibility() == VISIBLE;
    }

    public boolean isImageShown() {
        return image_quick_switch.getVisibility() == VISIBLE;
    }

    public void showEmotionView() {
        image_quick_switch.setVisibility(GONE);
        face_lay.setVisibility(VISIBLE);
        changeViewStateExpress();
    }

    public void showImageQuickView() {
        face_lay.setVisibility(GONE);
        image_quick_switch.setVisibility(VISIBLE);
        changeViewStateImage();
    }


    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            Object tag = getTag();
            if (tag != null) {
                int tagi = (int) tag;
                if (tagi == EmotionInputDetector.TAG_EMOTION) {
                    image_quick_switch.setVisibility(GONE);
                    face_lay.setVisibility(VISIBLE);
                    changeViewStateExpress();
                } else if (tagi == EmotionInputDetector.TAG_IMAGE) {
                    face_lay.setVisibility(GONE);
                    image_quick_switch.setVisibility(VISIBLE);
                    changeViewStateImage();
                }
            }
//            if (face_lay.getVisibility() == VISIBLE) {
//                face_lay.setVisibility(GONE);
//                image_quick_switch.setVisibility(VISIBLE);
//                changeViewStateImage();
//            } else {
//                image_quick_switch.setVisibility(GONE);
//                face_lay.setVisibility(VISIBLE);
//                changeViewStateExpress();
//            }
        } else {
            changeViewStateNomal();
            face_lay.setVisibility(GONE);
            image_quick_switch.setVisibility(GONE);
            setTag(null);
        }
    }

    private void changeViewStateNomal() {
        if (this.txtIndiView != null) {
            this.txtIndiView.setSelected(true);
            MsgImageKeeper.getInstance().clear();
        }
        if (this.imageQuickIndiView != null) {
            this.imageQuickIndiView.setSelected(false);
        }
        if (this.expressIndiView != null) {
            this.expressIndiView.setSelected(false);
        }
    }

    private void changeViewStateExpress() {
        if (this.txtIndiView != null) {
            this.txtIndiView.setSelected(false);
        }
        if (this.imageQuickIndiView != null) {
            this.imageQuickIndiView.setSelected(false);
        }
        if (this.expressIndiView != null) {
            this.expressIndiView.setSelected(true);
        }
    }

    private void changeViewStateImage() {
        if (this.txtIndiView != null) {
            this.txtIndiView.setSelected(false);
        }
        if (this.imageQuickIndiView != null) {
            this.imageQuickIndiView.setSelected(true);
        }
        if (this.expressIndiView != null) {
            this.expressIndiView.setSelected(false);
        }
    }
}
