package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/8.
 */
public class AgoraConnectionInterruptedEvent extends AgoraCallEvent {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public AgoraConnectionInterruptedEvent() {
    }

    protected AgoraConnectionInterruptedEvent(Parcel in) {
        super(in);
    }

    public static final Creator<AgoraConnectionInterruptedEvent> CREATOR = new Creator<AgoraConnectionInterruptedEvent>() {
        public AgoraConnectionInterruptedEvent createFromParcel(Parcel source) {
            return new AgoraConnectionInterruptedEvent(source);
        }

        public AgoraConnectionInterruptedEvent[] newArray(int size) {
            return new AgoraConnectionInterruptedEvent[size];
        }
    };
}
