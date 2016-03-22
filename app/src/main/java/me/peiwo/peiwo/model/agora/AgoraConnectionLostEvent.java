package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/8.
 */
public class AgoraConnectionLostEvent extends AgoraCallEvent {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public AgoraConnectionLostEvent() {
    }

    protected AgoraConnectionLostEvent(Parcel in) {
        super(in);
    }

    public static final Creator<AgoraConnectionLostEvent> CREATOR = new Creator<AgoraConnectionLostEvent>() {
        public AgoraConnectionLostEvent createFromParcel(Parcel source) {
            return new AgoraConnectionLostEvent(source);
        }

        public AgoraConnectionLostEvent[] newArray(int size) {
            return new AgoraConnectionLostEvent[size];
        }
    };
}
