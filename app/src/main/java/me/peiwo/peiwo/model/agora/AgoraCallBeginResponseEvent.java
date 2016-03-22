package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/21.
 */
public class AgoraCallBeginResponseEvent extends AgoraCallEvent {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public AgoraCallBeginResponseEvent() {
    }

    protected AgoraCallBeginResponseEvent(Parcel in) {
        super(in);
    }

    public static final Creator<AgoraCallBeginResponseEvent> CREATOR = new Creator<AgoraCallBeginResponseEvent>() {
        @Override
        public AgoraCallBeginResponseEvent createFromParcel(Parcel source) {
            return new AgoraCallBeginResponseEvent(source);
        }

        @Override
        public AgoraCallBeginResponseEvent[] newArray(int size) {
            return new AgoraCallBeginResponseEvent[size];
        }
    };
}
