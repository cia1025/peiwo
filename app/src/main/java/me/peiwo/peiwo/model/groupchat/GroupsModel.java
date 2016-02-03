package me.peiwo.peiwo.model.groupchat;

import android.os.Parcel;
import android.os.Parcelable;
import me.peiwo.peiwo.model.TabfindGroupModel;

import java.util.List;

/**
 * Created by fuhaidong on 16/1/18.
 */
public class GroupsModel implements Parcelable {
    public List<TabfindGroupModel> admin;
    public List<TabfindGroupModel> member;
    public List<TabfindGroupModel> newbie;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(admin);
        dest.writeTypedList(member);
        dest.writeTypedList(newbie);
    }

    public GroupsModel() {
    }

    protected GroupsModel(Parcel in) {
        this.admin = in.createTypedArrayList(TabfindGroupModel.CREATOR);
        this.member = in.createTypedArrayList(TabfindGroupModel.CREATOR);
        this.newbie = in.createTypedArrayList(TabfindGroupModel.CREATOR);
    }

    public static final Parcelable.Creator<GroupsModel> CREATOR = new Parcelable.Creator<GroupsModel>() {
        public GroupsModel createFromParcel(Parcel source) {
            return new GroupsModel(source);
        }

        public GroupsModel[] newArray(int size) {
            return new GroupsModel[size];
        }
    };
}
