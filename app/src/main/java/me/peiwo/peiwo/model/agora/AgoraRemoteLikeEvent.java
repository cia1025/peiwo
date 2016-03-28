package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/22.
 */
public class AgoraRemoteLikeEvent extends AgoraCallEvent {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public AgoraRemoteLikeEvent() {
    }

    protected AgoraRemoteLikeEvent(Parcel in) {
        super(in);
    }

    public static final Creator<AgoraRemoteLikeEvent> CREATOR = new Creator<AgoraRemoteLikeEvent>() {
        @Override
        public AgoraRemoteLikeEvent createFromParcel(Parcel source) {
            return new AgoraRemoteLikeEvent(source);
        }

        @Override
        public AgoraRemoteLikeEvent[] newArray(int size) {
            return new AgoraRemoteLikeEvent[size];
        }
    };
}
