package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/22.
 */
public class AgoraLikeResponseEvent extends AgoraCallEvent {


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public AgoraLikeResponseEvent() {
    }

    protected AgoraLikeResponseEvent(Parcel in) {
        super(in);
    }

    public static final Creator<AgoraLikeResponseEvent> CREATOR = new Creator<AgoraLikeResponseEvent>() {
        @Override
        public AgoraLikeResponseEvent createFromParcel(Parcel source) {
            return new AgoraLikeResponseEvent(source);
        }

        @Override
        public AgoraLikeResponseEvent[] newArray(int size) {
            return new AgoraLikeResponseEvent[size];
        }
    };
}
