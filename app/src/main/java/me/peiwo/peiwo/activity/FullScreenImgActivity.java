package me.peiwo.peiwo.activity;

import android.text.TextUtils;
import android.widget.ProgressBar;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.widget.TouchImageView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class FullScreenImgActivity extends BaseFragmentActivity {

	//private Bitmap bitmap = null;
	private TouchImageView imageView;
	private MyReceiver mReceiver; 
    public static final String ACTIVITY_FINISH = "finish";
    private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY)
			.considerExifParams(true).build();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.full_screen_image_layout);  
		IntentFilter intentFilter = new IntentFilter(ACTIVITY_FINISH);
		mReceiver = new MyReceiver();
		registerReceiver(mReceiver, intentFilter);
        imageView = (TouchImageView)findViewById(R.id.full_screen_imgview);
		String local_path = getIntent().getExtras().getString("local_path");
        String image_url = getIntent().getExtras().getString("image_url");
        String thumbnail_url = getIntent().getExtras().getString("thumbnail_url");
		CustomLog.i("FullScreenImageActivity. image_url is : "+image_url);
		CustomLog.i("FullScreenImageActivity. local_path is : "+local_path);
		if(TextUtils.isEmpty(local_path))
            ImageLoader.getInstance().displayImage(thumbnail_url, imageView, options, getThumbImageLoadListener(image_url));
		else
			ImageLoader.getInstance().displayImage("file://" + local_path, imageView, options);
	}


	private ImageLoadingListener getThumbImageLoadListener(final String image_url) {
		return new ImageLoadingListener() {
			
			@Override
			public void onLoadingStarted(String arg0, View arg1) {
			}
			
			@Override
			public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
			}
			
			@Override
			public void onLoadingComplete(String arg0, View arg1, Bitmap bm) {
				ImageLoader.getInstance().displayImage(image_url, imageView, options, getImageLoadingListener(), getImageLoadingProgressListener());
			}
			
			@Override
			public void onLoadingCancelled(String arg0, View arg1) {
			}
		};
	}

	private ImageLoadingListener getImageLoadingListener() {
		return new ImageLoadingListener() {
			@Override
			public void onLoadingStarted(String s, View view) {
			}

			@Override
			public void onLoadingFailed(String s, View view, FailReason failReason) {
			}

			@Override
			public void onLoadingComplete(String s, View view, Bitmap bitmap) {

			}

			@Override
			public void onLoadingCancelled(String s, View view) {
			}
		};
	}

	private ImageLoadingProgressListener getImageLoadingProgressListener() {
		return new ImageLoadingProgressListener() {
			@Override
			public void onProgressUpdate(String s, View view, int i, int i1) {

			}
		};
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    finish();
        return super.onTouchEvent(event);  
    }

	class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}

	}
	
	@Override
	protected void onDestroy() {
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
		super.onDestroy();
	}
}
