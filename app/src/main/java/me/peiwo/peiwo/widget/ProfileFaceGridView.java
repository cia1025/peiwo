package me.peiwo.peiwo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.ImageModel;
import me.peiwo.peiwo.util.PWUtils;

import java.util.List;

/**
 * Created by Dong Fuhai on 2014-07-17 17:52.
 *
 * @modify:
 */
public class ProfileFaceGridView extends LinearLayout implements View.OnClickListener {

    private ImageLoader imageLoader;

    private static final DisplayImageOptions OPTIONS = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.ic_default_avatar)
            .showImageForEmptyUri(R.drawable.ic_default_avatar)
            .showImageOnFail(R.drawable.ic_default_avatar).cacheInMemory(true)
            .cacheOnDisk(true)
            .build();

    public ProfileFaceGridView(Context context) {
        super(context);
        init();
    }

    public ProfileFaceGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        //setBackgroundColor(Color.parseColor("#F0F0F0"));
        int pad = PWUtils.getPXbyDP(getContext(), 10);
        setPadding(pad, pad, pad, pad);
        imageLoader = ImageLoader.getInstance();
    }

    public void displayImages(List<ImageModel> mList) {
        int sw = getResources().getDisplayMetrics().widthPixels;
        int minus_w = PWUtils.getPXbyDP(getContext(), 10 * 2);
        int gap = PWUtils.getPXbyDP(getContext(), 10);
        int imageW = (sw - minus_w - gap * 3) / 4;
        createUserFaces(mList, imageW, gap);
        //添加图片按钮
        if (mList.size() < 8) {
            ImageView iv = new ImageView(getContext());
            LayoutParams params = new LayoutParams(imageW, imageW);
            if (mList.size() == 4) {
                params.setMargins(0, gap, 0, 0);
            } else {
                params.setMargins(gap, 0, 0, 0);
            }
            iv.setLayoutParams(params);
            iv.setImageResource(R.drawable.bg_add_photo_pressed);
            iv.setTag(-1);
            iv.setOnClickListener(this);
            if (mList.size() < 4) {
                ((LinearLayout) getChildAt(0)).addView(iv);
            } else if (mList.size() < 8) {
                ((LinearLayout) getChildAt(1)).addView(iv);
            }
        }

    }

    private void createUserFaces(List<ImageModel> mList, int imageW, int gap) {
        LinearLayout ll_container1 = new LinearLayout(getContext());
        LinearLayout ll_container2 = new LinearLayout(getContext());
        for (int i = 0; i < mList.size(); i++) {
            if (i <= 3) {
                //绘制第一行
                //ll_container1 = new LinearLayout(getContext());
                ll_container1.setOrientation(HORIZONTAL);
                ll_container1.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                ll_container1.addView(createImageChild(i, imageW, mList.get(i), gap, i != 0));
            } else {
                //绘制第2行
                //ll_container2 = new LinearLayout(getContext());
                ll_container2.setOrientation(HORIZONTAL);
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                params.setMargins(0, gap, 0, 0);
                ll_container2.setLayoutParams(params);
                ll_container2.addView(createImageChild(i, imageW, mList.get(i), gap, i != 4));

            }
        }
        addView(ll_container1, 0);
        addView(ll_container2, 1);
    }

    public void displayImagesForUserInfo(List<ImageModel> mList) {
        removeAllViews();
        int sw = getResources().getDisplayMetrics().widthPixels;
        int minus_w = PWUtils.getPXbyDP(getContext(), 8 * 2);
        int gap = PWUtils.getPXbyDP(getContext(), 8);
        int imageW = (sw - minus_w - gap * 3) / 4;
        createUserFaces(mList, imageW, gap);
    }

    private View createImageChild(int i, int size, ImageModel model, int gap, boolean needMargin) {
        ImageView iv = new ImageView(getContext());
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LayoutParams params = new LayoutParams(size, size);
        if (needMargin) {
            params.setMargins(gap, 0, 0, 0);
        }
        iv.setLayoutParams(params);
        iv.setTag(i);
        iv.setOnClickListener(this);
        imageLoader.displayImage(model.thumbnail_url, iv, OPTIONS);
        return iv;
    }

    private OnImgItemClickListener onImgItemClickListener;

    public void setOnImgItemClickListener(OnImgItemClickListener onImgItemClickListener) {
        this.onImgItemClickListener = onImgItemClickListener;
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag instanceof Integer) {
            int index = (Integer) tag;
            if (onImgItemClickListener != null) {
                onImgItemClickListener.onImgItemClick(index);
            }
        }
    }

    public void reload(List<ImageModel> images) {
        removeAllViews();
        displayImages(images);
    }

    public interface OnImgItemClickListener {
        public void onImgItemClick(int index);
    }
}
