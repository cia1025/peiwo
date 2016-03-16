package me.peiwo.peiwo.model.agora;

import android.os.Parcel;

/**
 * Created by wallace on 16/3/7.
 */
public class AgoraOnLeaveChannelEvent extends AgoraCallEvent {
    public int totalDuration;
    public int txBytes;
    public int rxBytes;
    public int txKBitRate;
    public int rxKBitRate;
    public int lastmileQuality;
    public double cpuTotalUsage;
    public double cpuAppUsage;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.totalDuration);
        dest.writeInt(this.txBytes);
        dest.writeInt(this.rxBytes);
        dest.writeInt(this.txKBitRate);
        dest.writeInt(this.rxKBitRate);
        dest.writeInt(this.lastmileQuality);
        dest.writeDouble(this.cpuTotalUsage);
        dest.writeDouble(this.cpuAppUsage);
    }

    public AgoraOnLeaveChannelEvent() {
    }

    public AgoraOnLeaveChannelEvent(int totalDuration, int txBytes, int rxBytes, int txKBitRate, int rxKBitRate, int lastmileQuality, double cpuTotalUsage, double cpuAppUsage) {
        this.totalDuration = totalDuration;
        this.txBytes = txBytes;
        this.rxBytes = rxBytes;
        this.txKBitRate = txKBitRate;
        this.rxKBitRate = rxKBitRate;
        this.lastmileQuality = lastmileQuality;
        this.cpuTotalUsage = cpuTotalUsage;
        this.cpuAppUsage = cpuAppUsage;
    }

    protected AgoraOnLeaveChannelEvent(Parcel in) {
        super(in);
        this.totalDuration = in.readInt();
        this.txBytes = in.readInt();
        this.rxBytes = in.readInt();
        this.txKBitRate = in.readInt();
        this.rxKBitRate = in.readInt();
        this.lastmileQuality = in.readInt();
        this.cpuTotalUsage = in.readDouble();
        this.cpuAppUsage = in.readDouble();
    }

    public static final Creator<AgoraOnLeaveChannelEvent> CREATOR = new Creator<AgoraOnLeaveChannelEvent>() {
        public AgoraOnLeaveChannelEvent createFromParcel(Parcel source) {
            return new AgoraOnLeaveChannelEvent(source);
        }

        public AgoraOnLeaveChannelEvent[] newArray(int size) {
            return new AgoraOnLeaveChannelEvent[size];
        }
    };
}
