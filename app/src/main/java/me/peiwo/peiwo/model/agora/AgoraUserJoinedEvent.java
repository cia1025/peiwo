package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/7.
 */
public class AgoraUserJoinedEvent extends AgoraCallEvent {
    public int uid;
    public int elapsed;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.uid);
        dest.writeInt(this.elapsed);
    }

    public AgoraUserJoinedEvent() {
    }

    public AgoraUserJoinedEvent(int uid, int elapsed) {
        this.uid = uid;
        this.elapsed = elapsed;
    }

    protected AgoraUserJoinedEvent(Parcel in) {
        super(in);
        this.uid = in.readInt();
        this.elapsed = in.readInt();
    }

    public static final Creator<AgoraUserJoinedEvent> CREATOR = new Creator<AgoraUserJoinedEvent>() {
        public AgoraUserJoinedEvent createFromParcel(Parcel source) {
            return new AgoraUserJoinedEvent(source);
        }

        public AgoraUserJoinedEvent[] newArray(int size) {
            return new AgoraUserJoinedEvent[size];
        }
    };
}
