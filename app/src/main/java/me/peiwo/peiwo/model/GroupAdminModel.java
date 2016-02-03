package me.peiwo.peiwo.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fuhaidong on 15/12/8.
 */
public class GroupAdminModel implements Parcelable {
    public int gender;
    public String slogan;
    public String name;
    public String avatar;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.gender);
        dest.writeString(this.slogan);
        dest.writeString(this.name);
        dest.writeString(this.avatar);
    }

    public GroupAdminModel() {
    }

    protected GroupAdminModel(Parcel in) {
        this.gender = in.readInt();
        this.slogan = in.readString();
        this.name = in.readString();
        this.avatar = in.readString();
    }

    public static final Parcelable.Creator<GroupAdminModel> CREATOR = new Parcelable.Creator<GroupAdminModel>() {
        public GroupAdminModel createFromParcel(Parcel source) {
            return new GroupAdminModel(source);
        }

        public GroupAdminModel[] newArray(int size) {
            return new GroupAdminModel[size];
        }
    };
}
