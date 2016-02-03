package me.peiwo.peiwo.util;

import me.peiwo.peiwo.PeiwoApp;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

public class LocationUtil {
	/**
	 * 定位获取自己所在位置
	 * 
	 * @param back
	 */
	public static void getMyLocation(final GetLocationCallback back) {
		final LocationClient mLocationClient = new LocationClient(
				PeiwoApp.getApplication());
		mLocationClient.registerLocationListener(new BDLocationListener() {
			@Override
			public void onReceiveLocation(BDLocation location) {
				String province = "";
				String city = "";
				if (location != null) {
					if (!TextUtils.isEmpty(location.getProvince())) {
						province = location.getProvince();
					}
					if (!TextUtils.isEmpty(location.getCity())) {
						city = location.getCity();
					}					
				}
				if (!TextUtils.isEmpty(province) || !TextUtils.isEmpty(city)) {
					if (back != null) {
						back.onComplete(province, city);
					}
				} else {
					if (back != null) {
						back.onError();
					}
				}
				mLocationClient.stop();
			}
		});
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式
		option.setCoorType("gcj02");//返回的定位结果是百度经纬度，默认值gcj02
		option.setScanSpan(1000);//设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);
		mLocationClient.setLocOption(option);
		mLocationClient.start();
	}

	public abstract interface GetLocationCallback {
		public abstract void onComplete(String adress, String city);
		public abstract void onError();
	}
}
