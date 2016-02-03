/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package me.peiwo.peiwo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.callback.DownloadCallback;
import me.peiwo.peiwo.model.ImageModel;
import me.peiwo.peiwo.net.PWDownloader;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.FileManager;
import me.peiwo.peiwo.util.Md5Util;
import me.peiwo.peiwo.widget.TouchImageView;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImagePagerActivity extends BaseActivity {

    public static final String KEY_URL_LIST = "url_list";
    public static final String KEY_POS = "pos";

    private static final String STATE_POSITION = "STATE_POSITION";
    private ViewPager pager;
    private TextView tvTitle;
    private List<ImageModel> urlList;
    private CompositeSubscription mSubscriptions;
    private View iv_save_img;

    private DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_default_avatar)
            .cacheInMemory(false).cacheOnDisk(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_image_pager);
        mSubscriptions = new CompositeSubscription();

        Intent intent = getIntent();
        //Bundle bundle = getIntent().getExtras();

//        List<ImageModel> urlList = (List<ImageModel>) bundle
//                .getSerializable(KEY_URL_LIST);
        urlList = intent.getParcelableArrayListExtra(KEY_URL_LIST);
        //int pagerPosition = bundle.getInt(KEY_POS, 0);
        int pagerPosition = intent.getIntExtra(KEY_POS, 0);
        if (savedInstanceState != null) {
            pagerPosition = savedInstanceState.getInt(STATE_POSITION);
        }
        iv_save_img = findViewById(R.id.iv_save_img);
        tvTitle = (TextView) findViewById(R.id.title);
        tvTitle.setText(String.format("%d/%d", pagerPosition + 1, urlList.size()));

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new ImagePagerAdapter(urlList));
        pager.setCurrentItem(pagerPosition);
        pager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int index) {
                tvTitle.setText(String.format("%d/%d", index + 1, urlList.size()));
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_POSITION, pager.getCurrentItem());
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private List<ImageModel> images;
        private LayoutInflater inflater;

        ImagePagerAdapter(List<ImageModel> images) {
            this.images = images;
            inflater = getLayoutInflater();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }

        @Override
        public void finishUpdate(View container) {
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object instantiateItem(ViewGroup view, final int position) {
            View imageLayout = inflater.inflate(R.layout.item_pager_image,
                    view, false);
            final TouchImageView imageView = (TouchImageView) imageLayout
                    .findViewById(R.id.image);
//            final ProgressBar spinner = (ProgressBar) imageLayout
//                    .findViewById(R.id.loading);

            String image_url = images.get(position).image_url;
            CustomLog.d("instantiateItem image_url : " + image_url);

            ImageLoader.getInstance().displayImage(image_url, imageView, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
                    iv_save_img.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    iv_save_img.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });

            ((ViewPager) view).addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View container) {
        }
    }

    @Override
    public void click(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_save_img:
                saveImage();
                break;

            default:
                break;
        }
    }

    private void saveImage() {
        ImageModel imageModel = urlList.get(pager.getCurrentItem());
        String path = imageModel.image_url;
        if (path.startsWith("http")) {
            File file = DiskCacheUtils.findInCache(path, ImageLoader.getInstance().getDiskCache());
            if (file != null && file.exists() && file.length() > 0) {
                copyChatImages(file);
            } else {
                //Toast.makeText(ImagePagerActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "正在保存...", Toast.LENGTH_SHORT).show();
                File dst = new File(FileManager.getChatImageCopyPath(), String.format("%s.jpg", Md5Util.getMd5code(path)));
                PWDownloader.getInstance().add(path, dst, new DownloadCallback() {
                    @Override
                    public void onComplete(String path) {
                        Toast.makeText(ImagePagerActivity.this, "图片已保存在" + path, Toast.LENGTH_SHORT).show();
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
                    }

                    @Override
                    public void onFailure(String path, IOException e) {
                        Toast.makeText(ImagePagerActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            File file = new File(path);
            copyChatImages(file);
        }
    }

    private void copyChatImages(File src) {
        Toast.makeText(this, "正在保存...", Toast.LENGTH_SHORT).show();
        File dst = new File(FileManager.getChatImageCopyPath(), String.format("%s.jpg", Md5Util.getMd5code(src.getAbsolutePath())));
        if (dst.exists() && dst.length() > 0) {
            Toast.makeText(ImagePagerActivity.this, "图片已存在" + dst.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            return;
        }
        Observable<Boolean> observable = FileManager.copyFile(src, dst);
        Subscription subscription = observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(ImagePagerActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    String rst_path = dst.getAbsolutePath();
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + rst_path)));
                    Toast.makeText(ImagePagerActivity.this, "图片已保存在" + rst_path, Toast.LENGTH_SHORT).show();
                }
            }
        });
        mSubscriptions.add(subscription);
    }


    @Override
    public void finish() {
        if (mSubscriptions != null && !mSubscriptions.isUnsubscribed()) {
            mSubscriptions.unsubscribe();
        }
        super.finish();
    }
}
