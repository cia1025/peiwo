package me.peiwo.peiwo.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gaoxiang on 2015/9/10.
 */
public class GroupMemberModel implements Parcelable {


    public String nickname;
    public String name;
    public int uid;
    public String member_type;
    public String avatar;
    public int notify_flag;
    public int show_nickname;
    public boolean isFirstNewbie;

    public GroupMemberModel() {
    }

    public GroupMemberModel(String nickname, String name, int uid, String member_type, String avatar) {
        this.nickname = nickname;
        this.name = name;
        this.uid = uid;
        this.member_type = member_type;
        this.avatar = avatar;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.nickname);
        dest.writeString(this.name);
        dest.writeInt(this.uid);
        dest.writeString(this.member_type);
        dest.writeString(this.avatar);
        dest.writeInt(this.notify_flag);
        dest.writeInt(this.show_nickname);
    }

    protected GroupMemberModel(Parcel in) {
        this.nickname = in.readString();
        this.name = in.readString();
        this.uid = in.readInt();
        this.member_type = in.readString();
        this.avatar = in.readString();
        this.notify_flag = in.readInt();
        this.show_nickname = in.readInt();
    }

    public static final Parcelable.Creator<GroupMemberModel> CREATOR = new Parcelable.Creator<GroupMemberModel>() {
        public GroupMemberModel createFromParcel(Parcel source) {
            return new GroupMemberModel(source);
        }

        public GroupMemberModel[] newArray(int size) {
            return new GroupMemberModel[size];
        }
    };
}