package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.MsgImageEvent;
import me.peiwo.peiwo.util.MsgImageKeeper;

public class MsgAlbumFullScreenActivity extends BaseActivity {

    public static String IMG_URL_LIST = "img_url_list";
    public static String INIT_POSITION = "init_position";
    private List<String> mImgUrlList = new ArrayList<>();
    private int MAX_SIZE = 5;

    @Bind(R.id.album_full_screen_container)
    ViewPager album_full_screen_container;
    @Bind(R.id.album_msg_send)
    TextView album_msg_send;
    @Bind(R.id.album_msg_count)
    TextView album_msg_count;
    @Bind(R.id.album_selected_tag)
    ImageView album_selected_tag;
    @Bind(R.id.btn_back)
    ImageView btn_back;

    private ImageLoader imageLoader;
    private String currentUrl;

    private DisplayImageOptions IMAGE_OPTIONS = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_default_avatar)
            .cacheInMemory(false).cacheOnDisk(false).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_album_full_screen);
        init();
    }

    private void init() {
        imageLoader = ImageLoader.getInstance();
        Intent intent = getIntent();
        ArrayList<String> tempUrlList = intent.getStringArrayListExtra(IMG_URL_LIST);
        tempUrlList.remove(0);
        mImgUrlList = tempUrlList;
        int tempPos = intent.getIntExtra(INIT_POSITION, 0);
        int currentPos = tempPos - 1;
        currentUrl = mImgUrlList.get(currentPos);
        AlbumFullScreenPageAdapter pagerAdapter = new AlbumFullScreenPageAdapter(mImgUrlList);
        album_full_screen_container.setAdapter(pagerAdapter);
        album_full_screen_container.setCurrentItem(currentPos);
        handleSelcetImage();
        updateCount();
        updateTag(mImgUrlList.get(currentPos));

        album_msg_send.setOnClickListener(v -> {
            if (MsgImageKeeper.getInstance().getImgList().size() == 0) {
                MsgImageKeeper.getInstance().add(currentUrl);
            }
            EventBus.getDefault().post(new MsgImageEvent());
            finish();
        });

        btn_back.setOnClickListener(v -> finish());

        album_full_screen_container.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentUrl = mImgUrlList.get(position);
                updateTag(currentUrl);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void handleSelcetImage() {
        album_selected_tag.setOnClickListener(v -> {
            int currentItem = album_full_screen_container.getCurrentItem();
            String currentUrl = mImgUrlList.get(currentItem);
            if (!MsgImageKeeper.getInstance().contains(currentUrl)) {
                if (MsgImageKeeper.getInstance().getImgList().size() >= MAX_SIZE) {
                    Snackbar.make(album_selected_tag, "最多选择5张照片", Snackbar.LENGTH_SHORT).show();
                } else {
                    MsgImageKeeper.getInstance().add(currentUrl);
                    album_selected_tag.setImageResource(R.drawable.image_selected_small_s);
                }
            } else {
                MsgImageKeeper.getInstance().remove(currentUrl);
                album_selected_tag.setImageResource(R.drawable.image_selected_small_n);
            }
            updateCount();
        });
    }

    private void updateTag(String imgUrl) {
        if (MsgImageKeeper.getInstance().contains(imgUrl)) {
            album_selected_tag.setImageResource(R.drawable.image_selected_small_s);
        } else {
            album_selected_tag.setImageResource(R.drawable.image_selected_small_n);
        }
    }

    private void updateCount() {
        int size = MsgImageKeeper.getInstance().getImgList().size();
        if (size > 0) {
            album_msg_count.setVisibility(View.VISIBLE);
            album_msg_count.setText(String.valueOf(size));
        } else {
            album_msg_count.setVisibility(View.INVISIBLE);
        }
    }


    private class AlbumFullScreenPageAdapter extends PagerAdapter {

        private List<String> mImgUrlList = new ArrayList<>();
        private LayoutInflater layoutInflater;


        public AlbumFullScreenPageAdapter(List<String> imgUrlList) {
            if (!imgUrlList.isEmpty())
                mImgUrlList = imgUrlList;
            layoutInflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return mImgUrlList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View containerView = layoutInflater.inflate(R.layout.item_album_view_pager, container, false);
            ImageView album_full_screen_img = (ImageView) containerView.findViewById(R.id.album_full_screen_img);
            String imgUrl = mImgUrlList.get(position);
            String wrapUrl = ImageDownloader.Scheme.FILE.wrap(imgUrl);
            imageLoader.displayImage(wrapUrl, album_full_screen_img, IMAGE_OPTIONS);
            ((ViewPager) container).addView(containerView, 0);
            return containerView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }


}
