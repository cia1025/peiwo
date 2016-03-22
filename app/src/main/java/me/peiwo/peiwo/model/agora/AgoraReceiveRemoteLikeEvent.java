package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/22.
 */
public class AgoraReceiveRemoteLikeEvent extends AgoraCallEvent {


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public AgoraReceiveRemoteLikeEvent() {
    }

    protected AgoraReceiveRemoteLikeEvent(Parcel in) {
        super(in);
    }

    public static final Creator<AgoraReceiveRemoteLikeEvent> CREATOR = new Creator<AgoraReceiveRemoteLikeEvent>() {
        @Override
        public AgoraReceiveRemoteLikeEvent createFromParcel(Parcel source) {
            return new AgoraReceiveRemoteLikeEvent(source);
        }

        @Override
        public AgoraReceiveRemoteLikeEvent[] newArray(int size) {
            return new AgoraReceiveRemoteLikeEvent[size];
        }
    };
}
