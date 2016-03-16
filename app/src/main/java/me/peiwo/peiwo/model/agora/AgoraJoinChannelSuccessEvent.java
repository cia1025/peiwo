package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/7.
 */
public class AgoraJoinChannelSuccessEvent extends AgoraCallEvent {
    public int agora_uid;
    public int elapsed;
    public String channel_id;

    public AgoraJoinChannelSuccessEvent(String channel_id, int agora_uid, int elapsed) {
        this.channel_id = channel_id;
        this.agora_uid = agora_uid;
        this.elapsed = elapsed;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.agora_uid);
        dest.writeInt(this.elapsed);
        dest.writeString(this.channel_id);
    }

    protected AgoraJoinChannelSuccessEvent(Parcel in) {
        super(in);
        this.agora_uid = in.readInt();
        this.elapsed = in.readInt();
        this.channel_id = in.readString();
    }

    public static final Creator<AgoraJoinChannelSuccessEvent> CREATOR = new Creator<AgoraJoinChannelSuccessEvent>() {
        public AgoraJoinChannelSuccessEvent createFromParcel(Parcel source) {
            return new AgoraJoinChannelSuccessEvent(source);
        }

        public AgoraJoinChannelSuccessEvent[] newArray(int size) {
            return new AgoraJoinChannelSuccessEvent[size];
        }
    };
}
