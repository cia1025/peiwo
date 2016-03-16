package me.peiwo.peiwo.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.ImageModel;
import me.peiwo.peiwo.model.ImageModelKeeper;
import me.peiwo.peiwo.util.PWUtils;

public class FeedFlowGridView extends LinearLayout implements View.OnClickListener {

    private final int paddingHorizontalDP = 14;
    private final int spaceDP = 7;
    private int spacePX = 0;
    private int paddingHorizontalPX = 0;

    private static final DisplayImageOptions OPTIONS = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.color.feed_flow_picture_background)
            .showImageForEmptyUri(R.color.feed_flow_picture_background)
            .showImageOnFail(R.color.feed_flow_picture_background).considerExifParams(true)
            .cacheInMemory(true).cacheOnDisk(true).build();
    private ImageModelKeeper imageModelKeeper;

    public FeedFlowGridView(Context context) {
        super(context);
        init();
    }

    public FeedFlowGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        spacePX = PWUtils.getPXbyDP(getContext(), spaceDP);//21
        paddingHorizontalPX = PWUtils.getPXbyDP(getContext(), paddingHorizontalDP); // 42
        imageModelKeeper = ImageModelKeeper.getInstance();
    }

    private void createUserFaces(List<ImageModel> mList, int imageW) {
        LinearLayout ll_container1 = null;
        LinearLayout ll_container2 = null;
        LinearLayout ll_container3 = null;
        int imageNums = mList.size();
        for (int i = 0; i < imageNums; i++) {
            if (i < 2 || (i == 2 && !has4Items(imageNums))) {
                // 绘制第一行
                if (ll_container1 == null) {
                    ll_container1 = new LinearLayout(getContext());
                }
                ll_container1.setOrientation(HORIZONTAL);
                ll_container1.setLayoutParams(new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
                ll_container1.addView(createImageChild(i, imageW, mList.get(i)));
            } else if (i <= 5) {
                // 绘制第2行
                if (ll_container2 == null) {
                    ll_container2 = new LinearLayout(getContext());
                }
                ll_container2.setOrientation(HORIZONTAL);
                LayoutParams params = new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT);
                params.setMargins(0, spacePX, 0, 0);
                ll_container2.setLayoutParams(params);
                View imageChild = createImageChild(i, imageW, mList.get(i));
                LayoutParams lp = (LayoutParams) imageChild.getLayoutParams();
                if ((i == 2 && has4Items(imageNums)) || (i == 3 && !has4Items(imageNums))) {
                    lp.setMargins(0, 0, 0, 0);
                } else {
                    lp.setMargins(spacePX, 0, 0, 0);
                }
                ll_container2.addView(imageChild);

            } else {
                // 绘制第三行
                if (ll_container3 == null) {
                    ll_container3 = new LinearLayout(getContext());
                }
                ll_container3.setOrientation(HORIZONTAL);
                LayoutParams params = new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT);
                params.setMargins(0, spacePX, 0, 0);
                ll_container3.setLayoutParams(params);
                ll_container3.addView(createImageChild(i, imageW, mList.get(i)));
            }
        }
        if (ll_container1 != null) {
            addView(ll_container1, 0);
        }
        if (ll_container2 != null) {
            addView(ll_container2, 1);
        }
        if (ll_container3 != null) {
            addView(ll_container3, 2);
        }
    }

    private boolean has4Items(int imageNums) {
        if (imageNums == 4)
            return true;
        return false;
    }

    public void displayImages(List<ImageModel> mList, int imageWidth, int imageHeight) {
        if (mList == null || mList.size() == 0)
            return;
        removeAllViews();

        if (mList.size() == 1) {
            displaySingleImage(mList, imageWidth, imageHeight);
            return;
        }

        int sw = getResources().getDisplayMetrics().widthPixels;
        int minus_w = paddingHorizontalPX * 2 + spacePX * 2; // 48
        int imageW = (sw - minus_w) / 3;   //335
        createUserFaces(mList, imageW);
    }

    public void displaySingleImage(List<ImageModel> mList, int imageWidth, int imageHeight) {
        removeAllViews();
        if (imageWidth == 0 || imageHeight == 0) {
            return;
        }
        int sw = getResources().getDisplayMetrics().widthPixels;
        int minus_w = paddingHorizontalPX * 2;
        int imageMax = (int) ((sw - minus_w) * 0.65);   //335

        int imageW = 0;
        int imageH = 0;

        ScaleType scaleType = ScaleType.FIT_XY;
        if (imageWidth >= imageHeight) {
            //横图
            if (imageWidth >= 3000 && imageWidth / imageHeight >= 6) {
                scaleType = ScaleType.CENTER_CROP;
                imageW = imageMax;
                imageH = imageMax;
            } else {
                double ratio = (double) imageMax / (double) imageWidth;
                imageW = imageMax;
                imageH = (int) (imageHeight * ratio);
            }
        } else {
            //竖图
            if (imageHeight >= 3000 && imageHeight / imageWidth >= 6) {
                scaleType = ScaleType.CENTER_CROP;
                imageW = imageMax;
                imageH = imageMax;
            } else {
                double ratio = (double) imageMax / (double) imageHeight;
                imageW = (int) (imageWidth * ratio);
                imageH = imageMax;
            }
        }

        ImageView iv = new ImageView(getContext());
        iv.setScaleType(scaleType);
//        iv.setAdjustViewBounds(true);
        LayoutParams params = new LayoutParams(imageW, imageH);
        params.setMargins(0, 0, 0, 0);
        iv.setLayoutParams(params);
        iv.setOnClickListener(this);
        iv.setTag(0);

        ImageModel model = mList.get(0);
        if (imageModelKeeper.getUrlList().contains(model.image_url)) {
            ImageLoader.getInstance().displayImage(model.image_url, iv, OPTIONS);
        } else {
            if (!TextUtils.isEmpty(model.thumbnail_url)) {
                ImageLoader.getInstance().displayImage(model.thumbnail_url, iv, OPTIONS);
            } else {
                ImageLoader.getInstance().displayImage(model.image_url, iv, OPTIONS);
            }
        }
//        if (!TextUtils.isEmpty(model.thumbnail_url)) {
//            ImageLoader.getInstance().displayImage(model.thumbnail_url, iv, OPTIONS);
//        } else {
//            ImageLoader.getInstance().displayImage(model.image_url, iv, OPTIONS);
//        }
        addView(iv);
    }

    private View createImageChild(int i, int size, ImageModel model) {

        ImageView iv = new ImageView(getContext());
        iv.setScaleType(ScaleType.CENTER_CROP);
//        iv.setAdjustViewBounds(true);
        LayoutParams params = new LayoutParams(size, size);
        if (i % 3 == 0) {
            params.setMargins(0, 0, 0, 0);
        } else {
            params.setMargins(spacePX, 0, 0, 0);
        }
        iv.setLayoutParams(params);
        iv.setTag(i);
        iv.setOnClickListener(this);
        if (imageModelKeeper.getUrlList().contains(model.image_url)) {
            ImageLoader.getInstance().displayImage(model.image_url, iv, OPTIONS);
        } else {
            if (!TextUtils.isEmpty(model.thumbnail_url)) {
                ImageLoader.getInstance().displayImage(model.thumbnail_url, iv, OPTIONS);
            } else {
                ImageLoader.getInstance().displayImage(model.image_url, iv, OPTIONS);
            }
        }
//        if (!TextUtils.isEmpty(model.thumbnail_url)) {
//            ImageLoader.getInstance().displayImage(model.thumbnail_url, iv, OPTIONS);
//        } else {
//            ImageLoader.getInstance().displayImage(model.image_url, iv, OPTIONS);
//        }
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

    public interface OnImgItemClickListener {
        public void onImgItemClick(int index);
    }
}
