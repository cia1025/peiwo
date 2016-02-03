package me.peiwo.peiwo.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.EditText;
import me.peiwo.peiwo.model.EmotionModel;
import me.peiwo.peiwo.util.PWUtils;

/**
 * Created by fuhaidong on 15/11/19.
 */
public class EmotionEditText extends EditText {
    private int emotion_size;

    public EmotionEditText(Context context) {
        super(context);
        init();
    }

    public EmotionEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EmotionEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        emotion_size = PWUtils.getFaceSizeFromScreen(getContext());
    }

    public void setTextEmotion(EmotionModel model) {
        Drawable drawable = getResources().getDrawable(model.res_id);
        if (drawable != null) {
            SpannableStringBuilder builder = new SpannableStringBuilder(model.regular);
            drawable.setBounds(0, 0, emotion_size, emotion_size);
            ImageSpan imageSpan = new ImageSpan(drawable);
            builder.setSpan(imageSpan, 0, model.regular.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            getText().insert(getSelectionStart(), builder);
        }
    }

}
