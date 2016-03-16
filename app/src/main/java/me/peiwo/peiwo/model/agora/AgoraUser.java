package me.peiwo.peiwo.model.agora;

import android.os.Parcel;
import android.os.Parcelable;
import me.peiwo.peiwo.model.ImageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wallace on 16/3/7.
 */
public class AgoraUser implements Parcelable {
    public int emotion;
    public int uid;
    public String profession;
    public String avatar_thumbnail;
    public List<ImageModel> images = new ArrayList<>();
    public String[] movie_tags;
    public String[] travel_tags;
    public String city;
    public int state;
    public int score;
    public String[] sport_tags;
    public String province;
    public String[] book_tags;
    public String[] music_tags;
    public String[] tags;
    public int price;
    public String[] app_tags;
    public String birthday;
    public String slogan;
    public String name;
    public int gender;
    public int call_duration;
    public int flags;
    public String avatar;
    public String[] food_tags;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.emotion);
        dest.writeInt(this.uid);
        dest.writeString(this.profession);
        dest.writeString(this.avatar_thumbnail);
        dest.writeTypedList(images);
        dest.writeStringArray(this.movie_tags);
        dest.writeStringArray(this.travel_tags);
        dest.writeString(this.city);
        dest.writeInt(this.state);
        dest.writeInt(this.score);
        dest.writeStringArray(this.sport_tags);
        dest.writeString(this.province);
        dest.writeStringArray(this.book_tags);
        dest.writeStringArray(this.music_tags);
        dest.writeStringArray(this.tags);
        dest.writeInt(this.price);
        dest.writeStringArray(this.app_tags);
        dest.writeString(this.birthday);
        dest.writeString(this.slogan);
        dest.writeString(this.name);
        dest.writeInt(this.gender);
        dest.writeInt(this.call_duration);
        dest.writeInt(this.flags);
        dest.writeString(this.avatar);
        dest.writeStringArray(this.food_tags);
    }

    public AgoraUser() {
    }

    protected AgoraUser(Parcel in) {
        this.emotion = in.readInt();
        this.uid = in.readInt();
        this.profession = in.readString();
        this.avatar_thumbnail = in.readString();
        this.images = in.createTypedArrayList(ImageModel.CREATOR);
        this.movie_tags = in.createStringArray();
        this.travel_tags = in.createStringArray();
        this.city = in.readString();
        this.state = in.readInt();
        this.score = in.readInt();
        this.sport_tags = in.createStringArray();
        this.province = in.readString();
        this.book_tags = in.createStringArray();
        this.music_tags = in.createStringArray();
        this.tags = in.createStringArray();
        this.price = in.readInt();
        this.app_tags = in.createStringArray();
        this.birthday = in.readString();
        this.slogan = in.readString();
        this.name = in.readString();
        this.gender = in.readInt();
        this.call_duration = in.readInt();
        this.flags = in.readInt();
        this.avatar = in.readString();
        this.food_tags = in.createStringArray();
    }

    public static final Parcelable.Creator<AgoraUser> CREATOR = new Parcelable.Creator<AgoraUser>() {
        public AgoraUser createFromParcel(Parcel source) {
            return new AgoraUser(source);
        }

        public AgoraUser[] newArray(int size) {
            return new AgoraUser[size];
        }
    };
}
