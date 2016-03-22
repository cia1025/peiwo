package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/17.
 */
public class AgoraWildCallLikeEvent extends AgoraCallEvent {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public AgoraWildCallLikeEvent() {
    }

    protected AgoraWildCallLikeEvent(Parcel in) {
        super(in);
    }

    public static final Creator<AgoraWildCallLikeEvent> CREATOR = new Creator<AgoraWildCallLikeEvent>() {
        @Override
        public AgoraWildCallLikeEvent createFromParcel(Parcel source) {
            return new AgoraWildCallLikeEvent(source);
        }

        @Override
        public AgoraWildCallLikeEvent[] newArray(int size) {
            return new AgoraWildCallLikeEvent[size];
        }
    };
}
