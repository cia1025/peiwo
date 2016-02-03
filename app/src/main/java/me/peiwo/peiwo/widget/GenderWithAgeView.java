package me.peiwo.peiwo.widget;

import android.graphics.Color;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.util.PWUtils;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * Created by fuhaidong on 14/10/21.
 */
public class GenderWithAgeView extends TextView {
    public GenderWithAgeView(Context context) {
        super(context);
    }

    public GenderWithAgeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GenderWithAgeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void displayGenderWithAge(int gender, String age) {
        int gap = PWUtils.getPXbyDP(getContext(), 2);
        int pad = PWUtils.getPXbyDP(getContext(), 5);
        setCompoundDrawablePadding(pad);
        //setPadding(gap, 0, gap, 0);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        if (gender == AsynHttpClient.GENDER_MASK_MALE) {
            //setBackgroundResource(R.drawable.bg_gender_m);
            setCompoundDrawables(getCompoundDrawable(R.drawable.ic_gender_m), null, null, null);
        } else if (gender == AsynHttpClient.GENDER_MASK_FEMALE) {
            //setBackgroundResource(R.drawable.bg_gender_f);
            setCompoundDrawables(getCompoundDrawable(R.drawable.ic_gender_f), null, null, null);
        }
        //setTextColor(getResources().getColor(R.color.c_white));
        setTextColor(Color.parseColor("#4d4d4d"));
        setText(age);

    }

    private Drawable getCompoundDrawable(int resId) {
        Drawable iconDrawable = getResources().getDrawable(resId);
        iconDrawable.setBounds(0, 0, iconDrawable.getMinimumWidth(),
                iconDrawable.getMinimumHeight());
        return iconDrawable;
    }
}
