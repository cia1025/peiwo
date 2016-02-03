package me.peiwo.peiwo.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.TextView;
import me.peiwo.peiwo.model.EmotionModel;
import me.peiwo.peiwo.util.group.ExpressionData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fuhaidong on 15/12/15.
 */
public class PWTextViewCompat extends TextView {
    private Pattern pattern = Pattern.compile("\\[[^\\]]+\\]");
    private ExpressionData expressionData;

    public PWTextViewCompat(Context context) {
        super(context);
        init();
    }

    public PWTextViewCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PWTextViewCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        expressionData = ExpressionData.getInstance(getContext());
    }

    public void setTextCompat(String source, int bounds) {
        SpannableString spannableString = new SpannableString(source);
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            String group = matcher.group();
            if (expressionData.getEmotionMappingData().containsKey(group)) {
                EmotionModel model = expressionData.getEmotionMappingData().get(group);
                Drawable drawable = getResources().getDrawable(model.res_id);
                if (drawable == null) {
                    continue;
                }
                drawable.setBounds(0, 0, bounds, bounds);
                ImageSpan imageSpan = new ImageSpan(drawable);
                spannableString.setSpan(imageSpan, matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        setText(spannableString);
    }
}
