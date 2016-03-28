package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/22.
 */
public class AgoraAudioQualityEvent extends AgoraCallEvent {
    public int uid;
    public int quality;
    public short delay;
    public short lost;

    public AgoraAudioQualityEvent(int uid, int quality, short delay, short lost) {
        this.uid = uid;
        this.quality = quality;
        this.delay = delay;
        this.lost = lost;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.uid);
        dest.writeInt(this.quality);
        dest.writeInt(this.delay);
        dest.writeInt(this.lost);
    }

    public AgoraAudioQualityEvent() {
    }

    protected AgoraAudioQualityEvent(Parcel in) {
        super(in);
        this.uid = in.readInt();
        this.quality = in.readInt();
        this.delay = (short) in.readInt();
        this.lost = (short) in.readInt();
    }

    public static final Creator<AgoraAudioQualityEvent> CREATOR = new Creator<AgoraAudioQualityEvent>() {
        @Override
        public AgoraAudioQualityEvent createFromParcel(Parcel source) {
            return new AgoraAudioQualityEvent(source);
        }

        @Override
        public AgoraAudioQualityEvent[] newArray(int size) {
            return new AgoraAudioQualityEvent[size];
        }
    };
}
