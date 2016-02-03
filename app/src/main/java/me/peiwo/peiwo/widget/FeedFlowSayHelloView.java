package me.peiwo.peiwo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.FeedFlowModel.FeedFlowLikersModel;

import java.util.List;

public class FeedFlowSayHelloView extends LinearLayout implements View.OnClickListener {

    private ImageLoader imageLoader;
    private onTextViewClickListener onTextViewClickListener;
    private onImageViewViewClickListener onImageViewViewClickListener;

    public FeedFlowSayHelloView(Context context) {
        super(context);
        init();
    }

    public FeedFlowSayHelloView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER_VERTICAL);
        imageLoader = ImageLoader.getInstance();
    }

    public void disPlayTextView(Context context) {
        findViewById(R.id.ll_ufaces).setVisibility(View.GONE);
        TextView tv_hello = (TextView) findViewById(R.id.tv_hello);
        tv_hello.setVisibility(View.VISIBLE);
        tv_hello.setOnClickListener(this);
        tv_hello.setTag(1);
    }

    public void disPlayUserFaces(Context context, List<FeedFlowLikersModel> imageList) {
        findViewById(R.id.tv_hello).setVisibility(View.GONE);
        if (imageList == null) {
            findViewById(R.id.ll_ufaces).setVisibility(View.GONE);
            return;
        }
        findViewById(R.id.ll_ufaces).setVisibility(View.VISIBLE);

        ImageView iv_first = (ImageView) findViewById(R.id.iv_first);
        ImageView iv_second = (ImageView) findViewById(R.id.iv_second);
        ImageView iv_third = (ImageView) findViewById(R.id.iv_third);
        iv_first.setOnClickListener(this);
        iv_first.setTag(2);
        iv_second.setOnClickListener(this);
        iv_second.setTag(2);
        iv_third.setOnClickListener(this);
        iv_third.setTag(2);

        if (imageList != null && imageList.size() > 0) {
            if (imageList.size() == 1) {
                imageLoader.displayImage(imageList.get(0).avatar, iv_first);
                iv_first.setVisibility(View.VISIBLE);
                iv_second.setVisibility(View.GONE);
                iv_third.setVisibility(View.GONE);
                return;
            } else if (imageList.size() == 2) {
                imageLoader.displayImage(imageList.get(0).avatar, iv_first);
                imageLoader.displayImage(imageList.get(1).avatar, iv_second);
                iv_first.setVisibility(View.VISIBLE);
                iv_second.setVisibility(View.VISIBLE);
                iv_third.setVisibility(View.GONE);
                return;
            } else if (imageList.size() == 3) {
                imageLoader.displayImage(imageList.get(0).avatar, iv_first);
                imageLoader.displayImage(imageList.get(1).avatar, iv_second);
                imageLoader.displayImage(imageList.get(2).avatar, iv_third);
                iv_first.setVisibility(View.VISIBLE);
                iv_second.setVisibility(View.VISIBLE);
                iv_third.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface onTextViewClickListener {
        public void onTextViewClick();
    }

    public interface onImageViewViewClickListener {
        public void onImageViewClick();
    }

    public void setOnImageViewViewClickListener(onImageViewViewClickListener onImageViewViewClickListener) {
        this.onImageViewViewClickListener = onImageViewViewClickListener;
    }

    public void setOnTextViewClickListener(onTextViewClickListener onTextViewClickListener) {
        this.onTextViewClickListener = onTextViewClickListener;
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag instanceof Integer) {
            int index = (Integer) tag;
            if (index == 1) {
                if (onTextViewClickListener != null) {
                    onTextViewClickListener.onTextViewClick();
                }
            } else if (index == 2) {
                if (onImageViewViewClickListener != null) {
                    onImageViewViewClickListener.onImageViewClick();
                }
            }

        }
    }
}
