package me.peiwo.peiwo.model.agora;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wallace on 16/3/7.
 */
public class AgoraUserOffineEvent extends AgoraCallEvent implements Parcelable {
    public int uid;
    public int reason;

    public AgoraUserOffineEvent(int uid, int reason) {
        this.uid = uid;
        this.reason = reason;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.uid);
        dest.writeInt(this.reason);
    }

    public AgoraUserOffineEvent() {
    }

    protected AgoraUserOffineEvent(Parcel in) {
        super(in);
        this.uid = in.readInt();
        this.reason = in.readInt();
    }

    public static final Creator<AgoraUserOffineEvent> CREATOR = new Creator<AgoraUserOffineEvent>() {
        public AgoraUserOffineEvent createFromParcel(Parcel source) {
            return new AgoraUserOffineEvent(source);
        }

        public AgoraUserOffineEvent[] newArray(int size) {
            return new AgoraUserOffineEvent[size];
        }
    };
}
