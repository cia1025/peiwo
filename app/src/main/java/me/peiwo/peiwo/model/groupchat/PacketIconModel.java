package me.peiwo.peiwo.model.groupchat;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fuhaidong on 15/12/21.
 */
public class PacketIconModel implements Parcelable {
    //    'id':1,
//            'msg': '由用户111提供',
//            'icons': [
//            'http://www.peiwo.cn/imagesorig/pc/peiwo_logo.png'
//            ]
    public String id;
    public String msg;
    public String[] icons;

    //被选中的图片
    public String send_icon;

    public PacketIconModel(String id, String msg, String send_icon) {
        this.id = id;
        this.msg = msg;
        this.send_icon = send_icon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.msg);
        dest.writeStringArray(this.icons);
        dest.writeString(this.send_icon);
    }

    public PacketIconModel() {
    }

    protected PacketIconModel(Parcel in) {
        this.id = in.readString();
        this.msg = in.readString();
        this.icons = in.createStringArray();
        this.send_icon = in.readString();
    }

    public static final Parcelable.Creator<PacketIconModel> CREATOR = new Parcelable.Creator<PacketIconModel>() {
        public PacketIconModel createFromParcel(Parcel source) {
            return new PacketIconModel(source);
        }

        public PacketIconModel[] newArray(int size) {
            return new PacketIconModel[size];
        }
    };
}
