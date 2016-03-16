package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/7.
 */
public class AgoraCalledMessageEvent extends AgoraCallEvent {
    public Data data;
    public String ssn;
    public int seq;
    public int uid;
    public String sid;
    public double last_update_time;
    public int msg_type;


    public static class Data implements android.os.Parcelable {
        public int call_id;
        public int channel;
        public AgoraUser user;
        public int video;
        public String channel_id;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.call_id);
            dest.writeInt(this.channel);
            dest.writeParcelable(this.user, 0);
            dest.writeInt(this.video);
            dest.writeString(this.channel_id);
        }

        public Data() {
        }

        protected Data(Parcel in) {
            this.call_id = in.readInt();
            this.channel = in.readInt();
            this.user = in.readParcelable(AgoraUser.class.getClassLoader());
            this.video = in.readInt();
            this.channel_id = in.readString();
        }

        public static final Creator<Data> CREATOR = new Creator<Data>() {
            public Data createFromParcel(Parcel source) {
                return new Data(source);
            }

            public Data[] newArray(int size) {
                return new Data[size];
            }
        };
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.data, 0);
        dest.writeString(this.ssn);
        dest.writeInt(this.seq);
        dest.writeInt(this.uid);
        dest.writeString(this.sid);
        dest.writeDouble(this.last_update_time);
        dest.writeInt(this.msg_type);
    }

    public AgoraCalledMessageEvent() {
    }

    protected AgoraCalledMessageEvent(Parcel in) {
        super(in);
        this.data = in.readParcelable(Data.class.getClassLoader());
        this.ssn = in.readString();
        this.seq = in.readInt();
        this.uid = in.readInt();
        this.sid = in.readString();
        this.last_update_time = in.readDouble();
        this.msg_type = in.readInt();
    }

    public static final Creator<AgoraCalledMessageEvent> CREATOR = new Creator<AgoraCalledMessageEvent>() {
        public AgoraCalledMessageEvent createFromParcel(Parcel source) {
            return new AgoraCalledMessageEvent(source);
        }

        public AgoraCalledMessageEvent[] newArray(int size) {
            return new AgoraCalledMessageEvent[size];
        }
    };
}
