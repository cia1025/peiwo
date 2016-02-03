package me.peiwo.peiwo.model.groupchat;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fuhaidong on 16/1/19.
 */
public class GroupsTitle implements Parcelable {
    public String group_prefix;
    public int group_count;

    public String group_title;

    public GroupsTitle(String group_prefix, int group_count) {
        this.group_prefix = group_prefix;
        this.group_count = group_count;
        this.group_title = String.format("%s（%d）", group_prefix, group_count);
    }

    public GroupsTitle() {
    }


    public void appendTitle() {
        this.group_title = String.format("%s（%d）", group_prefix, group_count);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.group_prefix);
        dest.writeInt(this.group_count);
        dest.writeString(this.group_title);
    }

    protected GroupsTitle(Parcel in) {
        this.group_prefix = in.readString();
        this.group_count = in.readInt();
        this.group_title = in.readString();
    }

    public static final Parcelable.Creator<GroupsTitle> CREATOR = new Parcelable.Creator<GroupsTitle>() {
        public GroupsTitle createFromParcel(Parcel source) {
            return new GroupsTitle(source);
        }

        public GroupsTitle[] newArray(int size) {
            return new GroupsTitle[size];
        }
    };
}
