package me.peiwo.peiwo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.jakewharton.rxbinding.internal.MainThreadSubscription;
import rx.Observable;
import rx.Subscriber;

import static com.jakewharton.rxbinding.internal.Preconditions.checkUiThread;

/**
 * Created by wallace on 16/3/21.
 */
public class RxRangeSensor {
    @CheckResult
    @NonNull
    public static Observable<Boolean> postEvent(@NonNull Context context) {
        return Observable.create(new SensorEvent(context));
    }


    public static final class SensorEvent implements Observable.OnSubscribe<Boolean> {
        private Context context;

        public SensorEvent(Context context) {
            this.context = context;
        }

        @Override
        public void call(Subscriber<? super Boolean> subscriber) {
            checkUiThread();
            SensorEventListener sProximitySensorListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(android.hardware.SensorEvent event) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(isNear(event));
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }

                private Boolean isNear(android.hardware.SensorEvent event) {
                    float threshold = 4.001f; // <= 4 cm is near
                    final float distanceInCm = event.values[0];
                    final float maxDistance = event.sensor.getMaximumRange();
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
            };
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            if (sensor != null) {
                sensorManager.registerListener(sProximitySensorListener, sensor, SensorManager.SENSOR_DELAY_UI);
            }
            subscriber.add(new MainThreadSubscription() {
                @Override
                protected void onUnsubscribe() {
                    context = null;
                    sensorManager.unregisterListener(sProximitySensorListener, sensor);
                }
            });
        }
    }
}
