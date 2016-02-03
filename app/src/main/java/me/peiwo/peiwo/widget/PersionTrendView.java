package me.peiwo.peiwo.widget;

import java.util.List;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.util.PWUtils;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.ImageLoader;

public class PersionTrendView extends LinearLayout {

    private ImageLoader imageLoader;

    public PersionTrendView(Context context) {
        super(context);
        init();
    }

    public PersionTrendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        int pad = PWUtils.getPXbyDP(getContext(), 2);
        setPadding(pad, pad, pad, pad);
        imageLoader = ImageLoader.getInstance();
    }


    private void createUserFaces(List<String> mList, int imageW, int gap) {
        LinearLayout ll_container1 = new LinearLayout(getContext());
        for (int i = 0; i < mList.size(); i++) {
			if (i > 2) {
				break;
			}
            ll_container1.setOrientation(HORIZONTAL);
            ll_container1.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            ll_container1.addView(createImageChild(i, imageW, mList.get(i), gap));
        }
        addView(ll_container1, 0);
    }

    public void displayImagesForUserInfo(List<String> mList) {
        removeAllViews();
        /** 获取控件的宽度  */
		int sw = getResources().getDisplayMetrics().widthPixels;
		int paddingHorizontalDP = 12 * 2 + 80 + 20;
		int paddingHorizontalPX = PWUtils.getPXbyDP(getContext(), paddingHorizontalDP); // 42
		int spacePX = PWUtils.getPXbyDP(getContext(), 7);
		
		int minus_w = sw - (paddingHorizontalPX + spacePX * 2); // 48
		int imageW = minus_w / 4;   //335
		
        createUserFaces(mList, imageW, spacePX);
    }

    public void displaySingleImage(Context context){
		removeAllViews();
		ImageView iv = new ImageView(getContext());
		iv.setTag(0);
		iv.setImageDrawable(context.getResources().getDrawable(R.drawable.bg_font));
		addView(iv);
	}
    
    private View createImageChild(int i, int size, String url, int gap) {
        ImageView iv = new ImageView(getContext());
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LayoutParams params = new LayoutParams(size, size);
        if (i % 3 == 0) {
            params.setMargins(0, 0, 0, 0);
        } else {
        	params.setMargins(gap, 0, 0, 0);
        }
        iv.setLayoutParams(params);
        iv.setTag(i);
        imageLoader.displayImage(url, iv);
        return iv;
    }
}
