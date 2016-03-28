package me.peiwo.peiwo;

import android.content.Context;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.jakewharton.rxbinding.internal.MainThreadSubscription;
import rx.Observable;
import rx.Subscriber;

import static com.jakewharton.rxbinding.internal.Preconditions.checkUiThread;

/**
 * Created by wallace on 16/3/22.
 */
public class RxTelephonyManager {
    @CheckResult
    @NonNull
    public static Observable<Boolean> postEvent(@NonNull Context context) {
        return Observable.create(new TelephonyEvent(context));
    }

    public static final class TelephonyEvent implements Observable.OnSubscribe<Boolean> {
        private Context context;

        public TelephonyEvent(Context context) {
            this.context = context;
        }

        @Override
        public void call(Subscriber<? super Boolean> subscriber) {
            checkUiThread();
            PhoneStateListener phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    if (!subscriber.isUnsubscribed()) {
                        if (state == TelephonyManager.CALL_STATE_OFFHOOK)
                            subscriber.onNext(true);
                        else subscriber.onNext(false);
                    }
                }
            };
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            subscriber.add(new MainThreadSubscription() {
                @Override
                protected void onUnsubscribe() {
                    context = null;
                    telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
                }
            });
        }
    }
}
