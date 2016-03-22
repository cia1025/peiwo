package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/22.
 */
public class AgoraNetworkQualityEvent extends AgoraCallEvent {
    public int quality;

    public AgoraNetworkQualityEvent(int quality) {
        this.quality = quality;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.quality);
    }

    public AgoraNetworkQualityEvent() {
    }

    protected AgoraNetworkQualityEvent(Parcel in) {
        super(in);
        this.quality = in.readInt();
    }

    public static final Creator<AgoraNetworkQualityEvent> CREATOR = new Creator<AgoraNetworkQualityEvent>() {
        @Override
        public AgoraNetworkQualityEvent createFromParcel(Parcel source) {
            return new AgoraNetworkQualityEvent(source);
        }

        @Override
        public AgoraNetworkQualityEvent[] newArray(int size) {
            return new AgoraNetworkQualityEvent[size];
        }
    };
}
