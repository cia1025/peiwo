package me.peiwo.peiwo.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fuhaidong on 15/12/8.
 */
public class TabfindGroupModel implements Parcelable {
//    notice": "hi4",
//            "is_recruiting": 1,
//            "admin_id": 4022055,
//            "group_prefix": "i4",
//            "admin": {
//        "gender": 1,
//                "slogan": "\u72d7\u72d7\u547d\u540d\n\n\n\n\n\n\n\nsrrr\nsrttttu\n\n\n\n\n\nsrrt\nmingmingh\nghhg\nsrrrrrrrprrrssr\ns\n\nsrrr\nsrrrr\n\nsp\ns\ns\u2006s\u2006s\u2006s",
//                "name": "\u98ce\u7b49\u660e\u5e74\u8f70\u8f70\u8f70\u8f70\u660e\u660e\u54e6",
//                "avatar": "http://peiwo-test.bjcnc.scs.sohucs.com//4022055_2a93ad60b2839a53d4e1c318e797f5bd"
//    },
//            "avatar": "http://picapi.ooopic.com/11/17/44/40b1OOOPIC2c.jpg",
//            "group_id": "200789_6ccaea5a971e11e5",
//            "ticket_price": 0


    public String notice;
    public int is_recruiting;
    public String admin_id;
    public String group_name;
    public GroupAdminModel admin;
    public String avatar;
    public String group_id;
    public int ticket_price;
    public String member_type;
//    public int member_counts;
//    public int newbie_counts;

    public int show_nickname;

    public int amount;

    //成员数量
    public int member_number;
    //群人数（含新人）
    public int total_number;

    //local
    public String msg1;
    public String msg2;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.notice);
        dest.writeInt(this.is_recruiting);
        dest.writeString(this.admin_id);
        dest.writeString(this.group_name);
        dest.writeParcelable(this.admin, 0);
        dest.writeString(this.avatar);
        dest.writeString(this.group_id);
        dest.writeInt(this.ticket_price);
        dest.writeString(this.member_type);
        dest.writeInt(this.show_nickname);
        dest.writeInt(this.amount);
        dest.writeInt(this.member_number);
        dest.writeInt(this.total_number);
        dest.writeString(this.msg1);
        dest.writeString(this.msg2);
    }

    public TabfindGroupModel() {
    }

    protected TabfindGroupModel(Parcel in) {
        this.notice = in.readString();
        this.is_recruiting = in.readInt();
        this.admin_id = in.readString();
        this.group_name = in.readString();
        this.admin = in.readParcelable(GroupAdminModel.class.getClassLoader());
        this.avatar = in.readString();
        this.group_id = in.readString();
        this.ticket_price = in.readInt();
        this.member_type = in.readString();
        this.show_nickname = in.readInt();
        this.amount = in.readInt();
        this.member_number = in.readInt();
        this.total_number = in.readInt();
        this.msg1 = in.readString();
        this.msg2 = in.readString();
    }

    public static final Parcelable.Creator<TabfindGroupModel> CREATOR = new Parcelable.Creator<TabfindGroupModel>() {
        public TabfindGroupModel createFromParcel(Parcel source) {
            return new TabfindGroupModel(source);
        }

        public TabfindGroupModel[] newArray(int size) {
            return new TabfindGroupModel[size];
        }
    };
}
