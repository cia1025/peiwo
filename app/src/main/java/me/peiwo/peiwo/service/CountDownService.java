package me.peiwo.peiwo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import me.peiwo.peiwo.eventbus.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import java.util.concurrent.TimeUnit;

/**
 * Created by gaoxiang on 16/2/24.
 */
public class CountDownService extends Service {
    public static final String COUNT_DOWN_ACTION = "me.peiwo.peiwo.ACTION_COUNT_DOWN";
    public static final String STOP_SELF = "me.peiwo.peiwo.STOP_SELF";
    private static final long COUNT_DOWN_SEC = 30;
    private Subscription mSubscription;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return 1;
        countDown();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void countDown() {
        mSubscription = Observable.interval(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            Intent it = new Intent();
            if (aLong <= COUNT_DOWN_SEC) {
                it.putExtra("count", aLong);
                it.setAction(COUNT_DOWN_ACTION);
                EventBus.getDefault().post(it);
            } else {
                stopSelf();
            }
        });
    }

    public void onEventMainThread(Intent intent) {
        if (intent == null || intent.getAction().equals("")) {
            return;
        }
        if (intent.getAction().equals(STOP_SELF)) {
            stopSelf();
            if (mSubscription != null && !mSubscription.isUnsubscribed()) {
                mSubscription.unsubscribe();
            }
        }
    }
}