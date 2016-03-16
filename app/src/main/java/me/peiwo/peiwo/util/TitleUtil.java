package me.peiwo.peiwo.util;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import me.peiwo.peiwo.R;

/**
 * Created by fuhaidong on 14/10/27.
 */
public class TitleUtil {
    public static void setTitleBar(Activity a, int strId, View.OnClickListener left,
                                   View.OnClickListener right) {
        setTitleBar(a, a.getString(strId), left, right);
    }

    public static void setTitleBar(Activity a, String title,
                                   View.OnClickListener left, View.OnClickListener right) {
        setTitleBar(a.getWindow().getDecorView(), title, left, right);
    }

    public static void setTitleBar(View a, String title, View.OnClickListener left,
                                   View.OnClickListener right) {
        setTitleBar(a, title, left, null, right);
    }

    public static void setTitleBar(Activity a, String title,
                                   View.OnClickListener left, String rightText, View.OnClickListener right) {
        setTitleBar(a.getWindow().getDecorView(), title, left, rightText, right);
    }

    public static void setTitleBar(View a, String title, View.OnClickListener left,
                                   String rightText, View.OnClickListener right) {
        TextView tv = (TextView) a.findViewById(R.id.title);
        tv.setText(title);
        View view_left = a.findViewById(R.id.view_left);
        view_left.setVisibility(left != null ? View.VISIBLE : View.GONE);
        view_left.setOnClickListener(left);
        TextView btnRight = (TextView) a.findViewById(R.id.btn_right);
        btnRight.setVisibility(right != null ? View.VISIBLE : View.GONE);
        btnRight.setOnClickListener(right);
//        if (!TextUtils.isEmpty(rightText))
//            btnRight.setText(rightText);
        if(!TextUtils.isEmpty(rightText)){
            btnRight.setText(rightText);
        }else{
            btnRight.setText("");
        }
    }

    public static void setTitleBar(Activity a, String title, View.OnClickListener left,
                                   int drawableId, View.OnClickListener right) {
        TextView tv = (TextView) a.findViewById(R.id.title);
        tv.setText(title);
        View view_left = a.findViewById(R.id.view_left);
        view_left.setVisibility(left != null ? View.VISIBLE : View.GONE);
        view_left.setOnClickListener(left);
        ImageView btnRight = (ImageView) a.findViewById(R.id.iv_right);
        btnRight.setImageResource(drawableId);
        btnRight.setVisibility(right != null ? View.VISIBLE : View.GONE);
        btnRight.setOnClickListener(right);
    }
}
