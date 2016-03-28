package me.peiwo.peiwo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.jakewharton.rxbinding.internal.MainThreadSubscription;
import rx.Observable;
import rx.Subscriber;

import static com.jakewharton.rxbinding.internal.Preconditions.checkUiThread;

/**
 * Created by wallace on 16/3/22.
 */
public class RxHeadSetReceiver {
    @CheckResult
    @NonNull
    public static Observable<Boolean> postEvent(@NonNull Context context) {
        return Observable.create(new HeadSetEvent(context));
    }

    public static final class HeadSetEvent implements Observable.OnSubscribe<Boolean> {
        private Context context;

        public HeadSetEvent(Context context) {
            this.context = context;
        }

        @Override
        public void call(Subscriber<? super Boolean> subscriber) {
            checkUiThread();
            BroadcastReceiver headSetReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (!subscriber.isUnsubscribed()) {
                        if (intent.hasExtra("state")) {
                            if (intent.getIntExtra("state", 0) == 0)
                                subscriber.onNext(false);
                            else if (intent.getIntExtra("state", 0) == 1)
                                subscriber.onNext(true);
                        }
                    }
                }
            };
            context.registerReceiver(headSetReceiver, new IntentFilter("android.intent.action.HEADSET_PLUG"));
            subscriber.add(new MainThreadSubscription() {
                @Override
                protected void onUnsubscribe() {
                    context.unregisterReceiver(headSetReceiver);
                    context = null;
                }
            });
        }
    }
}
