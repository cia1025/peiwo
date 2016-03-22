package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/8.
 */
public class AgoraHungUpByServEvent extends AgoraCallEvent {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public AgoraHungUpByServEvent() {
    }

    protected AgoraHungUpByServEvent(Parcel in) {
        super(in);
    }

    public static final Creator<AgoraHungUpByServEvent> CREATOR = new Creator<AgoraHungUpByServEvent>() {
        public AgoraHungUpByServEvent createFromParcel(Parcel source) {
            return new AgoraHungUpByServEvent(source);
        }

        public AgoraHungUpByServEvent[] newArray(int size) {
            return new AgoraHungUpByServEvent[size];
        }
    };
}
