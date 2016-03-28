package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/8.
 */
public class AgoraUserMuteAudioEvent extends AgoraCallEvent {
    public int uid;
    public boolean muted;

    public AgoraUserMuteAudioEvent(int uid, boolean muted) {
        this.uid = uid;
        this.muted = muted;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.uid);
        dest.writeByte(muted ? (byte) 1 : (byte) 0);
    }

    public AgoraUserMuteAudioEvent() {
    }

    protected AgoraUserMuteAudioEvent(Parcel in) {
        super(in);
        this.uid = in.readInt();
        this.muted = in.readByte() != 0;
    }

    public static final Creator<AgoraUserMuteAudioEvent> CREATOR = new Creator<AgoraUserMuteAudioEvent>() {
        public AgoraUserMuteAudioEvent createFromParcel(Parcel source) {
            return new AgoraUserMuteAudioEvent(source);
        }

        public AgoraUserMuteAudioEvent[] newArray(int size) {
            return new AgoraUserMuteAudioEvent[size];
        }
    };
}
