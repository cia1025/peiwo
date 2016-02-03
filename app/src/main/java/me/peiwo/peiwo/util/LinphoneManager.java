package me.peiwo.peiwo.util;


/*
LinphoneManager.java
Copyright (C) 2010  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

import java.util.HashSet;
import java.util.Set;

import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * 
 * Manager of the low level LibLinphone stuff.<br />
 * Including:<ul>
 * <li>Starting C liblinphone</li>
 * <li>Reacting to C liblinphone state changes</li>
 * <li>Calling Linphone android service listener methods</li>
 * <li>Interacting from Android GUI/service with low level SIP stuff/</li>
 * </ul> 
 * 
 * Add Service Listener to react to Linphone state changes.
 * 
 * @author Guillaume Beraudo
 *
 */
public class LinphoneManager {
	public static LinphoneManager instance = null;
 	public static LinphoneManager getInstance() {
		if (instance == null) {
			instance = new LinphoneManager();
		}
		return instance;
	}
 	
	public Boolean isProximitySensorNearby(final SensorEvent event) {
		float threshold = 4.001f; // <= 4 cm is near

		final float distanceInCm = event.values[0];
		final float maxDistance = event.sensor.getMaximumRange();
//		CustomLog.d(TAG,"Proximity sensor report ["+distanceInCm+ "] "+" for max range ["+maxDistance+"]");

		boolean isNear = false;
		if (maxDistance >= 1023.0f) {
			if (distanceInCm < threshold) {
				isNear = true;
			}
		} else {
			if (maxDistance >= 255.0f) { // 兼容联想A668t
				if (distanceInCm <= 0.0f) {
					isNear = true;
				}
			} else {
				if (maxDistance <= threshold) {
					threshold = maxDistance;
				}
				if (distanceInCm < threshold) {
					isNear = true;
				}
			}
		}
		return isNear;
	}


	private boolean sLastProximitySensorValueNearby;
	private Set<Activity> sProximityDependentActivities = new HashSet<Activity>();
	
	private SensorEventListener sProximitySensorListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.timestamp == 0) return; //just ignoring for nexus 1
			long currentTime = System.currentTimeMillis();
			long startTime = PeiwoApp.getApplication().sStartTime;
			if ((currentTime - startTime) < 1500) {
				return;
			}
			proximityNearbyChanged(isProximitySensorNearby(event));
		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	};


	private void simulateProximitySensorNearby(Activity activity, boolean nearby) {
		final Window window = activity.getWindow();
		WindowManager.LayoutParams lAttrs = activity.getWindow().getAttributes();
		View view = ((ViewGroup) window.getDecorView().findViewById(android.R.id.content)).getChildAt(0);
		if (nearby) {
			if(view == null) return;
			lAttrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
//			view.setVisibility(View.INVISIBLE);
			(view.findViewById(R.id.iv_control_screen)).setVisibility(View.VISIBLE);
		} else  {
			lAttrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN); 
			if(view == null) return;
//			view.setVisibility(View.VISIBLE);
			(view.findViewById(R.id.iv_control_screen)).setVisibility(View.GONE);
		}
		window.setAttributes(lAttrs);
	}

	private void proximityNearbyChanged(boolean mLastProximitySensorValueNearby) {
		for (Activity activity : sProximityDependentActivities) {
			simulateProximitySensorNearby(activity, mLastProximitySensorValueNearby);
		}
	}

	public synchronized void startProximitySensorForActivity(Activity activity) {
/*		if(Util.getMobileMode()!=null&&Util.getMobileMode().equals("s2")&&android.os.Build.VERSION.SDK_INT ==17){
        	return;
        }*/
		if (sProximityDependentActivities.contains(activity)) {
//			CustomLog.i(TAG,"proximity sensor already active for " + activity.getLocalClassName());
			return;
		}
		if (sProximityDependentActivities.isEmpty()) {
			SensorManager sm = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
			Sensor s = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			if (s != null) {
				sm.registerListener(sProximitySensorListener, s, SensorManager.SENSOR_DELAY_NORMAL);
//				CustomLog.i(TAG,"Proximity sensor detected, registering");
			}
		} else if (sLastProximitySensorValueNearby){
			simulateProximitySensorNearby(activity, true);
		}
		sProximityDependentActivities.add(activity);
	}

	/**
	 * 停止距离传感
	 * @param activity
	 * @author: xiaozhenhua
	 * @data:2012-12-6 下午4:44:23
	 */
	public synchronized void stopProximitySensorForActivity(Activity activity) {
		if (activity == null)
			return;
		sProximityDependentActivities.remove(activity);
		simulateProximitySensorNearby(activity, false);
		if (sProximityDependentActivities.isEmpty()) {
			SensorManager sm = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
			sm.unregisterListener(sProximitySensorListener);
			sLastProximitySensorValueNearby = false;
		}
	}

	
}
