package me.peiwo.peiwo;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by wallace on 16/3/7.
 */
public class RxBus {
    private final Subject<Object, Object> _bus = new SerializedSubject<>(PublishSubject.create());

    public void send(Object o) {
        _bus.onNext(o);
    }


    public Observable<Object> toObserverable() {
        return _bus;
    }


    private RxBus() {
    }

    private static class SingletonHolder {
        public static final RxBus INSTANCE = new RxBus();
    }

    public static RxBus provider() {
        return SingletonHolder.INSTANCE;
    }
}
