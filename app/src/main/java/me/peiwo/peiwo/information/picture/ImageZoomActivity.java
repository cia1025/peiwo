package me.peiwo.peiwo.information.picture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.ImageItem;

import java.util.ArrayList;
import java.util.List;

public class ImageZoomActivity extends Activity implements OnClickListener {
    private static final String STATE_POSITION = "STATE_POSITION";
    public static final String K_CURR_LOCATION = "curr_location";
    public static final String EXTRA_IMAGE_LIST = "image_list";
    private ViewPager pager;
    private MyPageAdapter adapter;
    private int currentPosition;
    private ArrayList<ImageItem> mDataList = new ArrayList<>();
    private TextView countTV;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_zoom);
        countTV = (TextView) findViewById(R.id.title);
        int pagerPosition = 0;
        if (savedInstanceState != null) {
            pagerPosition = savedInstanceState.getInt(STATE_POSITION);
        }
        countTV.setText(getPageIndexShow(pagerPosition));
        ImageView mPhotoDel = (ImageView) findViewById(R.id.photo_bt_del);
        mPhotoDel.setOnClickListener(this);
        TextView mBackTV = (TextView) findViewById(R.id.btn_left);
        mBackTV.setOnClickListener(this);

        Intent intent = getIntent();
        currentPosition = intent.getIntExtra(K_CURR_LOCATION, 0);
        ArrayList<ImageItem> data = intent.getParcelableArrayListExtra(EXTRA_IMAGE_LIST);
        mDataList.addAll(data);
        pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setOnPageChangeListener(pageChangeListener);
        adapter = new MyPageAdapter(mDataList);
        pager.setAdapter(adapter);
        pager.setCurrentItem(currentPosition);
        //ImageFetcher.getInstance().getDeleteList().clear();
    }


    public String getPageIndexShow(int curPos) {
        return (curPos + 1) + "/" + mDataList.size();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_POSITION, pager.getCurrentItem());
    }

    private void removeImgs() {
        mDataList.clear();
    }

    private void removeImg(int location) {
        if (location + 1 <= mDataList.size()) {
            mDataList.remove(location);
        }
    }

    private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

        public void onPageSelected(int arg0) {
            currentPosition = arg0;
            countTV.setText(getPageIndexShow(arg0));
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
            countTV.setText(getPageIndexShow(arg0));
        }

        public void onPageScrollStateChanged(int arg0) {

        }
    };

    class MyPageAdapter extends PagerAdapter {
        private List<ImageItem> dataList = new ArrayList<>();
        private ArrayList<ImageView> mViews = new ArrayList<ImageView>();

        public MyPageAdapter(ArrayList<ImageItem> dataList) {
            this.dataList = dataList;
            int size = dataList.size();
            for (int i = 0; i != size; i++) {
                ImageView iv = new ImageView(ImageZoomActivity.this);
                iv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                String path = "file://" + dataList.get(i).thumbnailPath;
                ImageLoader.getInstance().displayImage(path, iv);
                mViews.add(iv);
            }
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public Object instantiateItem(View arg0, int arg1) {
            ImageView iv = mViews.get(arg1);
            ((ViewPager) arg0).addView(iv);
            return iv;
        }

        public void destroyItem(View arg0, int arg1, Object arg2) {
            if (mViews.size() >= arg1 + 1) {
                ((ViewPager) arg0).removeView(mViews.get(arg1));
            }
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        public void removeView(int position) {
            if (position + 1 <= mViews.size()) {
                mViews.remove(position);
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.photo_bt_del:
                //ImageFetcher.getInstance().getDeleteList().add(mDataList.get(currentPosition));
                if (mDataList.size() == 1) {
                    removeImgs();
                    finish();
                } else {
                    removeImg(currentPosition);
                    pager.removeAllViews();
                    adapter.removeView(currentPosition);
                    adapter.notifyDataSetChanged();
                    countTV.setText(getPageIndexShow(currentPosition));
                }
                break;
            case R.id.btn_left:
                finish();
            default:
                break;
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_IMAGE_LIST, mDataList);
        setResult(RESULT_OK, intent);
        super.finish();
    }
}